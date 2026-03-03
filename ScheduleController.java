package com.itp41.lms.controller;

import com.itp41.lms.model.Schedule;
import com.itp41.lms.service.ScheduleService;
import com.itp41.lms.service.SchedulePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired(required = false)
    private SchedulePdfService schedulePdfService;

    // Subject names mapping
    private static final Map<Long, String> SUBJECT_NAMES = Map.of(
            1L, "Combine Mathematics",
            2L, "Chemistry",
            3L, "Physics"
    );

    // Subject IDs mapping (reverse mapping)
    private static final Map<String, Long> SUBJECT_IDS = Map.of(
            "combine-maths", 1L,
            "chemistry", 2L,
            "physics", 3L
    );

    // Helper method to check if user is a teacher
    private boolean isTeacher(HttpSession session) {
        Boolean isTeacher = (Boolean) session.getAttribute("isTeacher");
        return isTeacher != null && isTeacher;
    }

    private Long getTeacherId(HttpSession session) {
        Object teacherId = session.getAttribute("teacherId");
        if (teacherId instanceof Long) {
            return (Long) teacherId;
        } else if (teacherId instanceof Integer) {
            return ((Integer) teacherId).longValue();
        }
        return null;
    }

    // ==================== TEACHER PAGES ====================

    @GetMapping("/admin/{subject}/schedules")
    public String teacherSchedules(@PathVariable String subject, HttpSession session, Model model) {
        if (!isTeacher(session)) {
            return "redirect:/";
        }

        Long teacherId = getTeacherId(session);
        String teacherUsername = (String) session.getAttribute("teacherUsername");

        model.addAttribute("teacherUsername", teacherUsername);
        model.addAttribute("subject", subject);

        if (teacherId != null) {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
            model.addAttribute("schedules", schedules);
        }

        return "admin/" + subject + "/schedules";
    }

    // ==================== API ENDPOINTS ====================

    @GetMapping("/api/teacher/{subject}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeacherSchedules(HttpSession session, @PathVariable String subject) {
        Map<String, Object> response = new HashMap<>();
        
        if (!isTeacher(session)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "Teacher ID not found");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);
            List<Map<String, Object>> scheduleList = new java.util.ArrayList<>();
            
            for (Schedule schedule : schedules) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", schedule.getId());
                map.put("className", schedule.getClassName());
                map.put("educationCenterName", schedule.getEducationCenterName());
                map.put("grade", schedule.getGrade());
                map.put("scheduleDate", schedule.getScheduleDate());
                map.put("scheduleTime", schedule.getScheduleTime());
                map.put("classType", schedule.getClassType());
                map.put("isOnlineClass", schedule.getIsOnlineClass());
                map.put("onlinePlatform", schedule.getOnlinePlatform());
                scheduleList.add(map);
            }

            response.put("success", true);
            response.put("schedules", scheduleList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== PDF DOWNLOAD ENDPOINTS ====================

    @GetMapping("/api/schedules/download-all/{subject}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadAllSchedulesPdf(@PathVariable String subject, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            if (schedulePdfService == null) {
                // If PDF service not available, return error
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            byte[] pdfBytes = schedulePdfService.generateTeacherAllSchedulesPdf(teacherId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "All_Schedules_" + subject + "_Report.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("All Schedules PDF Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/{subject}/schedules/pdf/all")
    public ResponseEntity<byte[]> downloadSchedulesPdfBySubject(@PathVariable String subject, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            if (schedulePdfService == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            byte[] pdfBytes = schedulePdfService.generateTeacherAllSchedulesPdf(teacherId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Schedules_" + subject + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Schedule PDF Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
