package com.spring.ai.rag.tools;

import com.spring.ai.rag.model.Doctor;
import com.spring.ai.rag.service.AppointmentService;
import com.spring.ai.rag.service.AppointmentService.BookingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tools exposed to the Claude chat model via Spring AI's function/tool calling.
 * When a user asks to book an appointment, the model should:
 *   1. Call findAvailableSlots to see open times for the requested doctor/date.
 *   2. Call bookAppointment once a specific slot and the patient's details are confirmed.
 *
 * bookAppointment accepts ANY valid patientEmail — it is never restricted to a
 * fixed/hardcoded address. Whatever email the user provides in conversation is
 * the address the confirmation is sent to.
 */
@Slf4j
@Component
public class AppointmentTools {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentService appointmentService;

    public AppointmentTools(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Tool(description = "Find available appointment slots for a doctor on a specific date. " +
            "Use this BEFORE booking, to show the user open time slots. " +
            "doctorName can be a full or partial name (e.g. 'Dr. Steven Mehta' or 'Steven Mehta'). " +
            "date must be in yyyy-MM-dd format.")
    public String findAvailableSlots(
            @ToolParam(description = "Full or partial doctor name, e.g. 'Dr. Steven Mehta'") String doctorName,
            @ToolParam(description = "Date to check, in yyyy-MM-dd format") String date) {

        Optional<Doctor> doctorOpt = appointmentService.findDoctorByName(doctorName);
        if (doctorOpt.isEmpty()) {
            return "No doctor found matching \"" + doctorName + "\". Please check the spelling or ask for the full name.";
        }
        Doctor doctor = doctorOpt.get();

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date, DATE_FMT);
        } catch (DateTimeParseException e) {
            return "Invalid date format. Please provide the date as yyyy-MM-dd.";
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            return "That date is in the past. Please choose a future date.";
        }

        List<LocalTime> slots = appointmentService.getAvailableSlots(doctor.getId(), parsedDate);
        if (slots.isEmpty()) {
            return doctor.getName() + " has no available slots on " + date + ". Try another date.";
        }

        String slotList = slots.stream().map(t -> t.format(TIME_FMT)).collect(Collectors.joining(", "));
        return "Available slots for " + doctor.getName() + " on " + date + ": " + slotList;
    }

    @Tool(description = "Book an appointment for a patient with a specific doctor, date, and time, " +
            "and send a confirmation email to the patient's email address. The patient email can be " +
            "ANY valid address the user provides — it is not restricted to any fixed account. " +
            "Only call this AFTER confirming the slot is available via findAvailableSlots, and after " +
            "the user has provided their name, email, and the exact date/time they want.")
    public String bookAppointment(
            @ToolParam(description = "Full or partial doctor name, e.g. 'Dr. Steven Mehta'") String doctorName,
            @ToolParam(description = "Appointment date in yyyy-MM-dd format") String date,
            @ToolParam(description = "Appointment time in 24-hour HH:mm format, e.g. '14:30'") String time,
            @ToolParam(description = "Patient's full name") String patientName,
            @ToolParam(description = "Patient's email address (any valid address) — confirmation is sent here") String patientEmail) {

        Optional<Doctor> doctorOpt = appointmentService.findDoctorByName(doctorName);
        if (doctorOpt.isEmpty()) {
            return "No doctor found matching \"" + doctorName + "\". Please check the spelling.";
        }
        Doctor doctor = doctorOpt.get();

        LocalDate parsedDate;
        LocalTime parsedTime;
        try {
            parsedDate = LocalDate.parse(date, DATE_FMT);
            parsedTime = LocalTime.parse(time, TIME_FMT);
        } catch (DateTimeParseException e) {
            return "Invalid date or time format. Date must be yyyy-MM-dd and time must be HH:mm (24-hour).";
        }

        if (patientEmail == null || !patientEmail.contains("@")) {
            return "A valid patient email is required to send the confirmation. Please ask the user for their email.";
        }

        try {
            BookingResult result = appointmentService.bookAppointment(
                    doctor, parsedDate, parsedTime, patientName, patientEmail);

            String base = String.format(
                    "Appointment confirmed! Booking #%d — %s with %s on %s at %s.",
                    result.appointment().getId(), patientName, doctor.getName(), date, time
            );

            if (result.emailSent()) {
                return base + " A confirmation email has been sent to " + patientEmail + ".";
            } else {
                return base + " NOTE: the confirmation email to " + patientEmail + " could not be sent " +
                        "(the mail server rejected the send). The booking itself is still confirmed — " +
                        "let the user know to check with the clinic if they don't receive an email, and " +
                        "flag to the admin that the email service needs checking.";
            }
        } catch (IllegalStateException e) {
            return e.getMessage() + " Please call findAvailableSlots again to see current open times.";
        }
    }

    public String getImageUrl(String photoUrl) {
        String base64Image = null;
        try {
            File imageFile = new File(photoUrl);

            if (!imageFile.exists()) {
                System.err.println("Image file not found: " + imageFile.getAbsolutePath());
                return null;
            }

            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.err.println("Failed to convert image to HTML: " + e.getMessage());
            e.printStackTrace();
        }
        return buildHtml(base64Image, detectMimeType(photoUrl));
    }

    private static String detectMimeType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream"; // fallback
    }

    private static String buildHtml(String base64Image, String mimeType) {
        return "\n<img id=\"embeddedImage\" src=\"data:" + mimeType + ";base64," + base64Image + "\">\n";
    }
}
