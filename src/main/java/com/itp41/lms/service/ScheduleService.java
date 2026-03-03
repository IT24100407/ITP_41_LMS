package com.itp41.lms.service;

import com.itp41.lms.model.Schedule;
import com.itp41.lms.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    // Validate that schedule date and time are not in the past
    private void validateScheduleDateTime(LocalDate scheduleDate, LocalTime scheduleTime) {
        LocalDateTime scheduleDateTime = LocalDateTime.of(scheduleDate, scheduleTime);
        LocalDateTime now = LocalDateTime.now();
        
        if (scheduleDateTime.isBefore(now)) {
            throw new IllegalArgumentException("Cannot schedule for past dates or times. Please select a future date and time.");
        }
    }

    // Create a new schedule
    public Schedule createSchedule(Schedule schedule) {
        validateScheduleDateTime(schedule.getScheduleDate(), schedule.getScheduleTime());
        return scheduleRepository.save(schedule);
    }

    // Get all schedules
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    // Get all active schedules
    public List<Schedule> getAllActiveSchedules() {
        return scheduleRepository.findByIsActiveTrue();
    }

    // Get schedule by ID
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    // Get all schedules for a specific teacher
    public List<Schedule> getSchedulesByTeacherId(Long teacherId) {
        return scheduleRepository.findByTeacherIdAndIsActiveTrue(teacherId);
    }

    // Get schedules by grade
    public List<Schedule> getSchedulesByGrade(String grade) {
        return scheduleRepository.findByGrade(grade);
    }

    // Get schedules by class type
    public List<Schedule> getSchedulesByClassType(String classType) {
        return scheduleRepository.findByClassType(classType);
    }

    // Get schedules by date
    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByScheduleDate(date);
    }

    // Get schedules by teacher and grade
    public List<Schedule> getSchedulesByTeacherAndGrade(Long teacherId, String grade) {
        return scheduleRepository.findByTeacherIdAndGrade(teacherId, grade);
    }

    // Get schedules by teacher and class type
    public List<Schedule> getSchedulesByTeacherAndClassType(Long teacherId, String classType) {
        return scheduleRepository.findByTeacherIdAndClassType(teacherId, classType);
    }

    // Get schedules by teacher and date
    public List<Schedule> getSchedulesByTeacherAndDate(Long teacherId, LocalDate date) {
        return scheduleRepository.findByTeacherIdAndScheduleDate(teacherId, date);
    }

    // Update a schedule
    public Schedule updateSchedule(Long id, Schedule scheduleDetails) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(id);
        if (scheduleOptional.isPresent()) {
            Schedule schedule = scheduleOptional.get();
            
            // Validate the new date/time if they are being updated
            if (scheduleDetails.getScheduleDate() != null && scheduleDetails.getScheduleTime() != null) {
                validateScheduleDateTime(scheduleDetails.getScheduleDate(), scheduleDetails.getScheduleTime());
            }
            
            if (scheduleDetails.getClassName() != null) {
                schedule.setClassName(scheduleDetails.getClassName());
            }
            if (scheduleDetails.getEducationCenterName() != null) {
                schedule.setEducationCenterName(scheduleDetails.getEducationCenterName());
            }
            if (scheduleDetails.getGrade() != null) {
                schedule.setGrade(scheduleDetails.getGrade());
            }
            if (scheduleDetails.getScheduleDate() != null) {
                schedule.setScheduleDate(scheduleDetails.getScheduleDate());
            }
            if (scheduleDetails.getScheduleTime() != null) {
                schedule.setScheduleTime(scheduleDetails.getScheduleTime());
            }
            if (scheduleDetails.getClassType() != null) {
                schedule.setClassType(scheduleDetails.getClassType());
            }
            if (scheduleDetails.getIsOnlineClass() != null) {
                schedule.setIsOnlineClass(scheduleDetails.getIsOnlineClass());
            }
            if (scheduleDetails.getOnlinePlatform() != null) {
                schedule.setOnlinePlatform(scheduleDetails.getOnlinePlatform());
            }
            if (scheduleDetails.getMeetLink() != null) {
                schedule.setMeetLink(scheduleDetails.getMeetLink());
            }
            
            return scheduleRepository.save(schedule);
        }
        return null;
    }

    // Hard delete a schedule (removes from database)
    public boolean deleteSchedule(Long id) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(id);
        if (scheduleOptional.isPresent()) {
            scheduleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Hard delete a schedule (removes from database)
    public void hardDeleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    // Count schedules for a specific teacher
    public long countSchedulesByTeacher(Long teacherId) {
        return getSchedulesByTeacherId(teacherId).size();
    }

    // Get all schedules for students (all active schedules)
    public List<Schedule> getAllSchedulesForStudents() {
        return getAllActiveSchedules();
    }

    // Get schedules by grade for students
    public List<Schedule> getSchedulesByGradeForStudents(String grade) {
        List<Schedule> schedules = getSchedulesByGrade(grade);
        schedules.removeIf(s -> !s.getIsActive());
        return schedules;
    }
}
