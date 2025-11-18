package com.general.citas.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor         
@NoArgsConstructor          
@Data                      	      
@Builder
public class AppointmentSearchDTO {
    
    private String uuid;            // ID de la cita
    private LocalDate date;         // Fecha de la cita
    private LocalTime time;         // Hora de la cita
    private String serviceName;     // Nombre del servicio solicitado
    private String userName;        // Nombre del usuario que solicita la cita
}
