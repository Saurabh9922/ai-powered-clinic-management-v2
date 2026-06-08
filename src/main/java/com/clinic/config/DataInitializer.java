package com.clinic.config;

import com.clinic.entity.Doctor;
import com.clinic.entity.User;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.UserRepository;
import com.clinic.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private DoctorService doctorService;

    @Override
    public void run(String... args) {

        // Seed Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            admin.setEmail("admin@clinic.com");

            userRepository.save(admin);
            System.out.println("✅ Admin created → admin / admin123");
        }

// Seed Patient
        if (userRepository.findByUsername("patient").isEmpty()) {
            User patient = new User();
            patient.setUsername("patient");
            patient.setPassword("patient123");
            patient.setRole("PATIENT");
            patient.setEmail("patient@clinic.com");

            userRepository.save(patient);
            System.out.println("✅ Patient created → patient / patient123");
        }
        // Seed Doctors
        if (doctorRepository.count() == 0) {
            seedDoctor("Dr. Sharma",   "General");
            seedDoctor("Dr. Mehta",    "General");
            seedDoctor("Dr. Kapoor",   "Neurologist");
            seedDoctor("Dr. Joshi",    "Endocrinologist");
            seedDoctor("Dr. Rao",      "Cardiologist");
            seedDoctor("Dr. Patel",    "Pulmonologist");
            seedDoctor("Dr. Singh",    "Gastroenterologist");
            System.out.println("✅ Sample doctors seeded with time slots.");
        }
    }

    private void seedDoctor(String name, String specialization) {
        Doctor d = new Doctor();
        d.setName(name);
        d.setSpecialization(specialization);
        doctorService.addDoctor(d);  // uses DoctorService which calls flush + generateSlots
    }
}
