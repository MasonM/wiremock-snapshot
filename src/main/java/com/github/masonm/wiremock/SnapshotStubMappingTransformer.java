package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;

/**
 * Transforms ServeEvents to StubMappings using SnapshotRequestPatternTransformer and SnapshotResponseDefinitionTransformer
 */
public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private final SnapshotRequestPatternTransformer requestTransformer;
    private final SnapshotResponseDefinitionTransformer responseTransformer;

    public SnapshotStubMappingTransformer(
        SnapshotRequestPatternTransformer requestTransformer,
        SnapshotResponseDefinitionTransformer responseTransformer
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    public SnapshotStubMappingTransformer(SnapshotRequestPatternTransformer requestTransformer) {
        this(
            requestTransformer == null ? new SnapshotRequestPatternTransformer() : requestTransformer,
            new SnapshotResponseDefinitionTransformer()
        );
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        RequestPattern requestPattern = requestTransformer.apply(event.getRequest()).build();
        ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());
        return new StubMapping(requestPattern, responseDefinition);
    }
}
