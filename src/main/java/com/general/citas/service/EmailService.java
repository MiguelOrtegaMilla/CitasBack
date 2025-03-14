package com.general.citas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.general.citas.DTO.AppointmentResponseDTO;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

   public void sendAppointmentNotification(String toEmail, String subject, AppointmentResponseDTO appointment, String action) {
        String body = buildEmailBody(appointment, action);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
    
        mailSender.send(message);
    }


    private String buildEmailBody(AppointmentResponseDTO appointmentDTO, String action) {
        String date = appointmentDTO.getDatetime().toLocalDate().toString();
        String time = appointmentDTO.getDatetime().toLocalTime().toString();

        return switch (action.toLowerCase()) {
                case "create" -> String.format(
                "Hola %s,\n\nTu cita ha sido confirmada:\n\nServicio: %s\nFecha: %s\nHora: %s\n\nGracias por confiar en nosotros.",
                appointmentDTO.getUserName(),
                appointmentDTO.getServiceName(),
                date,
                time
                );
                case "cancel" -> String.format(
                "Hola %s,\n\nTu cita para el servicio %s programada para el %s a las %s ha sido cancelada.\n\nSi necesitas ayuda, no dudes en contactarnos.",
                appointmentDTO.getUserName(),
                appointmentDTO.getServiceName(),
                date,
                time
                );
                case "update" -> String.format(
                "Hola %s,\n\nTu cita ha sido actualizada:\n\nServicio: %s\nNueva Fecha: %s\nNueva Hora: %s\n\nGracias por confiar en nosotros.",
                appointmentDTO.getUserName(),
                appointmentDTO.getServiceName(),
                date,
                time
                );
                default -> throw new IllegalArgumentException("Acción desconocida: " + action);
        };
    }

}
