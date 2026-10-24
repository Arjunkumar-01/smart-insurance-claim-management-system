package com.talentfarm.sicms.payment.service;

import com.talentfarm.sicms.payment.domain.Receipt;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReceiptPdfService {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC);

    public byte[] generate(Receipt receipt) {
        var payment = receipt.getPayment();
        List<String> lines = List.of(
                "Receipt number: " + receipt.getReceiptNumber(),
                "Payment number: " + payment.getPaymentNumber(),
                "Policy number: " + payment.getPolicy().getPolicyNumber(),
                "Payment method: " + payment.getPaymentMethod(),
                "Payment status: " + payment.getStatus(),
                "Amount: " + payment.getAmount().toPlainString(),
                "Paid at: " + TIMESTAMP.format(payment.getPaidAt()),
                "Receipt issued at: " + TIMESTAMP.format(receipt.getIssuedAt()),
                "This receipt records a simulated SICMS premium payment."
        );
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                content.newLineAtOffset(54, 738);
                content.showText("SICMS Premium Payment Receipt");
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                content.setLeading(24);
                for (String line : lines) {
                    content.newLine();
                    content.showText(line);
                }
                content.endText();
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Receipt PDF could not be generated", exception);
        }
    }
}