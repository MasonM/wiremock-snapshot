package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 * Creates a RequestPattern that includes any non-null fields (url, method, headers) which
 * match a given Request.
 */
public class TemplatedRequestPatternTransformer implements Function<Request, RequestPattern> {
    private UrlPattern url;
    private RequestMethod method;
    private Map<String, MultiValuePattern> headers;

    @JsonCreator
    public TemplatedRequestPatternTransformer() {
        // Default: always include URL and method
        this.url = anyUrl();
        this.method = RequestMethod.ANY;
    }

    @JsonCreator
    public TemplatedRequestPatternTransformer(
        @JsonProperty("url") String url,
        @JsonProperty("urlPattern") String urlPattern,
        @JsonProperty("urlPath") String urlPath,
        @JsonProperty("urlPathPattern") String urlPathPattern,
        @JsonProperty("method") RequestMethod method,
        @JsonProperty("headers") Map<String, MultiValuePattern> headers
    ) {
        if (url != null || urlPattern != null || urlPath != null || urlPathPattern != null) {
            this.url = UrlPattern.fromOneOf(url, urlPattern, urlPath, urlPathPattern);
        }
        this.method = method;
        this.headers = headers;
    }

    /**
     * Returns a RequestPatternBuilder with the non-null fields (url, method, headers) that
     * match the request.
     *
     * This is best explained with an example: Suppose there's you have an instance of this class with
     * with the url pattern ".*foo.*" and method "GET". If createFrom() is called with the request
     * "GET /foo", it will include both the URL and method in the RequestPatternBuilder. If createFrom()
     * is called with the request "POST /bar/foo", only the URL is included.
     *
     * @param request
     * @return RequestPattern with matched fields
     */
    @Override
    public RequestPattern apply(Request request) {
        UrlPattern urlToMatch = null;
        if (url != null && url.match(request.getUrl()).isExactMatch()) {
            urlToMatch = urlEqualTo(request.getUrl());
        }

        RequestMethod methodToMatch = null;
        if (method != null && method.match(request.getMethod()).isExactMatch()) {
            methodToMatch = request.getMethod();
        }

        Map<String, MultiValuePattern> headerPatterns = newLinkedHashMap();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, MultiValuePattern> header : headers.entrySet()) {
                String headerName = header.getKey();
                MultiValuePattern matcher = header.getValue();
                if (matcher.match(request.header(headerName)).isExactMatch()) {
                    StringValuePattern headerMatcher = equalTo(request.getHeader(headerName));
                    headerPatterns.put(headerName, MultiValuePattern.of(headerMatcher));
                }
            }
        }

        String body = request.getBodyAsString();

        return new RequestPattern(
            urlToMatch,
            methodToMatch,
            headerPatterns.isEmpty() ? null : headerPatterns,
            null,
            null,
            null,
            (body == null || body.isEmpty()) ? null : Lists.newArrayList(valuePatternForContentType(request)),
            null
        );
    }

    /**
     * If request body was JSON or XML, use "equalToJson" or "equalToXml" (respectively) in the RequestPattern so it's
     * easier to read. Otherwise, just use "equalTo"
     */
    private StringValuePattern valuePatternForContentType(Request request) {
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
