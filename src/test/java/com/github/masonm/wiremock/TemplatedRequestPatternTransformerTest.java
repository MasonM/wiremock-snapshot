package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.*;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;

public class TemplatedRequestPatternTransformerTest {
    @Test
    public void applyWithDefaultsAndNoBody() {
        TemplatedRequestPatternTransformer matcher = new TemplatedRequestPatternTransformer();

        Request request = mockRequest()
            .method(RequestMethod.GET)
            .url("/foo")
            .header("User-Agent", "foo")
            .header("X-Foo", "bar");
        // Default is to include method and URL exactly
        RequestPattern expected = aRequestPattern(urlEqualTo("/foo"), RequestMethod.GET);
        assertEquals(expected, matcher.apply(request));
    }

    @Test
    public void applyWithUrlWithMatchingRequest() {
        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer("/foo");

        Request request = mockRequest()
            .method(RequestMethod.POST)
            .url("/foo");
        // URL matches
        RequestPattern expected = aRequestPattern(urlEqualTo("/foo"), null);
        assertEquals(expected, fields.apply(request));
    }

    @Test
    public void applyWithUrlWithNonMatchingRequest() {
        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer("/foo");
        Request request = mockRequest()
            .method(RequestMethod.POST)
            .url("/bar");
        assertEquals(aRequestPattern(), fields.apply(request));
    }

    @Test
    public void applyWithUrlAndMethodAndMatchingUrl() {
        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer(".*foo", RequestMethod.GET);

        Request request = mockRequest()
            .method(RequestMethod.POST)
            .url("/bar/foo");
        RequestPattern expected = aRequestPattern(urlEqualTo("/bar/foo"));
        assertEquals(expected, fields.apply(request));
    }

    @Test
    public void applyWithUrlAndMethodAndBothMatch() {
        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer(".*foo", RequestMethod.GET);

        Request request = mockRequest()
            .method(RequestMethod.GET)
            .url("/foo");
        // Both URL and method match
        RequestPattern expected = aRequestPattern(urlEqualTo("/foo"), RequestMethod.GET);
        assertEquals(expected, fields.apply(request));
    }

    @Test
    public void applyWithUrlAndMethodAndMethodMatch() {
        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer(".*foo", RequestMethod.GET);

        Request request = mockRequest()
            .method(RequestMethod.GET)
            .url("/fo");
        // Method matches, but URL does not
        RequestPattern expected = aRequestPattern(null, RequestMethod.GET);
        assertEquals(expected, fields.apply(request));
    }

    @Test
    public void applyWithHeaders() {
        Map<String, MultiValuePattern> headers = newLinkedHashMap();
        headers.put("Accept", new MultiValuePattern(equalTo("foo")));
        headers.put("X-NoMatch", new MultiValuePattern(absent()));
        headers.put("X-Matches", new MultiValuePattern(matching(".az")));

        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer(
            ".*foo",
            RequestMethod.GET,
            headers
        );

        Request request = mockRequest()
            .method(RequestMethod.GET)
            .header("Accept", "foo")
            .header("X-Ignored", "ignored")
            .header("X-NoMatch", "not matching")
            .header("X-Matches", "Baz");

        Map<String, MultiValuePattern> expectedHeaders = newLinkedHashMap();
        expectedHeaders.put("Accept", MultiValuePattern.of(equalTo("foo")));
        expectedHeaders.put("X-Matches", MultiValuePattern.of(equalTo("Baz")));

        RequestPattern expected = aRequestPattern(null, RequestMethod.GET, expectedHeaders, null);

        assertEquals(expected, fields.apply(request));
    }

    @Test
    public void applyWithUrlAndPlainTextBody() {
        Map<String, MultiValuePattern> headers = newLinkedHashMap();
        headers.put("Accept", new MultiValuePattern(new AnythingPattern()));

        TemplatedRequestPatternTransformer fields = aRequestPatternTransformer(".*", null, headers);

        Request request = mockRequest()
            .body("HELLO")
            .method(RequestMethod.GET)
            .url("/foo")
            .header("Accept", "foo")
            .header("User-Agent", "bar");

        Map<String, MultiValuePattern> expectedHeaders = newLinkedHashMap();
        expectedHeaders.put("Accept", MultiValuePattern.of(equalTo("foo")));

        RequestPattern expected = aRequestPattern(
            urlEqualTo("/foo"),
            RequestMethod.ANY,
            expectedHeaders,
            equalTo("HELLO")
        );

        assertEquals(expected, fields.apply(request));
    }

    @Test
    public void applyWithOnlyJsonBody() {
        Request request = mockRequest()
            .header("Content-Type", "application/json")
            .body("['hello']");
        RequestPattern expected = aRequestPattern(equalToJson("['hello']"));

        assertEquals(expected, aRequestPatternTransformer(null).apply(request));
    }

    @Test
    public void applyWithOnlyXmlBody() {
        Request request = mockRequest()
            .header("Content-Type", "application/xml")
            .body("<foo/>");

        RequestPattern expected = aRequestPattern(equalToXml("<foo/>"));

        assertEquals(expected, aRequestPatternTransformer(null).apply(request));
    }

    private static TemplatedRequestPatternTransformer aRequestPatternTransformer(String urlPattern, RequestMethod method, Map<String, MultiValuePattern> headers) {
        return new TemplatedRequestPatternTransformer(null, urlPattern, null, null, method, headers);
    }

    private static TemplatedRequestPatternTransformer aRequestPatternTransformer(String urlPattern, RequestMethod method) {
        return aRequestPatternTransformer(urlPattern, method, null);
    }

    private static TemplatedRequestPatternTransformer aRequestPatternTransformer(String urlPattern) {
        return aRequestPatternTransformer(urlPattern, null);
    }

    private static RequestPattern aRequestPattern() {
        return aRequestPattern(null, null);
    }

    private static RequestPattern aRequestPattern(UrlPattern url) {
        return aRequestPattern(url, null);
    }

    private static RequestPattern aRequestPattern(UrlPattern url, RequestMethod method) {
        return aRequestPattern(url, method, null, null);
    }

    private static RequestPattern aRequestPattern(StringValuePattern bodyPattern) {
        return aRequestPattern(null, null, null, bodyPattern);
    }

    private static RequestPattern aRequestPattern(
        UrlPattern url,
        RequestMethod method,
        Map<String, MultiValuePattern> headers,
        StringValuePattern bodyPattern
    ) {
        return new RequestPattern(
            url,
            method,
            headers,
            null,
            null,
            null,
            bodyPattern != null ? Lists.<StringValuePattern>newArrayList(bodyPattern) : null,
            null);
    }
}
