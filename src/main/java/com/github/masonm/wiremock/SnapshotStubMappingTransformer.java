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
            SnapshotDefinition snapshotDefinition,
            IdGenerator idGenerator,
            SnapshotResponseDefinitionTransformer responseTransformer,
            SnapshotRequestPatternTransformer requestTransformer) {
        this.idGenerator = idGenerator;
        this.responseTransformer = responseTransformer;
        this.requestTransformer = requestTransformer;
        if (snapshotDefinition.getCaptureFields() != null) {
            this.requestTransformer.setCaptureFields(snapshotDefinition.getCaptureFields());
        }
    }

    public SnapshotStubMappingTransformer(SnapshotDefinition snapshotDefinition) {
        this(
                snapshotDefinition,
                new VeryShortIdGenerator(),
                new SnapshotResponseDefinitionTransformer(),
                new SnapshotRequestPatternTransformer()
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
