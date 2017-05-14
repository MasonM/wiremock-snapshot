package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class SnapshotRequestPatternBuilder {
    private LoggedRequest request;
    private RequestFields captureFields;

    public SnapshotRequestPatternBuilder(LoggedRequest request) {
        this.request = request;
    }

    public void setCaptureFields(RequestFields captureFields) {
        this.captureFields = captureFields;
    }

    public RequestPattern build() {
        RequestPatternBuilder builder;
        if (this.captureFields != null) {
            builder = this.captureFields.createRequestPatternBuilderFrom(request);
        } else {
            // Default: only capture method and URL
            builder = new RequestPatternBuilder(request.getMethod(), urlEqualTo(request.getUrl()));
        }

        String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            builder.withRequestBody(valuePatternForContentType());
        }

        return builder.build();
    }

    private StringValuePattern valuePatternForContentType() {
        ContentTypeHeader contentType = request.getHeaders().getContentTypeHeader();
        if (contentType.containsValue("json")) {
            return equalToJson(request.getBodyAsString(), true, true);
        } else if (contentType.containsValue("xml")) {
            return equalToXml(request.getBodyAsString());
        }

        return equalTo(request.getBodyAsString());
    }
}
