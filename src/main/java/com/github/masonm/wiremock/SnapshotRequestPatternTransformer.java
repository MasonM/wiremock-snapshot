package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class SnapshotRequestPatternTransformer  implements Function<LoggedRequest, RequestPattern> {
    private RequestFields captureFields;

    public SnapshotRequestPatternTransformer withCaptureFields(RequestFields captureFields) {
        this.captureFields = captureFields;
        return this;
    }

    @Override
    public RequestPattern apply(LoggedRequest request) {
        RequestPatternBuilder builder;
        if (this.captureFields != null) {
            builder = this.captureFields.createRequestPatternBuilderFrom(request);
        } else {
            // Default: only capture method and URL
            builder = new RequestPatternBuilder(request.getMethod(), urlEqualTo(request.getUrl()));
        }

        String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            builder.withRequestBody(valuePatternForContentType(request));
        }

        return builder.build();
    }

    private StringValuePattern valuePatternForContentType(LoggedRequest request) {
        ContentTypeHeader contentType = request.getHeaders().getContentTypeHeader();
        if (contentType.mimeTypePart() != null) {
            if (contentType.mimeTypePart().contains("json")) {
                return equalToJson(request.getBodyAsString(), true, true);
            } else if (contentType.mimeTypePart().contains("xml")) {
                return equalToXml(request.getBodyAsString());
            }
        }

        return equalTo(request.getBodyAsString());
    }
}
