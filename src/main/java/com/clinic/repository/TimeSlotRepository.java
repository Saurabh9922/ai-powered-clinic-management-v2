package com.clinic.repository;

import com.clinic.entity.Doctor;
import com.clinic.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByDoctor(Doctor doctor);

    // Explicit JPQL query to avoid any Spring naming ambiguity with boolean field "booked"
    @Query("SELECT t FROM TimeSlot t WHERE t.doctor = :doctor AND t.booked = false")
    List<TimeSlot> findByDoctorAndBookedFalse(@Param("doctor") Doctor doctor);
}
