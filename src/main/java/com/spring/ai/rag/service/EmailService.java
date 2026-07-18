package com.spring.ai.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@doctorrag.local}")
    private String fromAddress;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a confirmation email to ANY recipient address passed in — the "to" address
     * is never hardcoded or restricted to a fixed account. Returns true if the send
     * succeeded, false if it failed (e.g. sender SMTP auth issue), so callers can
     * decide how to inform the user.
     */
    public boolean sendAppointmentConfirmation(String toEmail, String patientName, String doctorName,
                                                LocalDate date, LocalTime time) {
        if (toEmail == null || !toEmail.contains("@")) {
            log.warn("Refusing to send email — invalid recipient address: {}", toEmail);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Appointment Confirmed with " + doctorName);
            message.setText(String.format(
                "Hi %s,%n%n" +
                "Your appointment has been confirmed:%n%n" +
                "Appointment Id: %s%n" +
                "Doctor: %s%n" +
                "Date: %s%n" +
                "Time: %s%n%n" +
                "Please arrive 10 minutes early. If you need to reschedule or cancel, " +
                "please contact the clinic directly.%n%n" +
                "Thank you,%n" +
                "Doctor AI Assistant",
                patientName, new Random().nextInt(9000) + 1000, doctorName, date.format(DATE_FMT), time.format(TIME_FMT)
            ));
            log.info("Sending email to {}", toEmail);
            mailSender.send(message);
            log.info("Confirmation email sent to {}", toEmail);
            return true;
        } catch (Exception e) {
            // Don't let email failure break the booking flow — log and report failure to caller.
            log.error("Failed to send confirmation email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
