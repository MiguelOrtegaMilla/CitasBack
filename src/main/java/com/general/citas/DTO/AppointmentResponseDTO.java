package com.general.citas.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Este AppointmentDTO será enviado desde el backend al cliente y contiene toda la información relevante, incluyendo duración y precio.

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentResponseDTO {
 
    private String uuid;             // ID de la cita
    private String userName;         // Nombre del usuario
    private String serviceName;      // Nombre del servicio
    private LocalDateTime datetime;  // Fecha y hora de la cita
    private BigDecimal price;        // Precio del servicio
    private Integer duration;        // Duración del servicio
    private String recibo;           // Número del recibo
}
