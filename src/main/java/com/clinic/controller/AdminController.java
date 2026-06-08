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

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("totalDoctors", doctorService.getAllDoctors().size());
        return "admin-dashboard";
    }

    @GetMapping("/add-doctor")
    public String addDoctorPage() {
        return "add-doctor";
    }

    @PostMapping("/save-doctor")
    public String saveDoctor(@RequestParam String name,
                             @RequestParam String specialization,
                             Model model) {
        try {
            Doctor doctor = new Doctor();
            doctor.setName(name.trim());
            doctor.setSpecialization(specialization.trim());
            doctorService.addDoctor(doctor);
            return "redirect:/admin/doctors?added=true";
        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to add doctor: " + e.getMessage());
            return "add-doctor";
        }
    }

    @GetMapping("/doctors")
    public String viewDoctors(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "view-doctors";
    }

    @GetMapping("/delete-doctor/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "redirect:/admin/doctors?deleted=true";
    }
}
