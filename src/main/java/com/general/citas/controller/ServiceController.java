package com.general.citas.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.general.citas.DTO.ServiceRequestDTO;
import com.general.citas.DTO.ServiceResponseDTO;
import com.general.citas.service.ServiciosService;

@RestController
@RequestMapping("/admin/services")
public class ServiceController {

    @Autowired
    private ServiciosService serviceService;

    //Crear un nuevo servicio.
    @PostMapping
    public ResponseEntity<ServiceResponseDTO> createService(@RequestBody ServiceRequestDTO serviceDTO) {
        ServiceResponseDTO createdService = serviceService.createService(serviceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    //Obtener todos los servicios.
    @GetMapping("/list")
    public ResponseEntity<List<ServiceResponseDTO>> getAllServices() {
        List<ServiceResponseDTO> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    //Obtener un servicio por ID.
    @GetMapping("/{uuid}")
    public ResponseEntity<ServiceResponseDTO> getServiceById(@PathVariable String uuid) {
        ServiceResponseDTO service = serviceService.getServiceById(uuid);
        return ResponseEntity.ok(service);
    }


 
    //Actualizar un servicio existente.
    @PutMapping("/{uuid}")
    public ResponseEntity<ServiceResponseDTO> updateService(@PathVariable String uuid, @RequestBody ServiceRequestDTO serviceDTO) {
        ServiceResponseDTO updatedService = serviceService.updateService(uuid, serviceDTO);
        return ResponseEntity.ok(updatedService);
    }


    //Eliminar un servicio por ID.
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteService(@PathVariable String uuid) {
        serviceService.deleteService(uuid);
        return ResponseEntity.noContent().build();
    }
}
