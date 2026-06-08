package com.clinic.controller;

import com.clinic.entity.Doctor;
import com.clinic.service.DoctorService;
import com.clinic.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PredictionController {

    @Autowired private PredictionService predictionService;
    @Autowired private DoctorService     doctorService;

    /** Show predict page — symptom checkboxes loaded dynamically from CSV */
    @GetMapping("/predict")
    public String showPredictPage(Model model) {
        model.addAttribute("symptoms", predictionService.getAllSymptoms());
        return "predict";
    }

    /** Handle symptom form submission → run Naive Bayes → show result */
    @PostMapping("/predict")
    public String predict(@RequestParam(required = false) List<String> symptoms,
                          Model model) {

        if (symptoms == null || symptoms.isEmpty()) {
            return "redirect:/predict?noSymptom=true";
        }

        // Run Naive Bayes prediction
        String disease        = predictionService.predictDisease(symptoms);
        String specialization = predictionService.getSpecializationForDisease(disease);

        // Find matching doctors; fall back to all doctors if none found
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        if (doctors.isEmpty()) {
            doctors = doctorService.getAllDoctors();
        }

        model.addAttribute("disease",          disease);
        model.addAttribute("specialization",   specialization);
        model.addAttribute("doctors",          doctors);
        model.addAttribute("selectedSymptoms", symptoms);

        return "result";
    }
}
