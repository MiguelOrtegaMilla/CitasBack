package com.general.citas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "dashboard_metrics")
public class DashboardMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingresos_mensuales", nullable = false)
    private BigDecimal currentMonthRevenue;

    @Column(name = "citas_mensuales", nullable = false)
    private Integer currentMonthAppointments;

    @Lob
    private String peakHours; // JSON: {"18:00": 346, "13:00": 255}

    @Lob
    private String popularServices; // JSON: {"Service-1": 146, "Service-3": 255}

    @Lob
    private String appointmentsPerDay; // JSON: {"Monday": 146, "Tuesday": 255}

    @Column(name = "actualizado_el", nullable = false)
    private LocalDateTime updatedAt;

}
