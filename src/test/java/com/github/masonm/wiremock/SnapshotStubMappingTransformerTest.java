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

        StubMapping actual = stubMappingTransformer.apply(new ServeEvent(
            null,
            LoggedRequest.createFrom(aRequest(new Mockery()).build()),
            null,
            null,
            LoggedResponse.from(Response.notConfigured()),
            false
        ));
        StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
        expected.setId(actual.getId());

        assertEquals(expected, actual);
    }
}
