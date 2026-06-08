package com.clinic.service;

import com.clinic.entity.*;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.TimeSlotRepository;
import com.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private TimeSlotRepository    timeSlotRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private DoctorRepository      doctorRepository;
    @Autowired
    private EmailService emailService;

    public List<TimeSlot> getAvailableSlots(Doctor doctor) {
        return timeSlotRepository.findByDoctorAndBookedFalse(doctor);
    }

    /**
     * Uses patient ID (not the detached entity) — safe with session user.
     */
    public List<Appointment> getAppointmentsByUser(User sessionUser) {
        return appointmentRepository.findByPatientId(sessionUser.getId());
    }

    @Transactional
    public void bookAppointment(User sessionUser, Doctor sessionDoctor, Long slotId, LocalDate date) {

        // ── FIX: Both User and Doctor from outside are DETACHED entities ──────
        // Re-fetch fresh managed copies inside this transaction using their IDs.
        // Without this, Hibernate throws:
        //   "detached entity passed to persist: com.clinic.entity.User"
        // which the controller catches and wrongly shows "slot taken".
        User managedPatient = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found: " + sessionUser.getId()));

        Doctor managedDoctor = doctorRepository.findById(sessionDoctor.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + sessionDoctor.getId()));
        // ─────────────────────────────────────────────────────────────────────

        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));

        if (slot.isBooked()) {
            throw new RuntimeException("Slot already booked!");
        }

        // Build and save appointment using fully managed entities
        Appointment appointment = new Appointment();
        appointment.setPatient(managedPatient);
        appointment.setDoctor(managedDoctor);
        appointment.setDate(date);
        appointment.setTimeSlot(slot);
        appointmentRepository.save(appointment);

        String body =
                "🏥 CLINIC MANAGEMENT SYSTEM\n\n" +

                        "Dear " + managedPatient.getUsername() + ",\n\n" +

                        "Your appointment has been successfully booked.\n\n" +

                        "═══════════════════════════════\n" +
                        "Appointment Details\n" +
                        "═══════════════════════════════\n\n" +

                        "Doctor          : " + managedDoctor.getName() + "\n" +
                        "Specialization  : " + managedDoctor.getSpecialization() + "\n" +
                        "Date            : " + date + "\n" +
                        "Time            : " + slot.getStartTime() +
                        " - " + slot.getEndTime() + "\n\n" +

                        "═══════════════════════════════\n\n" +

                        "Please arrive 10 minutes before your appointment.\n\n" +

                        "Thank you for choosing our services.\n\n" +

                        "Regards,\n" +
                        "Clinic Management Team";

        emailService.sendEmail(
                managedPatient.getEmail(),
                "Appointment Confirmation - Clinic Management System",
                body
        );

        // Mark slot booked AFTER appointment saved (so rollback keeps slot free)
        slot.setBooked(true);
        timeSlotRepository.save(slot);
    }
}
