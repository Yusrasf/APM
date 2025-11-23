package org.apm.backend.dto.immunization;

import java.util.List;

public class VaccinationCertificateDto {

    private String patientId;
    private String patientName;

    private List<ImmunizationDto> immunizations;

    /**
     * QR code payload, e.g. base64 or plain text.
     */
    private String qrContent;

    /**
     * Optional link to a generated PDF (if you implement that).
     */
    private String pdfUrl;

    private String generatedAt;

    public VaccinationCertificateDto() {
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public List<ImmunizationDto> getImmunizations() {
        return immunizations;
    }

    public void setImmunizations(List<ImmunizationDto> immunizations) {
        this.immunizations = immunizations;
    }

    public String getQrContent() {
        return qrContent;
    }

    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
