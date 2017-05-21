package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.primitives.Bytes;

import java.util.UUID;

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
        ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());
        RequestPattern requestPattern = requestTransformer.apply(event.getRequest());
        byte[] hashCode = Bytes.concat(Json.toByteArray(requestPattern), Json.toByteArray(responseDefinition));

        StubMapping stubMapping = new StubMapping(requestPattern, responseDefinition);
        stubMapping.setId(UUID.nameUUIDFromBytes(hashCode));
        return stubMapping;
    }
}
