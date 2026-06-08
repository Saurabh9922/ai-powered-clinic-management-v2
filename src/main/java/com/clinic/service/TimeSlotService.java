package com.clinic.service;

import com.clinic.entity.Doctor;
import com.clinic.entity.TimeSlot;
import com.clinic.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeSlotService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    /**
     * Generates 30-minute time slots from 09:00 to 17:00 for a doctor.
     * Saved in a single batch to avoid N+1 issues.
     */
    @Transactional
    public void generateSlots(Doctor doctor) {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end   = LocalTime.of(17, 0);

        List<TimeSlot> slots = new ArrayList<>();
        while (start.isBefore(end)) {
            LocalTime next = start.plusMinutes(30);
            TimeSlot slot = new TimeSlot();
            slot.setStartTime(start);
            slot.setEndTime(next);
            slot.setBooked(false);
            slot.setDoctor(doctor);
            slots.add(slot);
            start = next;
        }
        timeSlotRepository.saveAll(slots);
        System.out.println("✅ Generated " + slots.size() + " slots for " + doctor.getName());
    }
}
