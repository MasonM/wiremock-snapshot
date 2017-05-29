package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

import java.util.Comparator;

/**
 * Represents a field in a request that we can use for sorting or whitelisting.
 * Currently, the field is limited to "url", "method", or a request header.
 */
public class RequestField  implements Comparator<RequestPattern> {
    private final String field;

    @JsonCreator
    public RequestField(@JsonProperty("field") String field) {
        this.field = field;
    }

    /**
     * Compares two RequestPatterns in terms of the field.
     */
    public int compare(RequestPattern one, RequestPattern two) {
        switch (field) {
            case "url":
                return one.getUrl().compareToIgnoreCase(two.getUrl());
            case "method":
                return one.getMethod().equals(two.getMethod()) ? 0 : 1;
            default: // assume field is a header
                if (one.getHeaders() == null) {
                    return -1;
                } else if (two.getHeaders() == null) {
                    return 1;
                }

                MultiValuePattern headerOne = one.getHeaders().get(field);
                MultiValuePattern headerTwo = two.getHeaders().get(field);
                if (headerOne == null) {
                    return -1;
                } else if (headerTwo == null) {
                    return 1;
                }
                return headerOne.getExpected().compareToIgnoreCase(headerTwo.getExpected());
        }
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        RequestField that = (RequestField) o;
        return this.field.equals(that.field);
    }
}