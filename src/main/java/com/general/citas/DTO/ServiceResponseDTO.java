package com.general.citas.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponseDTO {

    private String uuid;                 // Identificador del servicio

    @NotBlank(message = "El nombre del servicio no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;             // Nombre del servicio

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String description;      // Descripción breve

    @Positive(message = "La duración debe ser un número positivo")
    @Max(value = 480, message = "La duración no puede ser mayor a 480 minutos (8 horas)")
    private int duration;            // Duración en minutos

    @Positive(message = "El precio debe ser positivo")
    private BigDecimal price;        // Precio del servicio
}
