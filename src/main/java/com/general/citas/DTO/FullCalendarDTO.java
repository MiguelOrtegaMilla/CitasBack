package com.general.citas.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FullCalendarDTO {

    private String id;       // UUID del evento (cita) que fullcalendar interpreta como id
    private String title;    // Título del evento
    private LocalDateTime start; // Fecha y hora de inicio
    private LocalDateTime end;   // Fecha y hora de fin
}
