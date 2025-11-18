package com.general.citas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.general.citas.DTO.DashboardMetricsDTO;
import com.general.citas.model.DashboardMetrics;
import com.general.citas.service.DashboardService;

@RestController
@RequestMapping("/admin/metrics")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/all")
    public ResponseEntity<DashboardMetricsDTO> getMetrics() {
        DashboardMetrics metrics = dashboardService.getSingletonMetrics();

        // Convertir los datos de DashboardMetrics a un DTO
        DashboardMetricsDTO metricsDTO = DashboardMetricsDTO.builder()
                .peakHours(dashboardService.parseJson(metrics.getPeakHours()))
                .popularServices(dashboardService.parseJson(metrics.getPopularServices()))
                .appointmentsPerDay(dashboardService.parseJson(metrics.getAppointmentsPerDay()))
                .totalEarningsCurrentMonth(metrics.getCurrentMonthRevenue())
                .totalAppointmentsCurrentMonth(metrics.getCurrentMonthAppointments())
                .build();

        return ResponseEntity.ok(metricsDTO);
    }

}
