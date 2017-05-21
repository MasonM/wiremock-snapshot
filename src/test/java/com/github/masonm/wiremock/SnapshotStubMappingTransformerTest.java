package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.junit.Assert.assertEquals;

public class SnapshotStubMappingTransformerTest {
    @Test
    public void apply() {
        Mockery context = new Mockery();
        context.setImposteriser(ClassImposteriser.INSTANCE);

        @SuppressWarnings("unchecked")
        final SnapshotRequestPatternTransformer requestTransformer = context.mock(SnapshotRequestPatternTransformer.class);
        final RequestPattern requestPattern = newRequestPattern().build();
        final RequestFields captureFields = new RequestFields("foo");

        @SuppressWarnings("unchecked")
        final SnapshotResponseDefinitionTransformer responseTransformer = context.mock(SnapshotResponseDefinitionTransformer.class);
        final ResponseDefinition responseDefinition = ResponseDefinition.ok();

        context.checking(new Expectations() {{
            allowing(requestTransformer).apply(with(any(LoggedRequest.class))); will(returnValue(requestPattern));
            allowing(requestTransformer).withCaptureFields(with(captureFields));
            allowing(responseTransformer).apply(with(any(LoggedResponse.class))); will(returnValue(responseDefinition));
        }});

        SnapshotStubMappingTransformer stubMappingTransformer = new SnapshotStubMappingTransformer(
            requestTransformer,
            responseTransformer,
            captureFields
        );

        StubMapping expected = new StubMapping(requestPattern, responseDefinition);
        expected.setId(UUID.fromString("241ee4bc-98df-3069-abfc-9abe37650411"));

        assertEquals(expected, stubMappingTransformer.apply(new ServeEvent(
            null,
            LoggedRequest.createFrom(aRequest(context).build()),
            null,
            null,
            LoggedResponse.from(Response.notConfigured()),
            false
        )));
    }
}
