package com.general.citas.DTO;

import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DashboardMetricsDTO {

    private Map<String, Integer> peakHours;                 // {"18:00": 346, "13:00": 255}
    private Map<String, Integer> popularServices;           // {"Service-1": 146, "Service-3": 255}
    private Map<String, Integer> appointmentsPerDay;        // {"Monday": 146, "Tuesday": 255}
    private BigDecimal totalEarningsCurrentMonth;
    private Integer totalAppointmentsCurrentMonth;

}
