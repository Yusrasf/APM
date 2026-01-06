package com.example.vaxregistry.service;

import com.example.vaxregistry.config.IceProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class IceDecisionSupportService {

    private static final String SOAP_ACTION_EVALUATE_AT_TIME =
            "http://www.omg.org/spec/CDSS/201105/dssWsdl:operation:evaluateAtSpecifiedTime";

    // CVX OID used by ICE (vMR)
    private static final String CVX_OID = "2.16.840.1.113883.12.292";

    private final FhirClientService fhir;
    private final RestClient ice;
    private final IceProperties props;
    private final ObjectMapper mapper;

    public IceDecisionSupportService(FhirClientService fhir, RestClient iceRestClient, IceProperties props, ObjectMapper mapper) {
        this.fhir = fhir;
        this.ice = iceRestClient;
        this.props = props;
        this.mapper = mapper;
    }

    public IceForecastResponse forecastForPatient(String patientId, OffsetDateTime specifiedTime) {
        // Fetch FHIR Patient
        String patientJson = fhir.read("Patient", patientId).getBody();
        if (patientJson == null || patientJson.isBlank()) {
            throw new IllegalStateException("FHIR Patient response was empty");
        }
        JsonNode patient = readJson(patientJson);
        String birthDate = patient.path("birthDate").asText("");
        String gender = patient.path("gender").asText("");

        if (birthDate.isBlank()) {
            throw new IllegalArgumentException("Patient.birthDate is required for official forecast (ICE)");
        }

        // Fetch immunizations
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("patient", patientId);
        String immBundleJson = fhir.search("Immunization", params).getBody();
        JsonNode bundle = readJson(immBundleJson == null ? "{}" : immBundleJson);
        List<CvxDose> cvxDoses = extractCvxDoses(bundle);

        String cdsInputXml = buildCdsInputXml(birthDate, gender, cvxDoses);
        String soapRequestXml = buildSoapEnvelope(cdsInputXml, specifiedTime);

        String soapResponse = ice.post()
                .uri("")
                .contentType(MediaType.valueOf("application/soap+xml"))
                .header("SOAPAction", SOAP_ACTION_EVALUATE_AT_TIME)
                .accept(MediaType.TEXT_XML, MediaType.APPLICATION_XML)
                .body(soapRequestXml)
                .retrieve()
                .body(String.class);

        if (soapResponse == null || soapResponse.isBlank()) {
            throw new IllegalStateException("ICE DSS response was empty");
        }

        String outPayloadB64 = extractFirstTagText(soapResponse, "base64EncodedPayload");
        if (outPayloadB64 == null || outPayloadB64.isBlank()) {
            throw new IllegalStateException("ICE DSS response missing base64EncodedPayload");
        }

        String cdsOutputXml = new String(Base64.getDecoder().decode(outPayloadB64), StandardCharsets.UTF_8);
        List<IceRecommendation> recs = parseRecommendationsFromCdsOutput(cdsOutputXml);

        IceForecastResponse resp = new IceForecastResponse();
        resp.service = "ICE";
        resp.endpointUrl = props.getEndpointUrl();
        resp.km = new IceForecastResponse.Km(props.getKm().getScopingEntityId(), props.getKm().getBusinessId(), props.getKm().getVersion());
        resp.patientId = patientId;
        resp.specifiedTime = specifiedTime.toString();
        resp.recommendations = recs;
        resp.inputDosesIncluded = cvxDoses.size();
        return resp;
    }

    private JsonNode readJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

    private static final Set<String> CVX_SYSTEMS = Set.of(
            "http://hl7.org/fhir/sid/cvx",
            "urn:oid:" + CVX_OID,
            CVX_OID
    );

    private List<CvxDose> extractCvxDoses(JsonNode immBundle) {
        List<CvxDose> out = new ArrayList<>();
        JsonNode entries = immBundle.path("entry");
        if (!entries.isArray()) return out;

        for (JsonNode e : entries) {
            JsonNode r = e.path("resource");
            if (!"Immunization".equals(r.path("resourceType").asText(""))) continue;

            String status = r.path("status").asText("");
            if (!status.isBlank() && !"completed".equalsIgnoreCase(status)) continue;

            String date = r.path("occurrenceDateTime").asText("");
            if (date.isBlank()) date = r.path("occurrenceString").asText("");
            if (date.isBlank()) continue;

            String cvx = null;
            String display = null;
            JsonNode coding = r.path("vaccineCode").path("coding");
            if (coding.isArray()) {
                for (JsonNode c : coding) {
                    String system = c.path("system").asText("");
                    if (!system.isBlank() && CVX_SYSTEMS.contains(system)) {
                        cvx = c.path("code").asText(null);
                        display = c.path("display").asText(null);
                        break;
                    }
                }
                // If no explicit CVX system, try heuristic: if code is numeric and display looks like a vaccine
                if (cvx == null) {
                    for (JsonNode c : coding) {
                        String code = c.path("code").asText("").trim();
                        if (code.matches("\\d{1,3}")) {
                            cvx = code;
                            display = c.path("display").asText(null);
                            break;
                        }
                    }
                }
            }

            if (cvx == null || cvx.isBlank()) continue;
            LocalDate ld = safeParseDate(date);
            if (ld == null) continue;
            out.add(new CvxDose(cvx, display, ld));
        }

        out.sort(Comparator.comparing(d -> d.date));
        return out;
    }

    private LocalDate safeParseDate(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        // FHIR often uses YYYY-MM-DD or full ISO date-time
        try {
            if (v.length() >= 10) {
                return LocalDate.parse(v.substring(0, 10));
            }
            return LocalDate.parse(v);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String buildCdsInputXml(String birthDateIso, String genderFhir, List<CvxDose> doses) {
        String birth = birthDateIso.replace("-", "");
        String gender = mapGender(genderFhir);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<ns4:cdsInput xmlns:ns2=\"org.opencds\" xmlns:ns3=\"org.opencds.vmr.v1_0.schema.vmr\" ");
        sb.append("xmlns:ns4=\"org.opencds.vmr.v1_0.schema.cdsinput\" xmlns:ns5=\"org.opencds.vmr.v1_0.schema.cdsoutput\">\n");
        sb.append("  <templateId root=\"2.16.840.1.113883.3.795.11.1.1\"/>\n");
        sb.append("  <cdsContext>\n");
        sb.append("    <cdsSystemUserPreferredLanguage code=\"en\" codeSystem=\"2.16.840.1.113883.6.99\" displayName=\"English\"/>\n");
        sb.append("  </cdsContext>\n");
        sb.append("  <vmrInput>\n");
        sb.append("    <templateId root=\"2.16.840.1.113883.3.795.11.1.1\"/>\n");
        sb.append("    <patient>\n");
        sb.append("      <templateId root=\"2.16.840.1.113883.3.795.11.2.1.1\"/>\n");
        sb.append("      <id root=\"").append(uuid()).append("\"/>\n");
        sb.append("      <demographics>\n");
        sb.append("        <birthTime value=\"").append(escapeXml(birth)).append("\"/>\n");
        sb.append("        <gender code=\"").append(escapeXml(gender)).append("\" codeSystem=\"2.16.840.1.113883.5.1\"/>\n");
        sb.append("      </demographics>\n");
        sb.append("      <clinicalStatements>\n");
        sb.append("        <substanceAdministrationEvents>\n");
        for (CvxDose d : doses) {
            sb.append("          <substanceAdministrationEvent>\n");
            sb.append("            <templateId root=\"2.16.840.1.113883.3.795.11.9.1.1\"/>\n");
            sb.append("            <id root=\"").append(uuid()).append("\"/>\n");
            sb.append("            <substanceAdministrationGeneralPurpose code=\"384810002\" codeSystem=\"2.16.840.1.113883.6.5\"/>\n");
            sb.append("            <substance>\n");
            sb.append("              <id root=\"").append(uuid()).append("\"/>\n");
            sb.append("              <substanceCode code=\"").append(escapeXml(d.cvx)).append("\" codeSystem=\"").append(CVX_OID).append("\"");
            if (d.display != null && !d.display.isBlank()) {
                sb.append(" displayName=\"").append(escapeXml(d.display)).append("\"");
            }
            sb.append("/>\n");
            sb.append("            </substance>\n");
            String ymd = d.date.format(DateTimeFormatter.BASIC_ISO_DATE);
            sb.append("            <administrationTimeInterval low=\"").append(ymd).append("\" high=\"").append(ymd).append("\"/>\n");
            sb.append("          </substanceAdministrationEvent>\n");
        }
        sb.append("        </substanceAdministrationEvents>\n");
        sb.append("      </clinicalStatements>\n");
        sb.append("    </patient>\n");
        sb.append("  </vmrInput>\n");
        sb.append("</ns4:cdsInput>\n");
        return sb.toString();
    }

    private String buildSoapEnvelope(String cdsInputXml, OffsetDateTime specifiedTime) {
        String b64 = Base64.getEncoder().encodeToString(cdsInputXml.getBytes(StandardCharsets.UTF_8));

        // ICE IG expects ISO date-time with offset.
        String specified = specifiedTime.toString();

        IceProperties.Km km = props.getKm();
        String interactionId = UUID.randomUUID().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>");
        sb.append("<S:Envelope xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\">");
        sb.append("<S:Body>");
        sb.append("<ns2:evaluateAtSpecifiedTime xmlns:ns2=\"http://www.omg.org/spec/CDSS/201105/dss\">");
        sb.append("<interactionId scopingEntityId=\"gov.nyc.health\" interactionId=\"").append(escapeXml(interactionId)).append("\"/>");
        sb.append("<specifiedTime>").append(escapeXml(specified)).append("</specifiedTime>");
        sb.append("<evaluationRequest clientLanguage=\"\" clientTimeZoneOffset=\"\">");
        sb.append("<kmEvaluationRequest>");
        sb.append("<kmId scopingEntityId=\"").append(escapeXml(km.getScopingEntityId())).append("\" businessId=\"").append(escapeXml(km.getBusinessId())).append("\" version=\"").append(escapeXml(km.getVersion())).append("\"/>");
        sb.append("</kmEvaluationRequest>");
        sb.append("<dataRequirementItemData>");
        sb.append("<driId itemId=\"cdsPayload\"><containingEntityId scopingEntityId=\"gov.nyc.health\" businessId=\"ICEData\" version=\"1.0.0.0\"/></driId>");
        sb.append("<data>");
        sb.append("<informationModelSSId scopingEntityId=\"org.opencds.vmr\" businessId=\"VMR\" version=\"1.0\"/>");
        sb.append("<base64EncodedPayload>").append(b64).append("</base64EncodedPayload>");
        sb.append("</data>");
        sb.append("</dataRequirementItemData>");
        sb.append("</evaluationRequest>");
        sb.append("</ns2:evaluateAtSpecifiedTime>");
        sb.append("</S:Body>");
        sb.append("</S:Envelope>");
        return sb.toString();
    }

    private String extractFirstTagText(String xml, String localName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setNamespaceAware(true);
            var doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xp = XPathFactory.newInstance().newXPath();
            String expr = "//*[local-name()='" + localName + "']/text()";
            return (String) xp.evaluate(expr, doc, XPathConstants.STRING);
        } catch (Exception e) {
            return null;
        }
    }

    private List<IceRecommendation> parseRecommendationsFromCdsOutput(String cdsOutputXml) {
        List<IceRecommendation> out = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setNamespaceAware(true);
            var doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(cdsOutputXml.getBytes(StandardCharsets.UTF_8)));

            XPath xp = XPathFactory.newInstance().newXPath();
            var nodes = (org.w3c.dom.NodeList) xp.evaluate("//*[local-name()='substanceAdministrationProposal']", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                org.w3c.dom.Node n = nodes.item(i);

                String focusCode = (String) xp.evaluate(".//*[local-name()='substanceCode']/@code", n, XPathConstants.STRING);
                String focusDisplay = (String) xp.evaluate(".//*[local-name()='substanceCode']/@displayName", n, XPathConstants.STRING);

                String recValueCode = (String) xp.evaluate(".//*[local-name()='relatedClinicalStatement']//*[local-name()='observationValue']//*[local-name()='concept']/@code", n, XPathConstants.STRING);
                String recValueDisplay = (String) xp.evaluate(".//*[local-name()='relatedClinicalStatement']//*[local-name()='observationValue']//*[local-name()='concept']/@displayName", n, XPathConstants.STRING);

                String reasonCode = (String) xp.evaluate(".//*[local-name()='relatedClinicalStatement']//*[local-name()='interpretation']/@code", n, XPathConstants.STRING);
                String reasonDisplay = (String) xp.evaluate(".//*[local-name()='relatedClinicalStatement']//*[local-name()='interpretation']/@displayName", n, XPathConstants.STRING);

                String proposedLow = (String) xp.evaluate(".//*[local-name()='proposedAdministrationTimeInterval']/@low", n, XPathConstants.STRING);
                String proposedHigh = (String) xp.evaluate(".//*[local-name()='proposedAdministrationTimeInterval']/@high", n, XPathConstants.STRING);
                String validLow = (String) xp.evaluate(".//*[local-name()='validAdministrationTimeInterval']/@low", n, XPathConstants.STRING);

                IceRecommendation r = new IceRecommendation();
                r.focusCode = blankToNull(focusCode);
                r.focusDisplayName = blankToNull(focusDisplay);
                r.recommendationCode = blankToNull(recValueCode);
                r.recommendationDisplay = blankToNull(recValueDisplay);
                r.reasonCode = blankToNull(reasonCode);
                r.reasonDisplay = blankToNull(reasonDisplay);
                r.earliestDate = ymdToIso(validLow);
                r.recommendedDate = ymdToIso(proposedLow);
                r.pastDueDate = ymdToIso(proposedHigh);
                out.add(r);
            }
        } catch (Exception e) {
            // If parsing fails, return empty list (caller can still show the raw endpoint used)
        }

        // de-dupe by focusCode+recCode
        LinkedHashMap<String, IceRecommendation> uniq = new LinkedHashMap<>();
        for (IceRecommendation r : out) {
            String k = (r.focusCode == null ? "" : r.focusCode) + "|" + (r.recommendationCode == null ? "" : r.recommendationCode);
            uniq.putIfAbsent(k, r);
        }
        return new ArrayList<>(uniq.values());
    }

    private static String ymdToIso(String ymd) {
        String v = ymd == null ? "" : ymd.trim();
        if (v.isEmpty() || v.length() != 8) return null;
        return v.substring(0, 4) + "-" + v.substring(4, 6) + "-" + v.substring(6, 8);
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String mapGender(String genderFhir) {
        String g = (genderFhir == null ? "" : genderFhir.trim().toLowerCase(Locale.ROOT));
        return switch (g) {
            case "male" -> "M";
            case "female" -> "F";
            default -> "UN";
        };
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static final class CvxDose {
        final String cvx;
        final String display;
        final LocalDate date;

        CvxDose(String cvx, String display, LocalDate date) {
            this.cvx = cvx;
            this.display = display;
            this.date = date;
        }
    }

    public static final class IceForecastResponse {
        public String service;
        public String endpointUrl;
        public Km km;
        public String patientId;
        public String specifiedTime;
        public int inputDosesIncluded;
        public List<IceRecommendation> recommendations = List.of();

        public static final class Km {
            public String scopingEntityId;
            public String businessId;
            public String version;

            public Km() {
            }

            public Km(String scopingEntityId, String businessId, String version) {
                this.scopingEntityId = scopingEntityId;
                this.businessId = businessId;
                this.version = version;
            }
        }
    }

    public static final class IceRecommendation {
        public String focusCode;
        public String focusDisplayName;
        public String recommendationCode;
        public String recommendationDisplay;
        public String reasonCode;
        public String reasonDisplay;
        public String earliestDate;
        public String recommendedDate;
        public String pastDueDate;
    }
}
