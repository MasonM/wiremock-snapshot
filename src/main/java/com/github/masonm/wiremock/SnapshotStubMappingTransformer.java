package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;

import java.util.UUID;

public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private final IdGenerator idGenerator;
    private final SnapshotRequestPatternTransformer requestTransformer;
    private final SnapshotResponseDefinitionTransformer responseTransformer;

    public SnapshotStubMappingTransformer(
            IdGenerator idGenerator,
            SnapshotRequestPatternTransformer requestTransformer,
            SnapshotResponseDefinitionTransformer responseTransformer) {
        this.idGenerator = idGenerator;
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    public SnapshotStubMappingTransformer(RequestFields captureFields) {
        this(
                new VeryShortIdGenerator(),
                new SnapshotRequestPatternTransformer(),
                new SnapshotResponseDefinitionTransformer()
        );
        if (captureFields != null) {
            this.requestTransformer.withCaptureFields(captureFields);
        }
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        String stubId = idGenerator.generate();
        StubMapping stubMapping = new StubMapping(
                requestTransformer.apply(event.getRequest()),
                responseTransformer.apply(event.getResponse())
        );
        stubMapping.setUuid(UUID.nameUUIDFromBytes(stubId.getBytes()));

        return stubMapping;
    }
}
