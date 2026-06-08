package com.clinic.controller;

import com.clinic.entity.Doctor;
import com.clinic.entity.User;
import com.clinic.service.AppointmentService;
import com.clinic.service.DoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class AppointmentController {

    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;

    // Step 1: Choose doctor
    @GetMapping("/book")
    public String selectDoctor(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "select-doctor";
    }

    // Step 2: Show available slots for chosen doctor
    @GetMapping("/slots/{doctorId}")
    public String showSlots(@PathVariable Long doctorId, Model model) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        model.addAttribute("doctor", doctor);
        model.addAttribute("slots", appointmentService.getAvailableSlots(doctor));
        return "select-slot";
    }

    // Step 3: Confirm booking
    @PostMapping("/book-appointment")
    public String bookAppointment(
            @RequestParam Long doctorId,
            @RequestParam(required = false) Long slotId,
            @RequestParam(required = false) String date,
            HttpSession session) {

        if (slotId == null || date == null || date.isBlank()) {
            return "redirect:/slots/" + doctorId + "?error=true";
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Doctor doctor = doctorService.getDoctorById(doctorId);

        try {
            appointmentService.bookAppointment(user, doctor, slotId, LocalDate.parse(date));
            return "redirect:/appointments?success=true";
        } catch (Exception e) {
            // Print FULL stack trace so you can see real error in IntelliJ console
            System.err.println("=== BOOKING ERROR: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/slots/" + doctorId + "?slotTaken=true";
        }
    }

    // View my appointments
    @GetMapping("/appointments")
    public String viewAppointments(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("appointments", appointmentService.getAppointmentsByUser(user));
        return "appointments";
    }
}
