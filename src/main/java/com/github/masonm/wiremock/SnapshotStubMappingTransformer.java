package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static com.google.common.collect.Iterables.filter;

public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private SnapshotDefinition snapshotDefinition;
    private IdGenerator idGenerator;

    public SnapshotStubMappingTransformer(SnapshotDefinition snapshotDefinition) {
        this.idGenerator = new VeryShortIdGenerator();
        this.snapshotDefinition = snapshotDefinition;
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        String stubId = idGenerator.generate();
        RequestPattern request = buildRequestPatternFrom(event.getRequest());
        ResponseDefinition response = buildResponseDefinitionFrom(event.getResponse());

        StubMapping stubMapping = new StubMapping(request, response);
        stubMapping.setUuid(UUID.nameUUIDFromBytes(stubId.getBytes()));
        return stubMapping;
    }

    private RequestPattern buildRequestPatternFrom(LoggedRequest request) {
        RequestPatternBuilder builder = this.snapshotDefinition.createRequestPatternBuilderFrom(request);

        String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            builder.withRequestBody(valuePatternForContentType(request));
        }

        return builder.build();
    }

    private StringValuePattern valuePatternForContentType(Request request) {
        String contentType = request.getHeader("Content-Type");
        if (contentType != null) {
            if (contentType.contains("json")) {
                return equalToJson(request.getBodyAsString(), true, true);
            } else if (contentType.contains("xml")) {
                return equalToXml(request.getBodyAsString());
            }
        }

        return equalTo(request.getBodyAsString());
    }

    private ResponseDefinition buildResponseDefinitionFrom(LoggedResponse response) {
        byte[] body = bodyDecompressedIfRequired(response);

        ResponseDefinitionBuilder responseDefinitionBuilder = responseDefinition()
                .withStatus(response.getStatus())
                .withBody(body);

        if (response.getHeaders() != null) {
            responseDefinitionBuilder.withHeaders(withoutContentEncodingAndContentLength(response.getHeaders()));
        }

        return responseDefinitionBuilder.build();
    }

    private HttpHeaders withoutContentEncodingAndContentLength(HttpHeaders httpHeaders) {
        return new HttpHeaders(filter(httpHeaders.all(), new Predicate<HttpHeader>() {
            public boolean apply(HttpHeader header) {
                return !header.keyEquals("Content-Encoding") && !header.keyEquals("Content-Length");
            }
        }));
    }

    private byte[] bodyDecompressedIfRequired(LoggedResponse response) {
        if (response.getBody() == null) {
            return null;
        }

        HttpHeaders headers = response.getHeaders();
        if (headers != null && headers.getHeader("Content-Encoding").containsValue("gzip")) {
            return Gzip.unGzip(response.getBody().getBytes());
        }

        return response.getBody().getBytes();
    }
}
