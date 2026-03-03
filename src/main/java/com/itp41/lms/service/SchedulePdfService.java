package com.itp41.lms.service;

import com.itp41.lms.model.Schedule;
import com.itp41.lms.util.PdfGeneratorUtil;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.geom.PageSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class SchedulePdfService {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * Generate PDF for all schedules created by a teacher
     */
    public byte[] generateTeacherAllSchedulesPdf(Long teacherId) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);

            // Header
            PdfGeneratorUtil.addReportHeader(document, "Complete Schedules Report",
                    "All schedules | Total: " + schedules.size());

            // Status banner
            PdfGeneratorUtil.addStatusBanner(document, "Complete list of all schedules",
                    PdfGeneratorUtil.PRIMARY_COLOR);

            // Schedules table
            if (!schedules.isEmpty()) {
                addSchedulesTable(document, schedules);
                
                // Summary section
                addSummarySection(document, schedules);
            } else {
                Paragraph noDataMsg = new Paragraph("No schedules found.")
                        .setFontSize(12)
                        .setFontColor(PdfGeneratorUtil.DARK_TEXT);
                document.add(noDataMsg);
            }

            // Footer
            PdfGeneratorUtil.addReportFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate All Schedules PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF for schedules in a specific subject
     */
    public byte[] generateSubjectSchedulesPdf(Long teacherId, String subjectName) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByTeacherId(teacherId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(30, 30, 30, 30);

            // Header
            PdfGeneratorUtil.addReportHeader(document, "Schedules List Report",
                    "Subject: " + subjectName + " | Total Schedules: " + schedules.size());

            // Status banner
            PdfGeneratorUtil.addStatusBanner(document, "All schedules for " + subjectName,
                    PdfGeneratorUtil.INFO_COLOR);

            // Schedules table
            if (!schedules.isEmpty()) {
                addSchedulesTable(document, schedules);
                
                // Summary section
                addSummarySection(document, schedules);
            } else {
                Paragraph noDataMsg = new Paragraph("No schedules found for this subject.")
                        .setFontSize(12)
                        .setFontColor(PdfGeneratorUtil.DARK_TEXT);
                document.add(noDataMsg);
            }

            // Footer
            PdfGeneratorUtil.addReportFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Subject Schedules PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Add schedules table to document
     */
    private void addSchedulesTable(Document document, List<Schedule> schedules) {
        float[] columnWidths = {2, 2, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth()
                .setFontSize(10);

        // Header row
        table.addCell(PdfGeneratorUtil.createHeaderCell("Class Name"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Education Center"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Grade"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Date"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Time"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Class Type"));
        table.addCell(PdfGeneratorUtil.createHeaderCell("Online"));

        // Data rows
        for (Schedule schedule : schedules) {
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getClassName()));
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getEducationCenterName()));
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getGrade()));
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getScheduleDate().toString()));
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getScheduleTime().toString()));
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getClassType()));
            table.addCell(PdfGeneratorUtil.createBodyCell(schedule.getIsOnlineClass() ? "Yes" : "No"));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add summary section
     */
    private void addSummarySection(Document document, List<Schedule> schedules) {
        Paragraph summaryTitle = new Paragraph("Summary")
                .setFontSize(14)
                .setBold()
                .setFontColor(PdfGeneratorUtil.PRIMARY_COLOR)
                .setMarginBottom(10);
        document.add(summaryTitle);

        long onlineCount = schedules.stream().filter(Schedule::getIsOnlineClass).count();
        long offlineCount = schedules.size() - onlineCount;

        Paragraph totalSchedules = new Paragraph("Total Schedules: " + schedules.size())
                .setFontSize(11)
                .setMarginBottom(5);
        document.add(totalSchedules);

        Paragraph onlineSchedules = new Paragraph("Online Classes: " + onlineCount)
                .setFontSize(11)
                .setMarginBottom(5);
        document.add(onlineSchedules);

        Paragraph offlineSchedules = new Paragraph("Offline Classes: " + offlineCount)
                .setFontSize(11)
                .setMarginBottom(10);
        document.add(offlineSchedules);
    }
}
