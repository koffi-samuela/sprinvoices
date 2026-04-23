package com.example.sprinvoices.service;

import com.example.sprinvoices.models.Invoice;
import com.example.sprinvoices.models.InvoiceRow;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generatePdfBytes(Invoice invoice) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // ── COULEURS ───────────────────────────────────────
        BaseColor primaryRed   = new BaseColor(192, 57, 43);
        BaseColor dark         = new BaseColor(44, 62, 80);
        BaseColor lightGray    = new BaseColor(248, 249, 250);
        BaseColor softGray     = new BaseColor(245, 247, 250);
        BaseColor white        = BaseColor.WHITE;

        BaseColor green        = new BaseColor(39, 174, 96);
        BaseColor blue         = new BaseColor(41, 128, 185);
        BaseColor orange       = new BaseColor(230, 126, 34);

        // ── POLICES ────────────────────────────────────────
        Font brandFont   = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, primaryRed);
        Font titleFont   = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, dark);
        Font subFont     = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, dark);

        Font headerFont  = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, white);
        Font cellFont    = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, dark);
        Font boldFont    = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, dark);

        Font labelFont   = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, new BaseColor(127,140,141));
        Font footerFont  = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, white);

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        // ═══════════════════════════════════════════════════
        // HEADER
        // ═══════════════════════════════════════════════════
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2f, 1f});
        header.setSpacingAfter(25);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph("SPRINVOICES", brandFont));
        left.addElement(new Paragraph("Facturation & gestion clients", subFont));
        header.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph title = new Paragraph("FACTURE", titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(title);

        Paragraph number = new Paragraph("#" + invoice.getNumber(), subFont);
        number.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(number);

        // STATUT
        String statut = invoice.getPaidAt() != null ? "PAYÉE"
                : invoice.getInvoicedAt() != null ? "FACTURÉE"
                : "CRÉÉE";

        BaseColor statusColor = invoice.getPaidAt() != null ? green
                : invoice.getInvoicedAt() != null ? blue
                : orange;

        PdfPTable badge = new PdfPTable(1);
        PdfPCell badgeCell = new PdfPCell(new Phrase(statut, headerFont));
        badgeCell.setBackgroundColor(statusColor);
        badgeCell.setPadding(8);
        badgeCell.setBorder(Rectangle.NO_BORDER);
        badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        badge.addCell(badgeCell);

        right.addElement(badge);
        header.addCell(right);

        document.add(header);

        // ═══════════════════════════════════════════════════
        // SEPARATEUR
        // ═══════════════════════════════════════════════════
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);

        PdfPCell sepCell = new PdfPCell();
        sepCell.setBackgroundColor(primaryRed);
        sepCell.setFixedHeight(3f);
        sepCell.setBorder(Rectangle.NO_BORDER);

        sep.addCell(sepCell);
        document.add(sep);

        // ═══════════════════════════════════════════════════
        // CLIENT + DATES
        // ═══════════════════════════════════════════════════
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1f, 1f});
        info.setSpacingAfter(20);

        PdfPCell client = new PdfPCell();
        client.setBorder(Rectangle.NO_BORDER);
        client.setBackgroundColor(softGray);
        client.setPadding(15);

        client.addElement(new Paragraph("CLIENT", labelFont));
        client.addElement(new Paragraph(invoice.getCustomer().getName(), boldFont));

        if (invoice.getCustomer().getCorporateName() != null)
            client.addElement(new Paragraph(invoice.getCustomer().getCorporateName(), subFont));

        if (invoice.getCustomer().getAddress() != null)
            client.addElement(new Paragraph(invoice.getCustomer().getAddress(), subFont));

        if (invoice.getCustomer().getZipcode() != null && invoice.getCustomer().getCity() != null)
            client.addElement(new Paragraph(
                    invoice.getCustomer().getZipcode() + " " + invoice.getCustomer().getCity(), subFont));

        info.addCell(client);

        PdfPCell dates = new PdfPCell();
        dates.setBorder(Rectangle.NO_BORDER);
        dates.setBackgroundColor(softGray);
        dates.setPadding(15);

        dates.addElement(new Paragraph("DATES", labelFont));
        dates.addElement(new Paragraph("Créée : " + invoice.getCreatedAt().toLocalDate(), subFont));

        if (invoice.getInvoicedAt() != null)
            dates.addElement(new Paragraph("Facturée : " + invoice.getInvoicedAt().toLocalDate(), subFont));

        if (invoice.getPaidAt() != null)
            dates.addElement(new Paragraph("Payée : " + invoice.getPaidAt().toLocalDate(), subFont));

        dates.addElement(new Paragraph("Délai paiement : " + invoice.getCustomer().getDelay() + " jours", subFont));

        info.addCell(dates);

        document.add(info);

        // ═══════════════════════════════════════════════════
        // TABLE LIGNES
        // ═══════════════════════════════════════════════════
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f, 2f, 1f, 2f});
        table.setSpacingAfter(15);

        String[] headers = {"Désignation", "Catégorie", "Prix HT", "Qté", "Total HT"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(dark);
            cell.setPadding(10);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        boolean alt = false;

        for (InvoiceRow row : invoice.getRows()) {

            BaseColor bg = alt ? new BaseColor(250, 251, 252) : white;

            String[] values = {
                    row.getProduct().getDesignation(),
                    row.getProduct().getCategory() != null ? row.getProduct().getCategory() : "—",
                    String.format("%.2f €", row.getProduct().getUnitPrice()),
                    String.valueOf(row.getQuantity()),
                    String.format("%.2f €", row.amount())
            };

            for (String v : values) {
                PdfPCell c = new PdfPCell(new Phrase(v, cellFont));
                c.setBackgroundColor(bg);
                c.setPadding(10);
                c.setBorder(Rectangle.NO_BORDER);

                if (v.contains("€")) {
                    c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }

                table.addCell(c);
            }

            alt = !alt;
        }

        document.add(table);

        // ═══════════════════════════════════════════════════
        // TOTAL
        // ═══════════════════════════════════════════════════
        PdfPTable total = new PdfPTable(1);
        total.setWidthPercentage(40);
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell totalCell = new PdfPCell();
        totalCell.setBackgroundColor(primaryRed);
        totalCell.setPadding(14);
        totalCell.setBorder(Rectangle.NO_BORDER);

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, white);

        Paragraph totalText = new Paragraph(
                "TOTAL HT : " + String.format("%.2f €", invoice.total()),
                totalFont
        );

        totalText.setAlignment(Element.ALIGN_RIGHT);
        totalCell.addElement(totalText);
        total.addCell(totalCell);

        document.add(total);

        // ═══════════════════════════════════════════════════
        // FOOTER
        // ═══════════════════════════════════════════════════
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(dark);
        footerCell.setPadding(12);
        footerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph footerText = new Paragraph(
                "SprinVoices • Document généré automatiquement",
                footerFont
        );
        footerText.setAlignment(Element.ALIGN_CENTER);

        footerCell.addElement(footerText);
        footer.addCell(footerCell);

        document.add(footer);

        document.close();

        return baos.toByteArray();
    }
}