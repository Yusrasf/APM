package com.example.vaxregistry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PdfService {

    // PDFBox 3 removed the PDType1Font.HELVETICA_* constants. Use Standard 14 fonts instead.
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    private final FhirClientService fhir;
    private final ObjectMapper mapper;

    public PdfService(FhirClientService fhir, ObjectMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public byte[] generateCertificatePdf(String patientId, String qrText) {
        CertificateData data = loadCertificateData(patientId);
        String issuedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String title = "Digital Vaccination Certificate (Demo)";

        List<String> lines = new ArrayList<>();
        lines.add("Issued at: " + issuedAt);
        lines.add("");
        lines.add("Patient (FHIR): Patient/" + patientId);
        if (!data.patientName.isBlank()) lines.add("Name: " + data.patientName);
        if (!data.birthDate.isBlank()) lines.add("Birth date: " + data.birthDate);
        if (!data.identifier.isBlank()) lines.add("Identifier: " + data.identifier);
        if (!data.country.isBlank()) lines.add("Country: " + data.country);
        lines.add("");
        lines.add("Vaccinations: " + data.immunizations.size());
        lines.addAll(formatImmunizationLines(data.immunizations));

        return buildPdf(title, lines, qrText);
    }

    public byte[] generatePhysicianReportPdf(String patientId, String qrText, String note) {
        CertificateData data = loadCertificateData(patientId);

        String title = "Vaccination Report (Demo)";

        List<String> lines = new ArrayList<>();
        lines.add("Patient (FHIR): Patient/" + patientId);
        if (!data.patientName.isBlank()) lines.add("Name: " + data.patientName);
        if (!data.birthDate.isBlank()) lines.add("Birth date: " + data.birthDate);
        if (!data.identifier.isBlank()) lines.add("Identifier: " + data.identifier);
        if (!data.country.isBlank()) lines.add("Country: " + data.country);
        if (note != null && !note.isBlank()) {
            lines.add("");
            lines.add("Note: " + note.trim());
        }
        lines.add("");
        lines.add("Vaccinations: " + data.immunizations.size());
        lines.addAll(formatImmunizationLines(data.immunizations));

        return buildPdf(title, lines, qrText);
    }

    private byte[] buildPdf(String title, List<String> lines, String qrText) {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            float margin = 50;
            float yStart = 780;
            float fontSize = 12;
            float leading = 16;

            // First page
            PDPage first = new PDPage(PDRectangle.A4);
            doc.addPage(first);

            BufferedImage qrImage = qrCode(qrText, 220);
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, qrImage);

            // Title + body with paging
            writePaged(doc, first, title, lines, margin, yStart, fontSize, leading);

            // QR on first page
            try (PDPageContentStream cs = new PDPageContentStream(doc, first, PDPageContentStream.AppendMode.APPEND, true)) {
                cs.drawImage(pdImage, margin, 90, 220, 220);

                cs.beginText();
                cs.setFont(FONT_ITALIC, 10);
                cs.newLineAtOffset(margin, 70);
                cs.showText("QR payload: " + shorten(qrText, 90));
                cs.endText();
            }

            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF", e);
        }
    }

    private void writePaged(PDDocument doc, PDPage firstPage, String title, List<String> lines,
                            float margin, float yStart, float fontSize, float leading) throws Exception {
        PDPage page = firstPage;
        float y = yStart;

        // write title on first page
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.beginText();
            cs.setFont(FONT_BOLD, 18);
            cs.newLineAtOffset(margin, y);
            cs.showText(title);
            cs.endText();
        }
        y -= 34;

        int i = 0;
        while (i < lines.size()) {
            boolean reserveQrSpace = page == firstPage;
            float usableWidth = page.getMediaBox().getWidth() - (2 * margin) - (reserveQrSpace ? 260 : 0);
            float bottomY = reserveQrSpace ? 320 : 60;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
                cs.setFont(FONT_REGULAR, fontSize);
                cs.beginText();
                cs.newLineAtOffset(margin, y);

                for (; i < lines.size(); i++) {
                    String raw = lines.get(i);
                    String line = raw == null ? "" : raw;

                    if (line.isEmpty()) {
                        cs.newLineAtOffset(0, -leading);
                        y -= leading;
                        if (y < bottomY) break;
                        continue;
                    }

                    for (String wrapped : wrap(line, FONT_REGULAR, fontSize, usableWidth)) {
                        cs.showText(wrapped);
                        cs.newLineAtOffset(0, -leading);
                        y -= leading;
                        if (y < bottomY) break;
                    }
                    if (y < bottomY) break;
                }

                cs.endText();
            }

            if (i < lines.size()) {
                // new page
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                y = yStart;
            }
        }
    }

    private static List<String> wrap(String text, PDType1Font font, float fontSize, float width) throws Exception {
        String[] words = text.split("\\s+");
        List<String> out = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (String w : words) {
            if (line.isEmpty()) {
                line.append(w);
                continue;
            }
            String candidate = line + " " + w;
            float wWidth = font.getStringWidth(candidate) / 1000f * fontSize;
            if (wWidth <= width) {
                line.append(' ').append(w);
            } else {
                out.add(line.toString());
                line = new StringBuilder(w);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }

    private record ImmunizationLine(String date, String vaccine, String lot, String performer, String location,
                                    String status) {
    }

    private record CertificateData(String patientName, String birthDate, String identifier, String country,
                                   List<ImmunizationLine> immunizations) {
    }

    private CertificateData loadCertificateData(String patientId) {
        try {
            JsonNode patient = mapper.readTree(fhir.read("Patient", patientId).getBody());
            String patientName = formatHumanName(patient.path("name"));
            String birthDate = patient.path("birthDate").asText("");
            String identifier = formatIdentifier(patient.path("identifier"));
            String country = formatCountry(patient.path("address"));

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("patient", patientId);
            params.add("_count", "200");
            JsonNode bundle = mapper.readTree(fhir.search("Immunization", params).getBody());

            List<ImmunizationLine> immunizations = extractImmunizations(bundle);
            immunizations.sort(Comparator.comparing(ImmunizationLine::date).reversed());

            return new CertificateData(patientName, birthDate, identifier, country, immunizations);
        } catch (Exception e) {
            // If the patient fetch fails, still generate a PDF with the id.
            return new CertificateData("", "", "", "", List.of());
        }
    }

    private List<String> formatImmunizationLines(List<ImmunizationLine> ims) {
        List<String> out = new ArrayList<>();
        if (ims.isEmpty()) {
            out.add("- (none found)");
            return out;
        }
        for (ImmunizationLine im : ims) {
            String date = im.date == null || im.date.isBlank() ? "(unknown date)" : im.date;
            String vaccine = im.vaccine == null || im.vaccine.isBlank() ? "(unknown vaccine)" : im.vaccine;
            String status = im.status == null || im.status.isBlank() ? "" : " | " + im.status;
            String lot = im.lot == null || im.lot.isBlank() ? "" : " | Lot: " + im.lot;
            String perf = im.performer == null || im.performer.isBlank() ? "" : " | By: " + im.performer;
            String loc = im.location == null || im.location.isBlank() ? "" : " | At: " + im.location;
            out.add("- " + date + " — " + vaccine + status + lot + perf + loc);
        }
        return out;
    }

    private static List<ImmunizationLine> extractImmunizations(JsonNode bundle) {
        List<ImmunizationLine> out = new ArrayList<>();
        JsonNode entries = bundle.path("entry");
        if (!entries.isArray()) return out;
        for (JsonNode entry : entries) {
            JsonNode res = entry.path("resource");
            if (!"Immunization".equals(res.path("resourceType").asText())) continue;

            String date = res.path("occurrenceDateTime").asText("");
            if (date.isBlank()) date = res.path("recorded").asText("");
            // try to keep it as YYYY-MM-DD
            if (!date.isBlank()) {
                try {
                    date = date.length() >= 10 ? date.substring(0, 10) : date;
                    LocalDate.parse(date);
                } catch (Exception ignored) {
                }
            }

            String vaccine = "";
            JsonNode vc = res.path("vaccineCode");
            if (vc.path("text").isTextual()) vaccine = vc.path("text").asText();
            JsonNode coding = vc.path("coding");
            if (vaccine.isBlank() && coding.isArray() && !coding.isEmpty()) {
                JsonNode c0 = coding.get(0);
                vaccine = c0.path("display").asText("");
                if (vaccine.isBlank()) vaccine = c0.path("code").asText("");
            }

            String lot = res.path("lotNumber").asText("");

            String performer = "";
            JsonNode perf = res.path("performer");
            if (perf.isArray() && !perf.isEmpty()) {
                JsonNode actor = perf.get(0).path("actor");
                performer = actor.path("display").asText("");
                if (performer.isBlank()) performer = actor.path("reference").asText("");
                performer = performer.replace("Practitioner/", "");
            }

            String location = "";
            JsonNode loc = res.path("location");
            if (loc.isObject()) {
                location = loc.path("display").asText("");
                if (location.isBlank()) location = loc.path("reference").asText("");
                location = location.replace("Location/", "");
            }

            String status = res.path("status").asText("");

            out.add(new ImmunizationLine(date, vaccine, lot, performer, location, status));
        }
        return out;
    }

    private static String formatHumanName(JsonNode nameNode) {
        if (!nameNode.isArray() || nameNode.isEmpty()) return "";
        JsonNode n = nameNode.get(0);
        if (n == null || !n.isObject()) return "";
        String text = n.path("text").asText("").trim();
        if (!text.isEmpty()) return text;
        String family = n.path("family").asText("").trim();
        String given = "";
        JsonNode givenArr = n.path("given");
        if (givenArr.isArray() && !givenArr.isEmpty()) {
            given = givenArr.get(0).asText("").trim();
        }
        return (given + " " + family).trim();
    }

    private static String formatIdentifier(JsonNode identifierNode) {
        if (!identifierNode.isArray() || identifierNode.isEmpty()) return "";
        for (JsonNode id : identifierNode) {
            String value = id.path("value").asText("").trim();
            if (value.isEmpty()) continue;
            String system = id.path("system").asText("").trim();
            return system.isEmpty() ? value : system + "|" + value;
        }
        return "";
    }

    private static String formatCountry(JsonNode addressNode) {
        if (!addressNode.isArray() || addressNode.isEmpty()) return "";
        for (JsonNode a : addressNode) {
            String c = a.path("country").asText("").trim();
            if (!c.isEmpty()) return c;
        }
        return "";
    }

    private static BufferedImage qrCode(String text, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
