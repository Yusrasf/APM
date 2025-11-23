package org.apm.backend.dto.common;

import java.util.ArrayList;
import java.util.List;

public class CodeableConceptDto {

    private String text;
    private List<CodingDto> codings = new ArrayList<>();

    public CodeableConceptDto() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<CodingDto> getCodings() {
        return codings;
    }

    public void setCodings(List<CodingDto> codings) {
        this.codings = codings;
    }
}
