package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.google.common.collect.Iterables.filter;

public class SnapshotResponseDefinitionTransformer implements Function<LoggedResponse, ResponseDefinition> {
    @Override
    public ResponseDefinition apply(LoggedResponse response) {
        ResponseDefinitionBuilder responseDefinitionBuilder = responseDefinition()
                .withStatus(response.getStatus())
                .withBody(bodyDecompressedIfRequired(response));

        if (response.getHeaders() != null) {
            responseDefinitionBuilder.withHeaders(withoutContentEncodingAndContentLength(response));
        }

        return responseDefinitionBuilder.build();
    }

    private HttpHeaders withoutContentEncodingAndContentLength(LoggedResponse response) {
        return new HttpHeaders(filter(response.getHeaders().all(), new Predicate<HttpHeader>() {
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
