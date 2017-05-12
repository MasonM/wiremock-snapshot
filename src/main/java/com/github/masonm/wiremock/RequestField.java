package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

public class RequestField {
    private final String field;

    @JsonCreator
    public RequestField(@JsonProperty("field") String field) {
        this.field = field;
    }

    public int compare(RequestPattern one, RequestPattern two) {
        if (field.equals("url")) {
            return one.getUrl().compareToIgnoreCase(two.getUrl());
        } else if (field.equals("method")) {
            return one.getMethod().equals(two.getMethod()) ? 0 : 1;
        } else {
            MultiValuePattern headerOne = one.getHeaders().get(field);
            MultiValuePattern headerTwo = two.getHeaders().get(field);
            if (headerOne == null) {
                return -1;
            }
            if (headerTwo == null) {
                return 1;
            }
            return headerOne.getExpected().compareToIgnoreCase(two.getExpected());
        }
    }


    @JsonValue
    public String value() { return field; }

    public boolean isHeader() { return field != "url" && field != "method"; }

    public String requestHeaderValue(LoggedRequest request) {
        return request.header(field).firstValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof CharSequence) return this.field.equals(o);
        if (o.getClass() != getClass()) return false;
        RequestField that = (RequestField) o;
        return this.field.equals(that.field);
    }
}
