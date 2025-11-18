package com.general.citas.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.general.citas.model.DashboardMetrics;
import com.general.citas.repository.AppointmentRepository;
import com.general.citas.repository.DashboardRepository;



@Service
public class DashboardService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Transactional
    public void updateMonthlyMetrics() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        BigDecimal totalEarnings = appointmentRepository.calculateTotalEarningsBetween(startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59));
        Integer totalAppointments = appointmentRepository.countByDatetimeBetween(startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59));

        DashboardMetrics metrics = dashboardRepository.findFirstByDate(LocalDate.now()).orElse(new DashboardMetrics());
        metrics.setCurrentMonthRevenue(totalEarnings);
        metrics.setCurrentMonthAppointments(totalAppointments);

        dashboardRepository.save(metrics);
    }

    @Transactional
    public void updateGlobalMetrics() {

        try{
            Map<String, Long> mostFrequentHours = convertQueryResultToMap(appointmentRepository.calculateMostFrequentHours());
            Map<String, Long> mostRequestedServices = convertQueryResultToMap(appointmentRepository.calculateMostRequestedServices());
            Map<String, Long> busiestDaysOfWeek = convertQueryResultToMap(appointmentRepository.calculateBusiestDaysOfWeek());

            DashboardMetrics metrics = dashboardRepository.findFirstByDate(LocalDate.now()).orElse(new DashboardMetrics());
            metrics.setPeakHours(new ObjectMapper().writeValueAsString(mostFrequentHours));
            metrics.setPopularServices(new ObjectMapper().writeValueAsString(mostRequestedServices));
            metrics.setAppointmentsPerDay(new ObjectMapper().writeValueAsString(busiestDaysOfWeek));

            dashboardRepository.save(metrics);

         }catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing metrics data to JSON", e);
         }
    }

    public Map<String, Object> calculateMetricsBetweenDates(LocalDate startDate, LocalDate endDate) {
        Map<String, Long> mostFrequentHours = convertQueryResultToMap(appointmentRepository.calculateMostFrequentHoursBetweenDates(startDate.atStartOfDay(), endDate.atTime(23, 59)));
        Map<String, Long> mostRequestedServices = convertQueryResultToMap(appointmentRepository.calculateMostRequestedServicesBetweenDates(startDate.atStartOfDay(), endDate.atTime(23, 59)));
        Map<String, Long> busiestDaysOfWeek = convertQueryResultToMap(appointmentRepository.calculateBusiestDaysOfWeekBetweenDates(startDate.atStartOfDay(), endDate.atTime(23, 59)));

        Map<String, Object> result = new HashMap<>();
        result.put("mostFrequentHours", mostFrequentHours);
        result.put("mostRequestedServices", mostRequestedServices);
        result.put("busiestDaysOfWeek", busiestDaysOfWeek);

        return result;
    }

    private Map<String, Long> convertQueryResultToMap(List<Object[]> queryResult) {
        return queryResult.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    @Transactional
    public void updatePeakHours(LocalDateTime appointmentTime) throws JsonProcessingException {
        // Obtener métricas existentes
        DashboardMetrics metrics = dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));

        // Inicializar ObjectMapper para trabajar con JSON
        ObjectMapper objectMapper = new ObjectMapper();

        // Leer el JSON actual
        String json = metrics.getPeakHours();
        Map<String, Integer> frequentHours;
        if (json == null || json.isEmpty()) {
            frequentHours = new HashMap<>(); // Inicializar mapa vacío si no hay datos
        } else {
            frequentHours = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        }

        // Obtener la hora de la cita en formato HH:00
        String hour = String.format("%02d:00", appointmentTime.getHour());

        // Actualizar el contador de la hora correspondiente
        frequentHours.put(hour, frequentHours.getOrDefault(hour, 0) + 1);

        // Convertir el mapa actualizado de vuelta a JSON
        String updatedJson = objectMapper.writeValueAsString(frequentHours);

        // Guardar el JSON actualizado en la base de datos
        metrics.setPeakHours(updatedJson);
        dashboardRepository.save(metrics);
    }

    @Transactional
    public void decrementPeakHours(LocalDateTime appointmentTime) throws JsonProcessingException {
        // Obtener métricas existentes
        DashboardMetrics metrics = dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));

        // Leer el JSON actual
        ObjectMapper objectMapper = new ObjectMapper();
        String json = metrics.getPeakHours();
        Map<String, Integer> frequentHours = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});

         // Obtener la hora de la cita en formato HH:00
        String hour = String.format("%02d:00", appointmentTime.getHour());

        // Decrementar el contador, o eliminar si llega a 0
        frequentHours.computeIfPresent(hour, (k, v) -> v > 1 ? v - 1 : null);

        // Convertir de vuelta a JSON y guardar
        String updatedJson = objectMapper.writeValueAsString(frequentHours);
        metrics.setPopularServices(updatedJson);
        dashboardRepository.save(metrics);    
    }  

    @Transactional
    public void updatePopularServices(String serviceName) throws JsonProcessingException {
        // Obtener métricas existentes
        DashboardMetrics metrics = dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));

        // Leer el JSON actual
        ObjectMapper objectMapper = new ObjectMapper();
        String json = metrics.getPopularServices();
        Map<String, Integer> requestedServices;
        if (json == null || json.isEmpty()) {
            requestedServices = new HashMap<>(); // Inicializar mapa vacío si no hay datos
        } else {
            requestedServices = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        }

        // Actualizar el contador del servicio
        requestedServices.put(serviceName, requestedServices.getOrDefault(serviceName, 0) + 1);

        // Convertir de vuelta a JSON y guardar
        String updatedJson = objectMapper.writeValueAsString(requestedServices);
        metrics.setPopularServices(updatedJson);
        dashboardRepository.save(metrics);
    }

    @Transactional
    public void decrementPopularServices(String serviceName) throws JsonProcessingException {
        // Obtener métricas existentes
        DashboardMetrics metrics = dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));

        // Leer el JSON actual
        ObjectMapper objectMapper = new ObjectMapper();
        String json = metrics.getPopularServices();
        Map<String, Integer> requestedServices = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});

        // Decrementar el contador, o eliminar si llega a 0
        requestedServices.computeIfPresent(serviceName, (k, v) -> v > 1 ? v - 1 : null);

        // Convertir de vuelta a JSON y guardar
        String updatedJson = objectMapper.writeValueAsString(requestedServices);
        metrics.setPopularServices(updatedJson);
        dashboardRepository.save(metrics);
        
    }   

    @Transactional
    public void updateAppointmentsPerDay(LocalDate appointmentDate) throws JsonProcessingException {
        // Obtener métricas existentes
        DashboardMetrics metrics = dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));

        // Leer el JSON actual
        ObjectMapper objectMapper = new ObjectMapper();
        String json = metrics.getAppointmentsPerDay();
        Map<String, Integer> busiestDays;
        if (json == null || json.isEmpty()) {
            busiestDays = new HashMap<>(); // Inicializar mapa vacío si no hay datos
        } else {
            busiestDays = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        }

        // Obtener el nombre del día de la semana (e.g., "Monday")
        String dayOfWeek = appointmentDate.getDayOfWeek().toString();

        // Actualizar el contador del día correspondiente
        busiestDays.put(dayOfWeek, busiestDays.getOrDefault(dayOfWeek, 0) + 1);

        // Convertir de vuelta a JSON y guardar
        String updatedJson = objectMapper.writeValueAsString(busiestDays);
        metrics.setAppointmentsPerDay(updatedJson);
        dashboardRepository.save(metrics);
    }

    @Transactional
    public void decrementAppointmentsPerDay(LocalDate appointmentDate) throws JsonProcessingException {
        // Obtener métricas existentes
        DashboardMetrics metrics = dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));

        // Leer el JSON actual
        ObjectMapper objectMapper = new ObjectMapper();
        String json = metrics.getAppointmentsPerDay();
        Map<String, Integer> busiestDays = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});

        // Obtener el nombre del día de la semana (e.g., "Monday")
        String dayOfWeek = appointmentDate.getDayOfWeek().toString();

        // Decrementar el contador, o eliminar si llega a 0
        busiestDays.computeIfPresent(dayOfWeek, (k, v) -> v > 1 ? v - 1 : null);

        // Convertir de vuelta a JSON y guardar
        String updatedJson = objectMapper.writeValueAsString(busiestDays);
        metrics.setAppointmentsPerDay(updatedJson);
        dashboardRepository.save(metrics);
    }

    public DashboardMetrics getSingletonMetrics() {
        return dashboardRepository.findSingletonMetrics()
                .orElseThrow(() -> new RuntimeException("Metrics not initialized"));
    }

    public Map<String, Integer> parseJson(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new HashMap<>();
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON: " + e.getMessage());
        }
    }

}
