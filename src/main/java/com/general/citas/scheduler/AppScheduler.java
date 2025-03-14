package com.general.citas.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.general.citas.service.AppointmentService;

@Component
public class AppScheduler {

    @Autowired
    private AppointmentService appointmentService;

    @Scheduled(cron = "0 0 3 * * ?") // Todos los días a las 3:00 AM
    public void cleanupOldAppointments() {
        appointmentService.deleteOldAppointments();
    }
}
