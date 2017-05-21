package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;

public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private final SnapshotRequestPatternTransformer requestTransformer;
    private final SnapshotResponseDefinitionTransformer responseTransformer;

    public SnapshotStubMappingTransformer(
        SnapshotRequestPatternTransformer requestTransformer,
        SnapshotResponseDefinitionTransformer responseTransformer,
        RequestFields captureFields
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
        if (captureFields != null) {
            this.requestTransformer.withCaptureFields(captureFields);
        }
    }

    public SnapshotStubMappingTransformer(RequestFields captureFields) {
        this(new SnapshotRequestPatternTransformer(), new SnapshotResponseDefinitionTransformer(), captureFields);
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        return new StubMapping(
            requestTransformer.apply(event.getRequest()),
            responseTransformer.apply(event.getResponse())
        );
    }
}
