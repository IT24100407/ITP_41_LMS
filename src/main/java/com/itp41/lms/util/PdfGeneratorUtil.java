package com.itp41.lms.util;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfGeneratorUtil {

    // Brand colors matching the LMS design
    public static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(0, 123, 255);
    public static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(73, 80, 87);
    public static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(40, 167, 69);
    public static final DeviceRgb WARNING_COLOR = new DeviceRgb(255, 193, 7);
    public static final DeviceRgb DANGER_COLOR = new DeviceRgb(220, 53, 69);
    public static final DeviceRgb INFO_COLOR = new DeviceRgb(23, 162, 184);
    public static final DeviceRgb LIGHT_BG = new DeviceRgb(248, 249, 250);
    public static final DeviceRgb BORDER_COLOR = new DeviceRgb(233, 236, 239);
    public static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    public static final DeviceRgb DARK_TEXT = new DeviceRgb(33, 37, 41);
    public static final DeviceRgb MUTED_TEXT = new DeviceRgb(108, 117, 125);

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    public static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Add a styled report header to the document
     */
    public static void addReportHeader(Document document, String title, String subtitle) {
        // Title
        Paragraph titlePara = new Paragraph(title)
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(titlePara);

        // Subtitle
        if (subtitle != null && !subtitle.isEmpty()) {
            Paragraph subtitlePara = new Paragraph(subtitle)
                    .setFontSize(12)
                    .setFontColor(MUTED_TEXT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5)
                    .setMarginBottom(10);
            document.add(subtitlePara);
        }

        // Divider line
        Div divider = new Div()
                .setHeight(2)
                .setBackgroundColor(BORDER_COLOR)
                .setMarginBottom(20);
        document.add(divider);
    }

    /**
     * Add a status banner showing a specific message with background color
     */
    public static void addStatusBanner(Document document, String message, DeviceRgb backgroundColor) {
        Div banner = new Div()
                .setBackgroundColor(backgroundColor)
                .setPadding(15)
                .setMarginBottom(20);

        Paragraph bannerText = new Paragraph(message)
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(14)
                .setBold();
        banner.add(bannerText);
        document.add(banner);
    }

    /**
     * Add a styled info card with title and value pairs
     */
    public static void addInfoCard(Document document, String cardTitle, Object[][] data) {
        Div card = new Div()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setPadding(15)
                .setMarginBottom(15);

        // Card title
        Paragraph cardTitlePara = new Paragraph(cardTitle)
                .setFontSize(12)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(10);
        card.add(cardTitlePara);

        // Data table (2 columns)
        Table infoTable = new Table(new float[]{1, 1})
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(0);
        infoTable.setBorder(Border.NO_BORDER);

        for (Object[] row : data) {
            Cell labelCell = new Cell()
                    .add(new Paragraph(row[0].toString())
                            .setFontColor(MUTED_TEXT)
                            .setFontSize(10))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
            infoTable.addCell(labelCell);

            Cell valueCell = new Cell()
                    .add(new Paragraph(row[1].toString())
                            .setFontColor(DARK_TEXT)
                            .setFontSize(10)
                            .setBold())
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
            infoTable.addCell(valueCell);
        }

        card.add(infoTable);
        document.add(card);
    }

    /**
     * Add a report footer with timestamp
     */
    public static void addReportFooter(Document document) {
        document.add(new Divider().setMarginTop(20));

        Paragraph footerPara = new Paragraph(
                "Generated on " + LocalDateTime.now().format(DATE_FORMAT) + " | ITP_41 LMS")
                .setFontSize(9)
                .setFontColor(MUTED_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(15);
        document.add(footerPara);
    }

    /**
     * Create a cell with a standard header style
     */
    public static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(WHITE).setFontSize(11))
                .setBackgroundColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setBorder(new SolidBorder(BORDER_COLOR, 1));
    }

    /**
     * Create a cell with standard body style
     */
    public static Cell createBodyCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setPadding(8)
                .setBorder(new SolidBorder(BORDER_COLOR, 1));
    }

    /**
     * Create a centered body cell
     */
    public static Cell createCenteredBodyCell(String text) {
        return createBodyCell(text)
                .setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Divider element
     */
    public static class Divider extends Div {
        public Divider() {
            super();
            setHeight(1);
            setBackgroundColor(BORDER_COLOR);
        }
    }
}
