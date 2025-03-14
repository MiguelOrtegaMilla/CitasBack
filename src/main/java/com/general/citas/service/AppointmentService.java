package com.general.citas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.general.citas.DTO.AppointmentRequestDTO;
import com.general.citas.DTO.AppointmentResponseDTO;
import com.general.citas.DTO.AppointmentSearchDTO;
import com.general.citas.DTO.FullCalendarDTO;
import com.general.citas.converter.AppointmentConverter;
import com.general.citas.converter.AppointmentSearchConverter;
import com.general.citas.model.Appointment;
import com.general.citas.model.Servicio;
import com.general.citas.model.User;
import com.general.citas.repository.AppointmentRepository;
import com.general.citas.repository.ServiceRepository;
import com.general.citas.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private DashboardService dashboardService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppointmentSearchConverter appointmentSearchConverter;

    @Autowired
    private AppointmentConverter appointmentConverter;


    //obtener citas en el buscador del dashboard
    public List<AppointmentSearchDTO> getAppointmentsByDatetime(LocalDateTime datetime) {
        List<Appointment> appointments = appointmentRepository.findAppointmentsByDatetime(datetime);

        // Convertir citas a DTOs
        return appointments.stream()
                .map(appointmentSearchConverter::AppointmentToAppointmentSearchDTO)
                .toList();
    }

    //obtener citas por numero de recibo
    public AppointmentResponseDTO getAppointmentByReceiptNumber(String receiptNumber) {

        if (!receiptNumber.matches("^REC-\\d{4}$")) {
            throw new RuntimeException("Invalid receipt number format. Expected format: REC-XXXX");
        }

        Appointment appointment = appointmentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Appointment with receipt number " + receiptNumber + " not found"))
        ;

        return appointmentConverter.appointmentToAppointmentResponseDTO(appointment);
    }

    //obtener lista de citas paginada(no imp)
    @Transactional(readOnly = true)
    public Page<Appointment> getAppointments(Optional<Long> userId, Optional<Long> serviceId, Optional<LocalDate> date , Pageable pageable) {
    
        // Recuperar citas basadas en los filtros proporcionados
        return appointmentRepository.findAppointmentsWithFilters(userId, serviceId, date, pageable);
    }

    @Transactional
    public AppointmentResponseDTO saveAppointment(AppointmentRequestDTO request) {

        Appointment appointment = appointmentConverter.appointmentRequestDTOtoAppointmentEntity(request);

        appointment.setUuid(UUID.randomUUID().toString());

        // Comprobar disponibilidad
        boolean isAvailable = isTimeSlotAvailable(
        appointment.getService().getUuid(),
        appointment.getUser().getId(),
        appointment.getDateTime(),
        null); // No hay cita que excluir.

        if (!isAvailable) {
            throw new RuntimeException("The selected time slot is already occupied");
        }

        // Generar el número de recibo
        String receiptNumber = generateReceiptNumber();
        appointment.setRecibo(receiptNumber);

        // Guardar la cita
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Actualizar métricas dinámicamente
        try {
            dashboardService.updatePopularServices(appointment.getService().getName());
            dashboardService.updateAppointmentsPerDay(appointment.getDateTime().toLocalDate());
            dashboardService.updatePeakHours(appointment.getDateTime());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error updating metrics: " + e.getMessage());
        }

        // Enviar correo de confirmación
        String mailStr = appointment.getUser().getEmail();
        AppointmentResponseDTO appointmentDTO = appointmentConverter.appointmentToAppointmentResponseDTO(savedAppointment);

        emailService.sendAppointmentNotification(
        mailStr,
        "Confirmación de Cita",
        appointmentDTO,
        "create"
        );

        return appointmentDTO;
    }

    //Borrar citas como admin
    @Transactional
    public AppointmentResponseDTO deleteAppointment(String appointmentUuid) {

        Appointment appointment = appointmentRepository.findByUuid(appointmentUuid)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointmentRepository.delete(appointment);

        AppointmentResponseDTO responseDTO = appointmentConverter.appointmentToAppointmentResponseDTO(appointment);

        // Actualizar métricas dinámicamente
        try {
            dashboardService.decrementPopularServices(appointment.getService().getName());
            dashboardService.decrementAppointmentsPerDay(appointment.getDateTime().toLocalDate());
            dashboardService.decrementPeakHours(appointment.getDateTime());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error updating metrics: " + e.getMessage());
        }

        return responseDTO;
    }

    //Borrar citas como usuario
    @Transactional
    public void cancelAppointment(String appointmentUuid) {

        Appointment appointment = appointmentRepository.findByUuid(appointmentUuid)
                .orElseThrow(() -> new RuntimeException("Appointment not found"))
        ;

        User currentUser = getCurrentUser();
        
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only create appointments for yourself");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancelLimit = appointment.getDateTime().minusDays(3);

        if (now.isAfter(cancelLimit)) {
            throw new RuntimeException("Appointment cannot be canceled within 3 days of the scheduled date");
        }

        // Actualizar métricas dinámicamente
        try {
            dashboardService.decrementPopularServices(appointment.getService().getName());
            dashboardService.decrementAppointmentsPerDay(appointment.getDateTime().toLocalDate());
            dashboardService.decrementPeakHours(appointment.getDateTime());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error updating metrics: " + e.getMessage());
        }

        // Enviar correo de confirmación
        String mailStr = appointment.getUser().getEmail();
        AppointmentResponseDTO responseDTO = appointmentConverter.appointmentToAppointmentResponseDTO(appointment);

        emailService.sendAppointmentNotification(
        mailStr,
        "Cancelacion de Cita",
        responseDTO,
        "cancel"
        );

        appointmentRepository.deleteById(appointment.getId());
    }

    @Transactional
    public AppointmentResponseDTO updateAppointment(String appointmentUuid, AppointmentRequestDTO request) {
        // Buscar la cita existente
        Appointment existingAppointment = appointmentRepository.findByUuid(appointmentUuid)
                .orElseThrow(() -> new RuntimeException("Appointment not found"))
        ;
   
        // 2. Verificar la existencia del servicio
        Servicio service = serviceRepository.findByUuid(request.getServiceUuid())
            .orElseThrow(() -> new RuntimeException("The specified service does not exist"))
        ;

        // 3. Verificar la existencia del usuario
        User user = userRepository.findByUuid(request.getUserUuid())
            .orElseThrow(() -> new RuntimeException("The specified user does not exist"))
        ;

        Long id = user.getId();


        // Comprobar disponibilidad
        boolean isAvailable = isTimeSlotAvailable(
            request.getServiceUuid(),
            id,
            request.getDateTime(),
            existingAppointment.getId())
        ;

        if (!isAvailable) {
            throw new RuntimeException("The selected time slot is already occupied");
        }

        // Verificar si hay cambios en los datos críticos
        boolean datetimeChanged = !existingAppointment.getDateTime().equals(request.getDateTime());
        boolean serviceChanged = !existingAppointment.getService().getUuid().equals(request.getServiceUuid());

        // Actualizar los datos de la cita
        LocalDateTime dateTime = request.getDateTime();
        existingAppointment.setDateTime(dateTime);
        existingAppointment.setDate(dateTime.toLocalDate());
        existingAppointment.setHour(dateTime.toLocalTime());
        existingAppointment.setService(service);
        existingAppointment.setUser(user);

        // Guardar la cita actualizada
        Appointment savedAppointment = appointmentRepository.save(existingAppointment);

        // Actualizar métricas si es necesario
        try {
            if (datetimeChanged) {
                // Decrementar métricas antiguas
                dashboardService.decrementPeakHours(existingAppointment.getDateTime());
                dashboardService.decrementAppointmentsPerDay(existingAppointment.getDateTime().toLocalDate());

                // Incrementar métricas nuevas
                dashboardService.updatePeakHours(request.getDateTime());
                dashboardService.updateAppointmentsPerDay(request.getDateTime().toLocalDate());
            }

            if (serviceChanged) {
                // Decrementar métricas del servicio antiguo
                dashboardService.decrementPopularServices(existingAppointment.getService().getName());

                // Incrementar métricas del nuevo servicio
                dashboardService.updatePopularServices(service.getName());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error updating metrics: " + e.getMessage());
        }

        // Enviar correo de confirmación
        String mailStr = savedAppointment.getUser().getEmail();
        AppointmentResponseDTO responseDTO = appointmentConverter.appointmentToAppointmentResponseDTO(savedAppointment);

        emailService.sendAppointmentNotification(
        mailStr,
        "Cancelacion de Cita",
        responseDTO,
        "cancel"
        );
        return responseDTO;
    }
 
    public List<FullCalendarDTO> getAppointmentsForCalendar(LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> appointments = appointmentRepository.findAppointmentsBetweenDates(startDate, endDate);

        // Convertir citas a FullCalendarEventDTO
        return appointments.stream()
                .map(this::convertToFullCalendarEvent)
                .toList();
    }

    //Borrar citas antiguas automaticamente
    @Transactional
    public void deleteOldAppointments() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        appointmentRepository.deleteByDateTimeBefore(sixMonthsAgo);
    }

    /**
     * Comprueba si un horario está disponible para un servicio y usuario específicos.
     * 
     * @param serviceId ID del servicio
     * @param userId    ID del usuario
     * @param datetime  Fecha y hora de la cita
     * @param excludeAppointmentId ID de una cita existente para excluirla de la validación (opcional)
     * @return true si el horario está disponible, false en caso contrario
    */
    public boolean isTimeSlotAvailable(String serviceUuid, Long userId, LocalDateTime datetime, Long excludeAppointmentId) {
        
        Integer serviceDuration = getServiceDuration(serviceUuid);

        // Tiempo de tolerancia (puede ser configurable)
        int toleranceMinutes = 0;

        // Calcular rango de tiempo de la cita
        LocalDateTime startTime = datetime.minusMinutes(toleranceMinutes);
        LocalDateTime endTime = datetime.plusMinutes(serviceDuration + toleranceMinutes);

        // Consultar citas que se solapen con el rango
        List<Appointment> conflictingAppointments = appointmentRepository.findConflictingAppointments(
                serviceUuid, userId, startTime, endTime, excludeAppointmentId);

        return conflictingAppointments.isEmpty();
    }

    private Integer getServiceDuration(String serviceUuid) {
        // Implementar lógica para obtener la duración del servicio desde el repositorio de servicios
        return serviceRepository.findByUuid(serviceUuid)
                .orElseThrow(() -> new RuntimeException("Service not found"))
                .getDuration();
    }

    public String generateReceiptNumber() {
        // Obtener el último número de recibo
        Optional<String> lastReceipt = appointmentRepository.findLastReceiptNumber();

        if (lastReceipt.isEmpty()) {
            return "REC-0001"; // Primer número de recibo
        }

        // Extraer el número y generar el siguiente
        String lastNumber = lastReceipt.get().replace("REC-", "");
        int nextNumber = Integer.parseInt(lastNumber) + 1;
        return String.format("REC-%04d", nextNumber);
    }

    private FullCalendarDTO convertToFullCalendarEvent(Appointment appointment) {
        LocalDateTime endDateTime = appointment.getDateTime().plusMinutes(appointment.getService().getDuration());
        return FullCalendarDTO.builder()
                .id(appointment.getUuid())      //UUID de la cita que FC interpreta como id
                .title(appointment.getService().getName())
                .start(appointment.getDateTime())
                .end(endDateTime)
                .build();
    }

    public void validateReceiptNumberFormat(String receiptNumber) {
        if (!receiptNumber.matches("^REC-\\d{4}$")) {
            throw new RuntimeException("Invalid receipt number format. Expected format: REC-XXXX");
        }
    }

     private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        return userRepository.findByName(currentUserName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
