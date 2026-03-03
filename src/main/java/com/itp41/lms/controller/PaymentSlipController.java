package com.itp41.lms.controller;

import com.itp41.lms.model.PaymentSlip;
import com.itp41.lms.model.User;
import com.itp41.lms.repository.UserRepository;
import com.itp41.lms.service.PaymentSlipService;
import com.itp41.lms.service.PaymentSlipPdfService;
import com.itp41.lms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class PaymentSlipController {

    @Autowired
    private PaymentSlipService paymentSlipService;

    @Autowired
    private PaymentSlipPdfService paymentSlipPdfService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Subject ID map
    private static final Map<String, Long> SUBJECT_IDS = Map.of(
            "combine-maths", 1L,
            "chemistry", 2L,
            "physics", 3L
    );

    private static final Map<Long, String> SUBJECT_NAMES = Map.of(
            1L, "Combine Mathematics",
            2L, "Chemistry",
            3L, "Physics"
    );

    // =========================================================
    // STUDENT SIDE
    // =========================================================

    // Student Payments Page
    @GetMapping("/payments")
    public String paymentsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/";
        model.addAttribute("user", user);
        return "client/payments";
    }

    // API: Student uploads a payment slip
    @PostMapping("/api/payments/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadPaymentSlip(
            HttpSession session,
            @RequestParam("subject") String subject,
            @RequestParam("paymentMonth") String paymentMonth,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam("file") MultipartFile file) {

        User user = (User) session.getAttribute("loggedInUser");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("success", false);
            response.put("message", "Please login to upload payment slip.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long teacherId = SUBJECT_IDS.get(subject);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "Invalid subject selected.");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Please select a file to upload.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String subjectName = SUBJECT_NAMES.get(teacherId);
            PaymentSlip slip = paymentSlipService.uploadPaymentSlip(
                    user.getId(), user.getFullName(), user.getUsername(),
                    teacherId, subjectName, paymentMonth, note, file);

            response.put("success", true);
            response.put("message", "Payment slip uploaded successfully! Status: Pending review.");
            response.put("slipId", slip.getId());

            // Send email notification
            emailService.sendPaymentUploadedEmail(
                    user.getEmail(), user.getFullName(), subjectName, paymentMonth);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to save file. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // API: Get logged-in student's payment slips
    @GetMapping("/api/payments/my")
    @ResponseBody
    public ResponseEntity<List<PaymentSlip>> getMyPayments(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<PaymentSlip> slips = paymentSlipService.getSlipsByStudent(user.getId());
        return ResponseEntity.ok(slips);
    }

    // =========================================================
    // ADMIN SIDE - Page Routes
    // =========================================================

    @GetMapping("/admin/combine-maths/payments")
    public String combineMathsPayments(HttpSession session, Model model) {
        if (!isTeacher(session)) return "redirect:/";
        model.addAttribute("teacherUsername", session.getAttribute("teacherUsername"));
        return "admin/combine-maths/payments";
    }

    @GetMapping("/admin/chemistry/payments")
    public String chemistryPayments(HttpSession session, Model model) {
        if (!isTeacher(session)) return "redirect:/";
        model.addAttribute("teacherUsername", session.getAttribute("teacherUsername"));
        return "admin/chemistry/payments";
    }

    @GetMapping("/admin/physics/payments")
    public String physicsPayments(HttpSession session, Model model) {
        if (!isTeacher(session)) return "redirect:/";
        model.addAttribute("teacherUsername", session.getAttribute("teacherUsername"));
        return "admin/physics/payments";
    }

    // =========================================================
    // ADMIN SIDE - APIs
    // =========================================================

    // API: Get payment slips for admin's subject
    @GetMapping("/api/teacher/payments")
    @ResponseBody
    public ResponseEntity<List<PaymentSlip>> getPaymentsForTeacher(HttpSession session) {
        if (!isTeacher(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Long teacherId = getTeacherId(session);
        if (teacherId == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(paymentSlipService.getSlipsBySubject(teacherId));
    }

    // API: Search and filter payment slips
    @GetMapping("/api/teacher/payments/search")
    @ResponseBody
    public ResponseEntity<List<PaymentSlip>> searchPayments(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String month) {
        if (!isTeacher(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Long teacherId = getTeacherId(session);
        if (teacherId == null) return ResponseEntity.badRequest().build();

        List<PaymentSlip> slips = paymentSlipService.getSlipsBySubject(teacherId);

        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            slips = slips.stream()
                    .filter(s -> s.getStudentName().toLowerCase().contains(searchLower) ||
                            s.getStudentUsername().toLowerCase().contains(searchLower) ||
                            (s.getNote() != null && s.getNote().toLowerCase().contains(searchLower)))
                    .toList();
        }

        // Apply status filter
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            slips = slips.stream()
                    .filter(s -> s.getStatus().equals(status.toUpperCase()))
                    .toList();
        }

        // Apply month filter
        if (month != null && !month.isEmpty() && !month.equals("all")) {
            slips = slips.stream()
                    .filter(s -> s.getPaymentMonth().equals(month))
                    .toList();
        }

        return ResponseEntity.ok(slips);
    }

    // API: Approve a payment slip
    @PostMapping("/api/teacher/payments/{id}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approvePayment(
            HttpSession session,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();
        if (!isTeacher(session)) {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long teacherId = getTeacherId(session);
        Optional<PaymentSlip> slipOpt = paymentSlipService.getSlipById(id);
        if (slipOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Payment slip not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!slipOpt.get().getTeacherId().equals(teacherId)) {
            response.put("success", false);
            response.put("message", "Access denied.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        String reviewNote = (body != null) ? body.getOrDefault("reviewNote", "") : "";
        paymentSlipService.approveSlip(id, reviewNote);

        // Send email notification to student
        PaymentSlip slip = slipOpt.get();
        userRepository.findById(slip.getStudentId()).ifPresent(student ->
                emailService.sendPaymentApprovedEmail(
                        student.getEmail(), student.getFullName(),
                        slip.getSubjectName(), slip.getPaymentMonth(), reviewNote));

        response.put("success", true);
        response.put("message", "Payment marked as approved.");
        return ResponseEntity.ok(response);
    }

    // API: Reject a payment slip
    @PostMapping("/api/teacher/payments/{id}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectPayment(
            HttpSession session,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {

        Map<String, Object> response = new HashMap<>();
        if (!isTeacher(session)) {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long teacherId = getTeacherId(session);
        Optional<PaymentSlip> slipOpt = paymentSlipService.getSlipById(id);
        if (slipOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Payment slip not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!slipOpt.get().getTeacherId().equals(teacherId)) {
            response.put("success", false);
            response.put("message", "Access denied.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        String reviewNote = (body != null) ? body.getOrDefault("reviewNote", "") : "";
        paymentSlipService.rejectSlip(id, reviewNote);

        // Send email notification to student
        PaymentSlip slip = slipOpt.get();
        userRepository.findById(slip.getStudentId()).ifPresent(student ->
                emailService.sendPaymentRejectedEmail(
                        student.getEmail(), student.getFullName(),
                        slip.getSubjectName(), slip.getPaymentMonth(), reviewNote));

        response.put("success", true);
        response.put("message", "Payment marked as rejected.");
        return ResponseEntity.ok(response);
    }

    // API: Serve payment slip file
    @GetMapping("/api/payments/file/{storedName:.+}")
    public ResponseEntity<byte[]> serveFile(
            HttpSession session,
            @PathVariable String storedName) {
        // Must be teacher to view
        if (!isTeacher(session)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            byte[] bytes = paymentSlipService.getFileBytes(storedName);
            String contentType = storedName.endsWith(".pdf") ? "application/pdf" : "image/jpeg";
            // Try to detect png
            if (storedName.endsWith(".png")) contentType = "image/png";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + storedName + "\"")
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // =========================================================
    // ADMIN SIDE - PDF DOWNLOAD ENDPOINTS
    // =========================================================

    @GetMapping("/admin/{subject}/payment-slips/pdf/single/{slipId}")
    public ResponseEntity<byte[]> downloadSinglePaymentSlipPdf(@PathVariable String subject, @PathVariable Long slipId, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            byte[] pdfBytes = paymentSlipPdfService.generateSinglePaymentSlipPdf(slipId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "PaymentSlip_" + slipId + "_Details.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Single Payment Slip PDF Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/{subject}/payment-slips/pdf/subject")
    public ResponseEntity<byte[]> downloadSubjectPaymentSlipsPdf(@PathVariable String subject, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            byte[] pdfBytes = paymentSlipPdfService.generateSubjectPaymentSlipsPdf(teacherId, SUBJECT_NAMES.getOrDefault(teacherId, subject));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "PaymentSlips_" + subject + "_Report.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Subject Payment Slips PDF Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/{subject}/payment-slips/pdf/all")
    public ResponseEntity<byte[]> downloadAllPaymentSlipsPdf(@PathVariable String subject, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            byte[] pdfBytes = paymentSlipPdfService.generateTeacherAllPaymentSlipsPdf(teacherId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "All_PaymentSlips_Complete_Report.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("All Payment Slips PDF Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/{subject}/payment-slips/pdf/status/{status}")
    public ResponseEntity<byte[]> downloadPaymentSlipsByStatusPdf(@PathVariable String subject, @PathVariable String status, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            byte[] pdfBytes = paymentSlipPdfService.generatePaymentSlipsByStatusPdf(teacherId, status);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "PaymentSlips_" + status + "_Report.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Status Payment Slips PDF Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/payments/download-all/{subject}")
    public ResponseEntity<byte[]> downloadAllPaymentSlipsPdfApi(@PathVariable String subject, HttpSession session) {
        if (!isTeacher(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long teacherId = getTeacherId(session);
        if (teacherId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            byte[] pdfBytes = paymentSlipPdfService.generateTeacherAllPaymentSlipsPdf(teacherId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "All_Payment_Slips_" + subject + "_Report.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("All Payment Slips PDF Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private boolean isTeacher(HttpSession session) {
        Boolean isTeacher = (Boolean) session.getAttribute("isTeacher");
        return isTeacher != null && isTeacher;
    }

    private Long getTeacherId(HttpSession session) {
        Object teacherId = session.getAttribute("teacherId");
        if (teacherId instanceof Long) return (Long) teacherId;
        if (teacherId instanceof Integer) return ((Integer) teacherId).longValue();
        return null;
    }
}
