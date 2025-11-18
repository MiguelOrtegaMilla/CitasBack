package com.general.citas.converter;

import org.springframework.stereotype.Component;

import com.general.citas.DTO.ServiceRequestDTO;
import com.general.citas.DTO.ServiceResponseDTO;
import com.general.citas.model.Servicio;

@Component
public class ServiceConverter {

    //Convierte una entidad Service a un ResponseDTO.
    public ServiceResponseDTO serviceToServiceResponseDTO(Servicio service) {
        return ServiceResponseDTO.builder()
                .uuid(service.getUuid())
                .name(service.getName())
                .description(service.getDescription())
                .duration(service.getDuration())
                .price(service.getPrice())
                .build();
    }

    public Servicio serviceRequestDTOToServiceEntity (ServiceRequestDTO dto){

        return Servicio.builder()
            .uuid(null)
            .name(dto.getName())
            .description(dto.getDescription())
            .duration(dto.getDuration())
            .price(dto.getPrice())
            .build();
    }

    //Convierte un ResponseDTO a una entidad Service.
    public Servicio serviceResponseDTOToServiceEntity(ServiceResponseDTO dto) {
        return Servicio.builder()
                .uuid(dto.getUuid())
                .name(dto.getName())
                .description(dto.getDescription())
                .duration(dto.getDuration())
                .price(dto.getPrice())
                .build();
    }


}
