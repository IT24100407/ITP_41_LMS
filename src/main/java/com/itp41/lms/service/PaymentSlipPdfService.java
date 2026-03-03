package com.itp41.lms.service;

import com.itp41.lms.model.PaymentSlip;
import com.itp41.lms.util.PdfGeneratorUtil;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PaymentSlipPdfService {

    @Autowired
    private PaymentSlipService paymentSlipService;

    /**
     * Generate PDF for a single payment slip
     */
    public byte[] generateSinglePaymentSlipPdf(Long slipId) {
        try {
            PaymentSlip slip = paymentSlipService.getSlipById(slipId)
                    .orElseThrow(() -> new RuntimeException("Payment slip not found"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // Header
            PdfGeneratorUtil.addReportHeader(document, "Payment Slip Details",
                    "Student: " + slip.getStudentName() + " | Subject: " + slip.getSubjectName());

            // Status banner
            DeviceRgb statusColor = getStatusColor(slip.getStatus());
            PdfGeneratorUtil.addStatusBanner(document, "Status: " + slip.getStatus(), statusColor);

            // Payment slip details card
            addPaymentSlipDetailsCard(document, slip);

            // Notes section (if applicable)
            if (slip.getNote() != null && !slip.getNote().isEmpty()) {
                addNotesSection(document, slip);
            }

            // Footer
            PdfGeneratorUtil.addReportFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Payment Slip PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF for all payment slips in a subject
     */
    public byte[] generateSubjectPaymentSlipsPdf(Long teacherId, String subjectName) {
        try {
            List<PaymentSlip> slips = paymentSlipService.getSlipsBySubject(teacherId).stream()
                    .filter(s -> s.getSubjectName().equals(subjectName))
                    .toList();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);

            // Header
            PdfGeneratorUtil.addReportHeader(document, "Payment Slips Report",
                    "Subject: " + subjectName + " | Total Slips: " + slips.size());

            // Status banner
            PdfGeneratorUtil.addStatusBanner(document, "Payment slips for " + subjectName,
                    PdfGeneratorUtil.INFO_COLOR);

            // Payment slips table
            addPaymentSlipsTable(document, slips);

            // Summary section
            addSummarySection(document, slips);

            // Footer
            PdfGeneratorUtil.addReportFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Subject Payment Slips PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF for all payment slips by a teacher
     */
    public byte[] generateTeacherAllPaymentSlipsPdf(Long teacherId) {
        try {
            List<PaymentSlip> slips = paymentSlipService.getSlipsBySubject(teacherId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);

            // Header
            PdfGeneratorUtil.addReportHeader(document, "Complete Payment Slips Report",
                    "All payment slips | Total: " + slips.size());

            // Status banner
            PdfGeneratorUtil.addStatusBanner(document, "Complete list of all payment slips",
                    PdfGeneratorUtil.PRIMARY_COLOR);

            // Group by subject
            slips.stream()
                    .map(PaymentSlip::getSubjectName)
                    .distinct()
                    .forEach(subject -> {
                        List<PaymentSlip> subjectSlips = slips.stream()
                                .filter(s -> s.getSubjectName().equals(subject))
                                .toList();

                        Paragraph subjectTitle = new Paragraph(subject)
                                .setFontSize(14)
                                .setBold()
                                .setFontColor(PdfGeneratorUtil.PRIMARY_COLOR)
                                .setMarginTop(15)
                                .setMarginBottom(10);
                        document.add(subjectTitle);

                        addPaymentSlipsTable(document, subjectSlips);
                        document.add(new Paragraph("\n"));
                    });

            // Footer
            PdfGeneratorUtil.addReportFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate All Payment Slips PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF for payment slips filtered by status
     */
    public byte[] generatePaymentSlipsByStatusPdf(Long teacherId, String status) {
        try {
            List<PaymentSlip> slips = paymentSlipService.getSlipsBySubject(teacherId).stream()
                    .filter(s -> s.getStatus().equals(status))
                    .toList();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);

            // Header
            PdfGeneratorUtil.addReportHeader(document, status + " Payment Slips Report",
                    "Status: " + status + " | Total: " + slips.size());

            // Status banner
            DeviceRgb statusColor = getStatusColor(status);
            PdfGeneratorUtil.addStatusBanner(document, "Payment slips with status: " + status, statusColor);

            // Payment slips table
            addPaymentSlipsTable(document, slips);

            // Summary section
            addSummarySection(document, slips);

            // Footer
            PdfGeneratorUtil.addReportFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Status Payment Slips PDF: " + e.getMessage(), e);
        }
    }

    // ==================== HELPER METHODS ====================

    private DeviceRgb getStatusColor(String status) {
        return switch (status) {
            case "APPROVED" -> PdfGeneratorUtil.SUCCESS_COLOR;
            case "REJECTED" -> PdfGeneratorUtil.DANGER_COLOR;
            case "PENDING" -> PdfGeneratorUtil.WARNING_COLOR;
            default -> PdfGeneratorUtil.SECONDARY_COLOR;
        };
    }

    private void addPaymentSlipDetailsCard(Document document, PaymentSlip slip) {
        Object[][] data = {
                {"Slip ID", slip.getId().toString()},
                {"Student Name", slip.getStudentName()},
                {"Student Username", slip.getStudentUsername()},
                {"Subject", slip.getSubjectName()},
                {"Payment Month", slip.getPaymentMonth()},
                {"File Name", slip.getFileName()},
                {"File Type", slip.getFileType()},
                {"Status", slip.getStatus()},
                {"Submitted", slip.getUploadedAt().format(PdfGeneratorUtil.DATE_FORMAT)}
        };
        
        if (slip.getReviewedAt() != null) {
            Object[][] dataWithReview = new Object[data.length + 1][];
            System.arraycopy(data, 0, dataWithReview, 0, data.length);
            dataWithReview[data.length] = new Object[]{"Reviewed", slip.getReviewedAt().format(PdfGeneratorUtil.DATE_FORMAT)};
            PdfGeneratorUtil.addInfoCard(document, "Payment Slip Information", dataWithReview);
        } else {
            PdfGeneratorUtil.addInfoCard(document, "Payment Slip Information", data);
        }
    }

    private void addNotesSection(Document document, PaymentSlip slip) {
        Paragraph notesTitle = new Paragraph("Review Notes")
                .setFontSize(12)
                .setBold()
                .setFontColor(PdfGeneratorUtil.PRIMARY_COLOR)
                .setMarginTop(15)
                .setMarginBottom(10);
        document.add(notesTitle);

        Paragraph notesPara = new Paragraph(slip.getNote())
                .setFontSize(10)
                .setMarginBottom(15);
        document.add(notesPara);
    }

    private void addPaymentSlipsTable(Document document, List<PaymentSlip> slips) {
        float[] columnWidths = {1.5f, 1.5f, 1.5f, 1.5f, 1, 1};
        Table table = new Table(columnWidths)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Header row
        table.addCell(PdfGeneratorUtil.createHeaderCell("Student Name"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Subject"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Month"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("File"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Status"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Date"));

        // Data rows
        for (PaymentSlip slip : slips) {
            table.addCell(PdfGeneratorUtil.createBodyCell(slip.getStudentName()));
            table.addCell(PdfGeneratorUtil.createBodyCell(slip.getSubjectName()));
            table.addCell(PdfGeneratorUtil.createBodyCell(slip.getPaymentMonth()));
            table.addCell(PdfGeneratorUtil.createBodyCell(
                    slip.getFileName().length() > 20 ? slip.getFileName().substring(0, 20) + "..." : slip.getFileName()
            ));
            table.addCell(PdfGeneratorUtil.createCenteredBodyCell(slip.getStatus()));
            table.addCell(PdfGeneratorUtil.createCenteredBodyCell(
                    slip.getUploadedAt().format(PdfGeneratorUtil.DATE_ONLY_FORMAT)
            ));
        }

        document.add(table);
    }

    private void addSummarySection(Document document, List<PaymentSlip> slips) {
        long approvedCount = slips.stream().filter(s -> s.getStatus().equals("APPROVED")).count();
        long rejectedCount = slips.stream().filter(s -> s.getStatus().equals("REJECTED")).count();
        long pendingCount = slips.stream().filter(s -> s.getStatus().equals("PENDING")).count();

        Paragraph summaryTitle = new Paragraph("Summary")
                .setFontSize(12)
                .setBold()
                .setFontColor(PdfGeneratorUtil.PRIMARY_COLOR)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(summaryTitle);

        Object[][] summaryData = {
                {"Total Slips", slips.size() + ""},
                {"Approved", approvedCount + ""},
                {"Rejected", rejectedCount + ""},
                {"Pending", pendingCount + ""}
        };
        PdfGeneratorUtil.addInfoCard(document, "Summary Statistics", summaryData);
    }
}
