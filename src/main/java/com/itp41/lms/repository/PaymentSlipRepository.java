package com.itp41.lms.repository;

import com.itp41.lms.model.PaymentSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSlipRepository extends JpaRepository<PaymentSlip, Long> {

    // Find all slips for a specific student
    List<PaymentSlip> findByStudentIdOrderByUploadedAtDesc(Long studentId);

    // Find all slips for a specific subject (admin view)
    List<PaymentSlip> findByTeacherIdOrderByUploadedAtDesc(Long teacherId);

    // Find slips by status for a subject
    List<PaymentSlip> findByTeacherIdAndStatusOrderByUploadedAtDesc(Long teacherId, String status);

    // Find slips by student and subject
    List<PaymentSlip> findByStudentIdAndTeacherId(Long studentId, Long teacherId);

    // Check if a student has an approved payment for a specific subject and month
    List<PaymentSlip> findByStudentIdAndTeacherIdAndPaymentMonthAndStatus(
            Long studentId, Long teacherId, String paymentMonth, String status);
}
