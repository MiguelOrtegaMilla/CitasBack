package com.general.citas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.general.citas.model.Servicio;

@Repository
public interface ServiceRepository extends JpaRepository<Servicio , Long> {

    boolean existsByName(String name);

    Optional<Servicio> findByUuid(String uuid); // Buscar por UUID
}

