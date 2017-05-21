package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.junit.Assert.assertEquals;

public class SnapshotRequestPatternTransformerTest {
    private static SnapshotRequestPatternTransformer aTransformer() {
        return new SnapshotRequestPatternTransformer();
    }

    @Test
    public void applyWithoutCaptureFieldsOrBody() {
        LoggedRequest request = mockRequest().method(RequestMethod.GET).url("/foo").asLoggedRequest();
        RequestPatternBuilder expected = newRequestPattern(RequestMethod.GET, urlEqualTo("/foo"));
        assertEquals(expected.build(), aTransformer().apply(request));
    }

    @Test
    public void applyWithCaptureFieldsAndPlainTextBody() {
        RequestFields captureFields = new RequestFields(Arrays.asList(
            new RequestField("url"),
            new RequestField("Accept")
        ));
        LoggedRequest request = LoggedRequest.createFrom(aRequest(new Mockery())
            .withBodyAsBase64(encodeBase64("HELLO".getBytes()))
            .withMethod(RequestMethod.GET)
            .withUrl("/foo")
            .withHeader("Accept", "foo")
            .withHeader("User-Agent", "bar")
            .build());
        RequestPatternBuilder expected = newRequestPattern(RequestMethod.ANY, urlEqualTo("/foo"))
            .withRequestBody(equalTo("HELLO"))
            .withHeader("Accept", equalTo("foo"));

        assertEquals(expected.build(), aTransformer().withCaptureFields(captureFields).apply(request));
    }

    @Test
    public void applyWithEmptyCaptureFieldsAndJsonBody() {
        LoggedRequest request = LoggedRequest.createFrom(aRequest(new Mockery())
            .withHeader("Content-Type", "application/json")
            .withBodyAsBase64(encodeBase64("['hello']".getBytes()))
            .build());
        RequestPatternBuilder expected = allRequests()
            .withRequestBody(equalToJson("['hello']", true, true));

        assertEquals(expected.build(), aTransformer()
            .withCaptureFields(new RequestFields(new ArrayList<RequestField>()))
            .apply(request));
    }

    @Test
    public void applyWithEmptyCaptureFieldsAndXmlBody() {
        LoggedRequest request = LoggedRequest.createFrom(aRequest(new Mockery())
            .withHeader("Content-Type", "application/xml")
            .withBodyAsBase64(encodeBase64("<foo/>".getBytes()))
            .build());
        RequestPatternBuilder expected = allRequests()
            .withRequestBody(equalToXml("<foo/>"));

        assertEquals(expected.build(), aTransformer()
            .withCaptureFields(new RequestFields(new ArrayList<RequestField>()))
            .apply(request));
    }
}
