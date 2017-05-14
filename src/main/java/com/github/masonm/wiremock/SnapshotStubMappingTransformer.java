package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;

import java.util.UUID;

public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private SnapshotDefinition snapshotDefinition;
    private IdGenerator idGenerator;

    public SnapshotStubMappingTransformer(SnapshotDefinition snapshotDefinition) {
        this.idGenerator = new VeryShortIdGenerator();
        this.snapshotDefinition = snapshotDefinition;
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        SnapshotRequestPatternBuilder requestBuilder = new SnapshotRequestPatternBuilder(event.getRequest());
        if (snapshotDefinition.getCaptureFields() != null) {
            requestBuilder.setCaptureFields(snapshotDefinition.getCaptureFields());
        }

        SnapshotResponseDefinitionBuilder responseBuilder = new SnapshotResponseDefinitionBuilder(event.getResponse());

        String stubId = idGenerator.generate();
        StubMapping stubMapping = new StubMapping(requestBuilder.build(), responseBuilder.build());
        stubMapping.setUuid(UUID.nameUUIDFromBytes(stubId.getBytes()));

        return stubMapping;
    }
}
