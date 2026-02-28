package com.itp41.lms.controller;

import com.itp41.lms.model.Schedule;
import com.itp41.lms.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ScheduleService scheduleService;

    // Helper method to check if user is a teacher
    private boolean isTeacher(HttpSession session) {
        Boolean isTeacher = (Boolean) session.getAttribute("isTeacher");
        return isTeacher != null && isTeacher;
    }

    // Helper method to get teacher ID from session
    private Long getTeacherId(HttpSession session) {
        Object teacherId = session.getAttribute("teacherId");
        if (teacherId instanceof Long) {
            return (Long) teacherId;
        } else if (teacherId instanceof Integer) {
            return ((Integer) teacherId).longValue();
        }
        return null;
    }

    // Combine Mathematics Dashboard
    @GetMapping("/combine-maths/dashboard")
    public String combineMathsDashboard(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }
        
        String teacherUsername = (String) session.getAttribute("teacherUsername");
        model.addAttribute("teacherUsername", teacherUsername);
        return "admin/combine-maths/dashboard";
    }

    // Combine Mathematics Schedules
    @GetMapping("/combine-maths/schedules")
    public String combineMathsSchedules(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }
        
        String teacherUsername = (String) session.getAttribute("teacherUsername");
        Long teacherId = getTeacherId(session);
        
        model.addAttribute("teacherUsername", teacherUsername);
        
        if (teacherId != null) {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
            model.addAttribute("schedules", schedules);
        }
        
        return "admin/combine-maths/schedules";
    }

    // Chemistry Dashboard
    @GetMapping("/chemistry/dashboard")
    public String chemistryDashboard(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }
        
        String teacherUsername = (String) session.getAttribute("teacherUsername");
        model.addAttribute("teacherUsername", teacherUsername);
        return "admin/chemistry/dashboard";
    }

    // Chemistry Schedules
    @GetMapping("/chemistry/schedules")
    public String chemistrySchedules(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }
        
        String teacherUsername = (String) session.getAttribute("teacherUsername");
        Long teacherId = getTeacherId(session);
        
        model.addAttribute("teacherUsername", teacherUsername);
        
        if (teacherId != null) {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
            model.addAttribute("schedules", schedules);
        }
        
        return "admin/chemistry/schedules";
    }

    // Physics Dashboard
    @GetMapping("/physics/dashboard")
    public String physicsDashboard(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }
        
        String teacherUsername = (String) session.getAttribute("teacherUsername");
        model.addAttribute("teacherUsername", teacherUsername);
        return "admin/physics/dashboard";
    }

    // Physics Schedules
    @GetMapping("/physics/schedules")
    public String physicsSchedules(HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }
        
        String teacherUsername = (String) session.getAttribute("teacherUsername");
        Long teacherId = getTeacherId(session);
        
        model.addAttribute("teacherUsername", teacherUsername);
        
        if (teacherId != null) {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
            model.addAttribute("schedules", schedules);
        }
        
        return "admin/physics/schedules";
    }

    // API: Get all schedules for a specific teacher (JSON)
    @GetMapping("/api/teacher/schedules")
    @ResponseBody
    public ResponseEntity<List<Schedule>> getTeacherSchedules(HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
        return ResponseEntity.ok(schedules);
    }

    // API: Create a new schedule
    @PostMapping("/api/teacher/schedules/create")
    @ResponseBody
    public ResponseEntity<?> createSchedule(HttpSession session, @RequestBody Schedule schedule) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        try {
            schedule.setTeacherId(teacherId);
            Schedule createdSchedule = scheduleService.createSchedule(schedule);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // API: Update a schedule
    @PutMapping("/api/teacher/schedules/{id}")
    @ResponseBody
    public ResponseEntity<?> updateSchedule(
            HttpSession session,
            @PathVariable Long id,
            @RequestBody Schedule scheduleDetails) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Schedule existingSchedule = scheduleService.getScheduleById(id).orElse(null);
        if (existingSchedule == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Long teacherId = getTeacherId(session);
        if (!existingSchedule.getTeacherId().equals(teacherId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Schedule updatedSchedule = scheduleService.updateSchedule(id, scheduleDetails);
            return ResponseEntity.ok(updatedSchedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // API: Delete a schedule
    @DeleteMapping("/api/teacher/schedules/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteSchedule(HttpSession session, @PathVariable Long id) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Schedule existingSchedule = scheduleService.getScheduleById(id).orElse(null);
        if (existingSchedule == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        Long teacherId = getTeacherId(session);
        if (!existingSchedule.getTeacherId().equals(teacherId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean deleted = scheduleService.deleteSchedule(id);
        if (deleted) {
            return ResponseEntity.ok("Schedule deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // Teacher Logout
    @GetMapping("/logout")
    public String teacherLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
