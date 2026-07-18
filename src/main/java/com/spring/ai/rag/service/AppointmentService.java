package com.spring.ai.rag.service;

import com.spring.ai.rag.model.Appointment;
import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.repository.AppointmentRepository;
import com.spring.ai.rag.repository.DoctorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentService {

    private static final LocalTime DAY_START = LocalTime.of(9, 0);
    private static final LocalTime DAY_END = LocalTime.of(23, 30);
    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END = LocalTime.of(13, 0);
    private static final int SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               DoctorRepository doctorRepository,
                               EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
    }

    /**
     * Finds the doctor whose name best matches the given (possibly partial) name.
     */
    public Optional<Doctor> findDoctorByName(String doctorName) {
        if (doctorName == null || doctorName.isBlank()) return Optional.empty();
        String cleaned = doctorName.replace("Dr.", "").replace("Dr", "").trim();
        List<Doctor> matches = doctorRepository.findByNameContainingIgnoreCase(cleaned);
        return matches.stream().findFirst();
    }

    /**
     * Returns all open 30-minute slots for a doctor on a given date
     * (9am–5pm, minus a 12–1pm lunch break, minus already-booked slots).
     */
    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {
        Set<LocalTime> booked = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatus(doctorId, date, "BOOKED")
                .stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        List<LocalTime> available = new ArrayList<>();
        LocalTime cursor = DAY_START;
        while (cursor.isBefore(DAY_END)) {
            boolean isLunch = !cursor.isBefore(LUNCH_START) && cursor.isBefore(LUNCH_END);
            if (!isLunch && !booked.contains(cursor)) {
                available.add(cursor);
            }
            cursor = cursor.plusMinutes(SLOT_MINUTES);
        }
        return available;
    }

    /**
     * Result of a booking attempt: the saved appointment plus whether the
     * confirmation email actually went out (to whatever address was given).
     */
    public record BookingResult(Appointment appointment, boolean emailSent) {}

    /**
     * Books an appointment if the requested slot is free, then attempts to send a
     * confirmation email to the given patientEmail — any valid address, never fixed
     * to a specific account. Booking succeeds even if the email fails to send;
     * the caller is told whether the email went out via BookingResult.emailSent().
     * Throws IllegalStateException if the slot is already taken.
     */
    public BookingResult bookAppointment(Doctor doctor, LocalDate date, LocalTime time,
                                          String patientName, String patientEmail) {
        List<LocalTime> available = getAvailableSlots(doctor.getId(), date);
        if (!available.contains(time)) {
            throw new IllegalStateException(
                "The " + time + " slot on " + date + " with " + doctor.getName() + " is no longer available.");
        }

        Appointment appointment = Appointment.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .patientName(patientName)
                .patientEmail(patientEmail)
                .appointmentDate(date)
                .appointmentTime(time)
                .status("BOOKED")
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Booked appointment #{} for {} with {} on {} at {}",
                saved.getId(), patientName, doctor.getName(), date, time);

        boolean emailSent = emailService.sendAppointmentConfirmation(
                patientEmail, patientName, doctor.getName(), date, time);

        return new BookingResult(saved, emailSent);
    }
}
