package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.util.Comparator;
import java.util.List;

public class RequestFields implements Comparator<StubMapping> {
    private final List<String> fields;

    @JsonCreator
    public RequestFields(@JsonProperty("fields") List<String> fields) {
        this.fields = fields;
    }

    public int compare(StubMapping one, StubMapping two) {
        for (String field : fields) {
            if (!requestFieldsEqual(field, one.getRequest(), two.getRequest())) {
                return 1;
            }
        }
        return 0;
    }

    public RequestPatternBuilder createRequestPatternBuilderFrom(LoggedRequest request) {
        RequestPatternBuilder builder = new RequestPatternBuilder(
            fields.contains("method") ? request.getMethod() : RequestMethod.ANY,
            fields.contains("url") ? urlEqualTo(request.getUrl()) : anyUrl()
        );

        for (String field : fields) {
            if (field.equals("url") || field.equals("method")) {
                continue;
            }
            if (request.containsHeader(field)) {
                builder = builder.withHeader(field, equalTo(request.header(field).firstValue()));
            }
        }

        return builder;
    }

    private static boolean requestFieldsEqual(String field, RequestPattern one, RequestPattern two) {
        if (field == "url") {
            return one.getUrl().equalsIgnoreCase(two.getUrl());
        } else if (field == "method") {
            return one.getMethod().equals(two.getMethod());
        } else {
            return one.getHeaders().get(field).equals(two.getHeaders().get(field));
        }
    }
}
