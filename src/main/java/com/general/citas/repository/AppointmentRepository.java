package com.general.citas.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.general.citas.model.Appointment;



@Repository
public interface AppointmentRepository extends JpaRepository<Appointment , Long> {

    Optional<Appointment> findByUuid(String uuid);

    // Ganancias totales mensuales
    @Query("SELECT SUM(a.service.price) FROM Appointment a WHERE a.datetime BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalEarningsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Total de citas mensuales
    Integer countByDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Horas más concurridas (global)
    @Query("SELECT FUNCTION('HOUR', a.datetime) AS hour, COUNT(a.id) AS count FROM Appointment a GROUP BY hour ORDER BY count DESC")
    List<Object[]> calculateMostFrequentHours();

    // Servicios más solicitados (global)
    @Query("SELECT a.service.name AS serviceName, COUNT(a.id) AS count FROM Appointment a GROUP BY a.service.name ORDER BY count DESC")
    List<Object[]> calculateMostRequestedServices();

    // Días de la semana más concurridos (global)
    @Query("SELECT FUNCTION('DAYNAME', a.datetime) AS day, COUNT(a.id) AS count FROM Appointment a GROUP BY day ORDER BY count DESC")
    List<Object[]> calculateBusiestDaysOfWeek();

    // Horas más concurridas en un rango de fechas
    @Query("SELECT FUNCTION('HOUR', a.datetime) AS hour, COUNT(a.id) AS count FROM Appointment a WHERE a.datetime BETWEEN :startDate AND :endDate GROUP BY hour ORDER BY count DESC")
    List<Object[]> calculateMostFrequentHoursBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Servicios más solicitados en un rango de fechas
    @Query("SELECT a.service.name AS serviceName, COUNT(a.id) AS count FROM Appointment a WHERE a.datetime BETWEEN :startDate AND :endDate GROUP BY a.service.name ORDER BY count DESC")
    List<Object[]> calculateMostRequestedServicesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Días de la semana más concurridos en un rango de fechas
    @Query("SELECT FUNCTION('DAYNAME', a.datetime) AS day, COUNT(a.id) AS count FROM Appointment a WHERE a.datetime BETWEEN :startDate AND :endDate GROUP BY day ORDER BY count DESC")
    List<Object[]> calculateBusiestDaysOfWeekBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT a FROM Appointment a 
        WHERE a.service.uuid = :serviceUuid 
        AND a.datetime BETWEEN :startTime AND :endTime
        AND (:excludeAppointmentId IS NULL OR a.id != :excludeAppointmentId)
    """)
    List<Appointment> findConflictingAppointments(@Param("serviceId") String serviceUuid,
                                                   @Param("userUuid") Long userId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("excludeAppointmentId") Long excludeAppointmentId)
    ;

    @Query("""
        SELECT a FROM Appointment a 
        WHERE (:userId IS NULL OR a.user.id = :userId)
        AND (:serviceId IS NULL OR a.service.id = :serviceId)
        AND (:status IS NULL OR a.status = :status)
        AND (:date IS NULL OR DATE(a.datetime) = :date)
    """)
    Page<Appointment> findAppointmentsWithFilters(@Param("userId") Optional<Long> userId,
                                                   @Param("serviceId") Optional<Long> serviceId,
                                                   @Param("date") Optional<LocalDate> date,
                                                   Pageable pageable)
    ;

    @Query("""
        SELECT a FROM Appointment a 
        WHERE a.datetime BETWEEN :startDate AND :endDate
    """)
    List<Appointment> findAppointmentsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate)
    ;

    @Query("""
        SELECT a FROM Appointment a 
        WHERE a.datetime = :datetime
    """)
    List<Appointment> findAppointmentsByDatetime(@Param("datetime") LocalDateTime datetime);        
    
    @Query("SELECT a.recibo FROM Appointment a WHERE a.recibo IS NOT NULL ORDER BY a.id DESC")
    Optional<String> findLastReceiptNumber();

    @Query("SELECT a FROM Appointment a WHERE a.receiptNumber = :receiptNumber")
    Optional<Appointment> findByReceiptNumber(@Param("receiptNumber") String receiptNumber);

    @Transactional
    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.dateTime < :dateTime")
    void deleteByDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
}
