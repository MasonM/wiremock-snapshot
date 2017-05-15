package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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

        Mockery context = new Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);
        final List<RequestField> fieldsList = Arrays.asList(
                context.mock(RequestField.class, "first"),
                context.mock(RequestField.class, "second")
        );
        context.checking(new Expectations() {{
            allowing(fieldsList.get(0)).compare(stub1.getRequest(), stub2.getRequest());
            will(returnValue(firstCompareResult));
            allowing(fieldsList.get(1)).compare(stub1.getRequest(), stub2.getRequest());
            will(returnValue(secondCompareResult));
        }});

        RequestFields fields = new RequestFields(fieldsList);
        return fields.compare(stub1, stub2);
    }
}
