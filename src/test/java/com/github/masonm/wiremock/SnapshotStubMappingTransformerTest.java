package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.junit.Assert.assertEquals;

public class SnapshotStubMappingTransformerTest {
    @Test
    public void apply() {
        final RequestPatternBuilder requestPatternBuilder = newRequestPattern();
        final ResponseDefinition responseDefinition = ResponseDefinition.ok();
        SnapshotRequestPatternTransformer requestTransformer = new SnapshotRequestPatternTransformer() {
            @Override
            public RequestPatternBuilder apply(Request request) {
                return requestPatternBuilder;
            }
        };

        SnapshotResponseDefinitionTransformer responseTransformer = new SnapshotResponseDefinitionTransformer() {
            @Override
            public ResponseDefinition apply(LoggedResponse response) {
                return responseDefinition;
            }
        };

        SnapshotStubMappingTransformer stubMappingTransformer = new SnapshotStubMappingTransformer(
            requestTransformer,
            responseTransformer
        );

        StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
        expected.setId(UUID.fromString("241ee4bc-98df-3069-abfc-9abe37650411"));

        assertEquals(expected, stubMappingTransformer.apply(new ServeEvent(
            null,
            LoggedRequest.createFrom(aRequest(new Mockery()).build()),
            null,
            null,
            LoggedResponse.from(Response.notConfigured()),
            false
        )));
    }
}
