package com.general.citas.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.general.citas.DTO.AppointmentRequestDTO;
import com.general.citas.DTO.AppointmentResponseDTO;
import com.general.citas.DTO.AppointmentSearchDTO;
import com.general.citas.DTO.FullCalendarDTO;
import com.general.citas.model.Appointment;
import com.general.citas.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    //Obtener eventos del calendario por rango de fechas
    @GetMapping("/admin/calendar-events")
    public ResponseEntity<List<FullCalendarDTO>> getCalendarEvents(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<FullCalendarDTO> events = appointmentService.getAppointmentsForCalendar(start, end);
        return ResponseEntity.ok(events);
    }

    //obtener citas en el buscador del dashboard por fecha y hora (datetime)
    @GetMapping("/admin/by-datetime")
    public ResponseEntity<List<AppointmentSearchDTO>> getAppointmentsByDatetime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime datetime) {

        List<AppointmentSearchDTO> appointments = appointmentService.getAppointmentsByDatetime(datetime);
        return ResponseEntity.ok(appointments);
    }

    //obtener citas en el buscador del dashboard por numero de recibo
    @GetMapping("/admin/by-receipt/{receiptNumber}")
    public ResponseEntity<?> getAppointmentByReceiptNumber(@PathVariable String receiptNumber) {

        try {
            AppointmentResponseDTO appointment = appointmentService.getAppointmentByReceiptNumber(receiptNumber);
            return ResponseEntity.ok(appointment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //obtener lista de citas paginada (funcionalidad no implementada en el front)
    @GetMapping("/list")
    public ResponseEntity<Page<Appointment>> getAppointments(
            @RequestParam Optional<Long> userId,
            @RequestParam Optional<Long> serviceId,
            @RequestParam Optional<LocalDate> date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datetime,asc") String[] sort) {

         Pageable pageable = PageRequest.of(page, size, Sort.by(parseSortOrder(sort)));
         
         Page<Appointment> appointments = appointmentService.getAppointments(userId, serviceId, date, pageable);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/users/create")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO request) {

        AppointmentResponseDTO response = appointmentService.saveAppointment(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{uuid}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@PathVariable String uuid, @Valid @RequestBody AppointmentRequestDTO request){

        AppointmentResponseDTO response = appointmentService.updateAppointment(uuid, request);
        return ResponseEntity.ok(response);
    }

    //Borrar citas como admin
    @DeleteMapping("/admin/{uuid}")
    public ResponseEntity<AppointmentResponseDTO> deleteAppointment(@PathVariable String uuid) {

        AppointmentResponseDTO response = appointmentService.deleteAppointment(uuid);
        return ResponseEntity.ok(response);
    }

    //Borrar citas como usuario
    @DeleteMapping("/users/cancel/{uuid}")
    public ResponseEntity<?> cancelAppointment(@PathVariable String uuid) {
        
        try {
            appointmentService.cancelAppointment(uuid);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
 
    private List<Sort.Order> parseSortOrder(String[] sort) {
        
        return Arrays.stream(sort)
                .map(order -> {
                    String[] parts = order.split(",");
                    return new Sort.Order(
                            parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                            parts[0]
                    );
                })
                .toList();
    }

}
