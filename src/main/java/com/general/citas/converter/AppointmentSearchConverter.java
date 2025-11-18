package com.general.citas.converter;

import com.general.citas.DTO.AppointmentSearchDTO;
import com.general.citas.model.Appointment;

public class AppointmentSearchConverter {

    public AppointmentSearchDTO AppointmentToAppointmentSearchDTO(Appointment appointment) {
        return AppointmentSearchDTO.builder()
                .uuid(appointment.getUuid())
                .date(appointment.getDateTime().toLocalDate())
                .time(appointment.getDateTime().toLocalTime())
                .serviceName(appointment.getService().getName())
                .userName(appointment.getUser().getName())
                .build();
    }
}
