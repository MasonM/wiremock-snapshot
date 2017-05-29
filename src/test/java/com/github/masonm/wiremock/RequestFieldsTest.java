package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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