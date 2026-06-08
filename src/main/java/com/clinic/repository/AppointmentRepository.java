package com.clinic.repository;

import com.clinic.entity.Appointment;
import com.clinic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Use patient ID directly to avoid detached entity issues
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId ORDER BY a.date DESC")
    List<Appointment> findByPatientId(@Param("patientId") Long patientId);

    // Keep original for compatibility
    List<Appointment> findByPatient(User patient);
}
