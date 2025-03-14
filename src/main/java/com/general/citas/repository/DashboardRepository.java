package com.general.citas.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.general.citas.model.DashboardMetrics;

@Repository
public interface DashboardRepository extends JpaRepository <DashboardMetrics , Long> {

    Optional<DashboardMetrics> findFirstByDate(LocalDate date);

    @Query("SELECT d FROM DashboardMetrics d WHERE d.id = 1")
    Optional<DashboardMetrics> findSingletonMetrics();
}
