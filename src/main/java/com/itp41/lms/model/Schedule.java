package com.itp41.lms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Class name is required")
    @Column(nullable = false)
    private String className;

    @NotBlank(message = "Education center name is required")
    @Column(nullable = false)
    private String educationCenterName;

    @NotBlank(message = "Grade is required")
    @Column(nullable = false)
    private String grade;

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate scheduleDate;

    @NotNull(message = "Time is required")
    @Column(nullable = false)
    private LocalTime scheduleTime;

    @NotBlank(message = "Class type is required")
    @Column(nullable = false)
    private String classType; // "Theory Class", "Paper Class", "Revision Class"

    @Column(nullable = false)
    private Boolean isOnlineClass = false;

    private String onlinePlatform; // "Zoom", "Google Meet", "Microsoft Teams"

    private String meetLink;

    @NotNull(message = "Teacher ID is required")
    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Constructors
    public Schedule() {}

    public Schedule(String className, String educationCenterName, String grade, 
                   LocalDate scheduleDate, LocalTime scheduleTime, String classType,
                   Boolean isOnlineClass, Long teacherId) {
        this.className = className;
        this.educationCenterName = educationCenterName;
        this.grade = grade;
        this.scheduleDate = scheduleDate;
        this.scheduleTime = scheduleTime;
        this.classType = classType;
        this.isOnlineClass = isOnlineClass;
        this.teacherId = teacherId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEducationCenterName() {
        return educationCenterName;
    }

    public void setEducationCenterName(String educationCenterName) {
        this.educationCenterName = educationCenterName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public LocalTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(LocalTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public Boolean getIsOnlineClass() {
        return isOnlineClass;
    }

    public void setIsOnlineClass(Boolean isOnlineClass) {
        this.isOnlineClass = isOnlineClass;
    }

    public String getOnlinePlatform() {
        return onlinePlatform;
    }

    public void setOnlinePlatform(String onlinePlatform) {
        this.onlinePlatform = onlinePlatform;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
