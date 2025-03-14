package com.general.citas.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.general.citas.DTO.ServiceRequestDTO;
import com.general.citas.DTO.ServiceResponseDTO;
import com.general.citas.converter.ServiceConverter;
import com.general.citas.model.Servicio;
import com.general.citas.repository.ServiceRepository;

@Service
public class ServiciosService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceConverter serviceConverter;

    
    //Crear un nuevo servicio.
    public ServiceResponseDTO createService(ServiceRequestDTO serviceDTO) {

        if (serviceRepository.existsByName(serviceDTO.getName())) {
            throw new RuntimeException("Ya existe un servicio con ese nombre");
        }

        Servicio service = serviceConverter.serviceRequestDTOToServiceEntity(serviceDTO);
        service.setUuid(UUID.randomUUID().toString()); // Asegurarse de generar UUID
        Servicio savedService = serviceRepository.save(service);

        return serviceConverter.serviceToServiceResponseDTO(savedService);
    }

  
    //Obtener todos los servicios.
    public List<ServiceResponseDTO> getAllServices() {
        List<Servicio> services = serviceRepository.findAll();
        return services.stream()
                .map(serviceConverter::serviceToServiceResponseDTO)
                .toList();
    }


    //Obtener un servicio por ID.
    public ServiceResponseDTO getServiceById(String uuid) {
        Servicio service = serviceRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return serviceConverter.serviceToServiceResponseDTO(service);
    }


    //Actualizar un servicio existente.
    public ServiceResponseDTO updateService(String uuid, ServiceRequestDTO serviceDTO) {
        Servicio existingService = serviceRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Actualizar los campos permitidos
        existingService.setName(serviceDTO.getName());
        existingService.setDescription(serviceDTO.getDescription());
        existingService.setDuration(serviceDTO.getDuration());
        existingService.setPrice(serviceDTO.getPrice());

        Servicio updatedService = serviceRepository.save(existingService);
        return serviceConverter.serviceToServiceResponseDTO(updatedService);
    }

    //Eliminar un servicio por ID.
    public void deleteService(String uuid) {

        Servicio service = serviceRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Service not found"))
        ;

        if (service.getAppointments() != null && !service.getAppointments().isEmpty()) {
            throw new RuntimeException("No se puede eliminar un servicio con citas asociadas");
        }
        serviceRepository.deleteById(service.getId());
    }
}
