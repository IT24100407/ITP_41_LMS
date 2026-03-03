package com.itp41.lms.repository;

import com.itp41.lms.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // Find all schedules for a specific teacher
    List<Schedule> findByTeacherId(Long teacherId);
    
    // Find all active schedules for a specific teacher
    List<Schedule> findByTeacherIdAndIsActiveTrue(Long teacherId);
    
    // Find schedules by grade
    List<Schedule> findByGrade(String grade);
    
    // Find schedules by class type
    List<Schedule> findByClassType(String classType);
    
    // Find schedules by date
    List<Schedule> findByScheduleDate(LocalDate scheduleDate);
    
    // Find schedules by teacher and grade
    List<Schedule> findByTeacherIdAndGrade(Long teacherId, String grade);
    
    // Find schedules by teacher and class type
    List<Schedule> findByTeacherIdAndClassType(Long teacherId, String classType);
    
    // Find schedules by teacher and date
    List<Schedule> findByTeacherIdAndScheduleDate(Long teacherId, LocalDate scheduleDate);
    
    // Find all active schedules
    List<Schedule> findByIsActiveTrue();
}
