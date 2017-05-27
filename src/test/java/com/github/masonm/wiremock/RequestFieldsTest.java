package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.*;

public class RequestFieldsTest {
    @Test
    public void compareWithNoFields(){
        RequestFields fields = new RequestFields(new ArrayList<RequestField>());
        assertEquals(0, fields.compare(new StubMapping(), new StubMapping()));
    }

    @Test
    public void compareEqualStubMappings() {
        assertEquals(0, compareWithTwoStubbedFields(0, 0));
    }

    @Test
    public void compareUnequalStubMappings() {
        assertEquals(1, compareWithTwoStubbedFields(1, -1));
        assertEquals(1, compareWithTwoStubbedFields(0, 1));
        assertEquals(-1, compareWithTwoStubbedFields(-1, 1));
        assertEquals(-1, compareWithTwoStubbedFields(0, -1));
    }

    @Test
    public void createRequestPatternBuilderFromWithNoFields() {
        RequestFields fields = new RequestFields();
        assertEquals(RequestPatternBuilder.allRequests().build(), fields.createRequestPatternBuilderFrom(mockRequest()).build());
    }

    @Test
    public void createRequestPatternBuilderFromWithUrlAndMethod() {
        RequestFields fields = new RequestFields("url", "method");
        Request request = mockRequest()
            .method(RequestMethod.GET)
            .url("/foo");
        RequestPatternBuilder expected = new RequestPatternBuilder(RequestMethod.GET, urlEqualTo("/foo"));
        assertEquals(expected.build(), fields.createRequestPatternBuilderFrom(request).build());
    }

    @Test
    public void createRequestPatternBuilderFromWithHeaders() {
        RequestFields fields = new RequestFields("method", "Accept", "X-Bar");

        Request request = mockRequest()
            .method(RequestMethod.GET)
            .header("Accept", "foo")
            .header("X-Bar", "Baz");

        RequestPatternBuilder expected = newRequestPattern(RequestMethod.GET, anyUrl())
            .withHeader("Accept", equalTo("foo"))
            .withHeader("X-Bar", equalTo("Baz"));

        assertEquals(expected.build(), fields.createRequestPatternBuilderFrom(request).build());
    }

    private int compareWithTwoStubbedFields(final int firstCompareResult, final int secondCompareResult) {
        final MappingBuilder stubBuilder = get(urlEqualTo("/foo"));
        final StubMapping stub1 = stubBuilder.build();
        final StubMapping stub2 = stubBuilder.build();

        RequestField one = new RequestField("first") {
            @Override
            public int compare(RequestPattern one, RequestPattern two) {
                return firstCompareResult;
            }
        };
        RequestField two = new RequestField("second") {
            @Override
            public int compare(RequestPattern one, RequestPattern two) {
                return secondCompareResult;
            }
        };

        RequestFields fields = new RequestFields(Arrays.asList(one, two));
        return fields.compare(stub1, stub2);
    }
}