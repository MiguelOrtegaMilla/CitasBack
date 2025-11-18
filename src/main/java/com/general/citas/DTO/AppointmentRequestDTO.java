package com.general.citas.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//Este AppointmentDTO se usará para crear o actualizar citas. Solo incluye los datos que el cliente debe enviar

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AppointmentRequestDTO {

    private String userUuid;             // ID del usuario asociado
    private String serviceUuid;          // ID del servicio solicitado
    private LocalDateTime dateTime;  // Fecha y hora de la cita
}
