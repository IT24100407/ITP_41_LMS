package com.itp41.lms.service;

import com.itp41.lms.model.PaymentSlip;
import com.itp41.lms.repository.PaymentSlipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentSlipService {

    @Autowired
    private PaymentSlipRepository paymentSlipRepository;

    private static final String UPLOAD_DIR = "uploads/payment-slips/";

    public PaymentSlip uploadPaymentSlip(
            Long studentId, String studentName, String studentUsername,
            Long teacherId, String subjectName,
            String paymentMonth, String note,
            MultipartFile file) throws IOException {

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Only JPG, PNG, or PDF files are allowed.");
        }

        // Ensure upload directory exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String extension = getExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save entity
        PaymentSlip slip = new PaymentSlip();
        slip.setStudentId(studentId);
        slip.setStudentName(studentName);
        slip.setStudentUsername(studentUsername);
        slip.setTeacherId(teacherId);
        slip.setSubjectName(subjectName);
        slip.setPaymentMonth(paymentMonth);
        slip.setNote(note);
        slip.setFileName(file.getOriginalFilename());
        slip.setFilePath(storedName);
        slip.setFileType(contentType);
        slip.setStatus("PENDING");

        return paymentSlipRepository.save(slip);
    }

    public List<PaymentSlip> getSlipsByStudent(Long studentId) {
        return paymentSlipRepository.findByStudentIdOrderByUploadedAtDesc(studentId);
    }

    public List<PaymentSlip> getSlipsBySubject(Long teacherId) {
        return paymentSlipRepository.findByTeacherIdOrderByUploadedAtDesc(teacherId);
    }

    public Optional<PaymentSlip> getSlipById(Long id) {
        return paymentSlipRepository.findById(id);
    }

    public PaymentSlip approveSlip(Long id, String reviewNote) {
        PaymentSlip slip = paymentSlipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment slip not found"));
        slip.setStatus("APPROVED");
        slip.setReviewNote(reviewNote);
        slip.setReviewedAt(LocalDateTime.now());
        return paymentSlipRepository.save(slip);
    }

    public PaymentSlip rejectSlip(Long id, String reviewNote) {
        PaymentSlip slip = paymentSlipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment slip not found"));
        slip.setStatus("REJECTED");
        slip.setReviewNote(reviewNote);
        slip.setReviewedAt(LocalDateTime.now());
        return paymentSlipRepository.save(slip);
    }

    // Get the stored file as bytes for serving
    public byte[] getFileBytes(String storedName) throws IOException {
        Path path = Paths.get(UPLOAD_DIR).resolve(storedName);
        return Files.readAllBytes(path);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
