package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import sun.misc.Request;

import java.util.Comparator;
import java.util.List;

public class SnapshotMatchFields implements Comparator<StubMapping> {
    private final List<String> fields;

    @JsonCreator
    public SnapshotMatchFields(@JsonProperty("fields") List<String> fields) {
        this.fields = fields;
    }

    public int compare(StubMapping one, StubMapping two) {
        for (String field : fields) {
            if (!compareByField(field, one.getRequest(), two.getRequest())) {
                return 1;
            }
        }
        return 0;
    }

    public boolean compareByField(String field, RequestPattern one, RequestPattern two) {
        if (field == "url") {
            return one.getUrl().equalsIgnoreCase(two.getUrl());
        } else if (field == "method") {
            return one.getMethod().equals(two.getMethod());
        } else if (field.matches("header\\[[^\\]*]")) {
            String header = field.substring("header[".length(), field.length() - 2);
            return one.getHeaders().get(header).equals(two.getHeaders().get(header));
        } else {
            throw new IllegalArgumentException("Unknown field:" + field);
        }
    }
}
