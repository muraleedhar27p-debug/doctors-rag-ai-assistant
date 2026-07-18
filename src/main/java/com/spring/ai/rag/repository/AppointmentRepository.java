package com.spring.ai.rag.repository;

import com.spring.ai.rag.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(Long doctorId, LocalDate date, String status);

    List<Appointment> findByPatientEmailIgnoreCase(String patientEmail);
}
