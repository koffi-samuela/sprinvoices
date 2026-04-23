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

        // ── Couleurs ──────────────────────────────────────────
        BaseColor rouge     = new BaseColor(192, 57, 43);
        BaseColor grisClair = new BaseColor(248, 249, 250);
        BaseColor grisFonce = new BaseColor(44, 62, 80);
        BaseColor blanc     = BaseColor.WHITE;

        // ── Polices ───────────────────────────────────────────
        Font fontTitre     = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,   rouge);
        Font fontSousTitre = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, grisFonce);
        Font fontHeader    = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   blanc);
        Font fontCell      = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, grisFonce);
        Font fontBold      = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   grisFonce);
        Font fontTotal     = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   rouge);
        Font fontLabel     = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   new BaseColor(127,140,141));
        Font fontSmall     = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, grisFonce);

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        // ── EN-TÊTE ───────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1f, 1f});
        header.setSpacingAfter(20);

        PdfPCell cellLeft = new PdfPCell();
        cellLeft.setBorder(Rectangle.NO_BORDER);
        cellLeft.addElement(new Paragraph("SprinVoices", fontTitre));
        cellLeft.addElement(new Paragraph("Module de facturation", fontSousTitre));
        header.addCell(cellLeft);

        PdfPCell cellRight = new PdfPCell();
        cellRight.setBorder(Rectangle.NO_BORDER);
        cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Font fontFacture = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, grisFonce);
        Paragraph numFacture = new Paragraph("FACTURE #" + invoice.getNumber(), fontFacture);
        numFacture.setAlignment(Element.ALIGN_RIGHT);
        cellRight.addElement(numFacture);

        String statut = invoice.getPaidAt() != null ? "PAYÉE"
                      : invoice.getInvoicedAt() != null ? "FACTURÉE" : "CRÉÉE";
        BaseColor couleurStatut = invoice.getPaidAt() != null
                ? new BaseColor(39, 174, 96)
                : invoice.getInvoicedAt() != null
                ? new BaseColor(41, 128, 185)
                : new BaseColor(230, 126, 34);
        Font fontStatut = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, blanc);
        PdfPTable badgeTable = new PdfPTable(1);
        badgeTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        PdfPCell badgeCell = new PdfPCell(new Phrase(statut, fontStatut));
        badgeCell.setBackgroundColor(couleurStatut);
        badgeCell.setPadding(6);
        badgeCell.setBorder(Rectangle.NO_BORDER);
        badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        badgeTable.addCell(badgeCell);
        cellRight.addElement(badgeTable);
        header.addCell(cellRight);
        document.add(header);

        // ── SÉPARATEUR ────────────────────────────────────────
        PdfPTable separateur = new PdfPTable(1);
        separateur.setWidthPercentage(100);
        separateur.setSpacingAfter(20);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setBackgroundColor(rouge);
        sepCell.setFixedHeight(3f);
        sepCell.setBorder(Rectangle.NO_BORDER);
        separateur.addCell(sepCell);
        document.add(separateur);

        // ── BLOC CLIENT / DATES ───────────────────────────────
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 1f});
        infoTable.setSpacingAfter(24);

        PdfPCell clientCell = new PdfPCell();
        clientCell.setBorder(Rectangle.NO_BORDER);
        clientCell.setBackgroundColor(grisClair);
        clientCell.setPadding(12);
        clientCell.addElement(new Paragraph("FACTURER À", fontLabel));
        clientCell.addElement(new Paragraph(invoice.getCustomer().getName(), fontBold));
        if (invoice.getCustomer().getCorporateName() != null)
            clientCell.addElement(new Paragraph(invoice.getCustomer().getCorporateName(), fontSmall));
        if (invoice.getCustomer().getAddress() != null)
            clientCell.addElement(new Paragraph(invoice.getCustomer().getAddress(), fontSmall));
        if (invoice.getCustomer().getZipcode() != null && invoice.getCustomer().getCity() != null)
            clientCell.addElement(new Paragraph(
                invoice.getCustomer().getZipcode() + " " + invoice.getCustomer().getCity(), fontSmall));
        infoTable.addCell(clientCell);

        PdfPCell datesCell = new PdfPCell();
        datesCell.setBorder(Rectangle.NO_BORDER);
        datesCell.setBackgroundColor(grisClair);
        datesCell.setPadding(12);
        datesCell.addElement(new Paragraph("DATES", fontLabel));
        datesCell.addElement(new Paragraph("Créée le : " +
            invoice.getCreatedAt().toLocalDate(), fontSmall));
        if (invoice.getInvoicedAt() != null)
            datesCell.addElement(new Paragraph("Facturée le : " +
                invoice.getInvoicedAt().toLocalDate(), fontSmall));
        if (invoice.getPaidAt() != null)
            datesCell.addElement(new Paragraph("Payée le : " +
                invoice.getPaidAt().toLocalDate(), fontSmall));
        datesCell.addElement(new Paragraph("Délai paiement : " +
            invoice.getCustomer().getDelay() + " jours", fontSmall));
        infoTable.addCell(datesCell);
        document.add(infoTable);

        // ── TABLEAU LIGNES ────────────────────────────────────
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1.5f, 1.5f, 1f, 1.5f});
        table.setSpacingAfter(10);

        String[] hdrs = {"Désignation", "Catégorie", "Prix unitaire HT", "Qté", "Montant HT"};
        for (String h : hdrs) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, fontHeader));
            hCell.setBackgroundColor(grisFonce);
            hCell.setPadding(8);
            hCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(hCell);
        }

        boolean alt = false;
        for (InvoiceRow row : invoice.getRows()) {
            BaseColor bg = alt ? grisClair : blanc;
            String[] vals = {
                row.getProduct().getDesignation(),
                row.getProduct().getCategory() != null ? row.getProduct().getCategory() : "—",
                String.format("%.2f €", row.getProduct().getUnitPrice()),
                String.valueOf(row.getQuantity()),
                String.format("%.2f €", row.amount())
            };
            for (String val : vals) {
                PdfPCell c = new PdfPCell(new Phrase(val, fontCell));
                c.setBackgroundColor(bg);
                c.setPadding(8);
                c.setBorder(Rectangle.NO_BORDER);
                table.addCell(c);
            }
            alt = !alt;
        }
        document.add(table);

        // ── TOTAL ─────────────────────────────────────────────
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingAfter(30);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL HT", fontTotal));
        totalLabel.setBorder(Rectangle.NO_BORDER);
        totalLabel.setBackgroundColor(new BaseColor(253, 236, 234));
        totalLabel.setPadding(10);
        totalTable.addCell(totalLabel);

        PdfPCell totalVal = new PdfPCell(
            new Phrase(String.format("%.2f €", invoice.total()), fontTotal));
        totalVal.setBorder(Rectangle.NO_BORDER);
        totalVal.setBackgroundColor(new BaseColor(253, 236, 234));
        totalVal.setPadding(10);
        totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalVal);
        document.add(totalTable);

        // ── FOOTER ────────────────────────────────────────────
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(grisFonce);
        footerCell.setBorder(Rectangle.NO_BORDER);
        footerCell.setPadding(10);
        Font fontFooter = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, blanc);
        Paragraph footerText = new Paragraph("SprinVoices — Document généré automatiquement", fontFooter);
        footerText.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(footerText);
        footerTable.addCell(footerCell);
        document.add(footerTable);

        document.close();
        return baos.toByteArray();
    }
}