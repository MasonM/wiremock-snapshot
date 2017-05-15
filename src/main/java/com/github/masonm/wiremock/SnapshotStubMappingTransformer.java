package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;

import java.util.UUID;

public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private IdGenerator idGenerator;
    private SnapshotRequestPatternTransformer requestTransformer;
    private SnapshotResponseDefinitionTransformer responseTransformer;

    public SnapshotStubMappingTransformer(
            IdGenerator idGenerator,
            SnapshotResponseDefinitionTransformer responseTransformer,
            SnapshotRequestPatternTransformer requestTransformer,
            RequestFields captureFields) {
        this.idGenerator = idGenerator;
        this.responseTransformer = responseTransformer;
        this.requestTransformer = requestTransformer;
        if (captureFields != null) {
            this.requestTransformer.setCaptureFields(captureFields);
        }
    }

    public SnapshotStubMappingTransformer(RequestFields captureFields) {
        this(
                new VeryShortIdGenerator(),
                new SnapshotResponseDefinitionTransformer(),
                new SnapshotRequestPatternTransformer(),
                captureFields
        );
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
