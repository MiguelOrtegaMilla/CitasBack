package com.general.citas.converter;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.general.citas.DTO.AppointmentRequestDTO;
import com.general.citas.DTO.AppointmentResponseDTO;
import com.general.citas.model.Appointment;
import com.general.citas.model.Servicio;
import com.general.citas.model.User;
import com.general.citas.repository.ServiceRepository;
import com.general.citas.repository.UserRepository;


@Component
public class AppointmentConverter {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;


    public AppointmentRequestDTO appointmentToAppointmentRequestDTO(Appointment appointment) {
        return AppointmentRequestDTO.builder()
                .userUuid(appointment.getUser().getUuid())
                .serviceUuid(appointment.getService().getUuid())
                .dateTime(appointment.getDateTime())
                .build();
    }

    public AppointmentResponseDTO appointmentToAppointmentResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .uuid(appointment.getUuid())
                .datetime(appointment.getDateTime())
                .price(appointment.getService().getPrice())         // Precio calculado del servicio
                .duration(appointment.getService().getDuration())   // Duración del servicio
                .recibo(appointment.getRecibo())
                .userName(appointment.getUser().getName())
                .serviceName(appointment.getService().getName())
                .build();
    }

    public Appointment appointmentRequestDTOtoAppointmentEntity(AppointmentRequestDTO dto) {
        
        User user = userRepository.findByUuid(dto.getUserUuid())
            .orElseThrow(() -> new RuntimeException("User not found"))
        ;

        Servicio service = serviceRepository.findByUuid(dto.getServiceUuid())
            .orElseThrow(() -> new RuntimeException("Service not found"))
        ;
        
        LocalDateTime dateTime = dto.getDateTime();
        return Appointment.builder()
        .user(user)
        .service(service)
        .dateTime(dateTime)
        .date(dateTime.toLocalDate())          
        .hour(dateTime.toLocalTime())
        .build();
    }

}
