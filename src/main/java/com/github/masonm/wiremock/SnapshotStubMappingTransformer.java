package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.primitives.Bytes;

import java.util.UUID;

/**
 * Transforms ServeEvents to StubMappings using TemplatedRequestPatternTransformer and SnapshotResponseDefinitionTransformer
 */
public class SnapshotStubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private final TemplatedRequestPatternTransformer requestTransformer;
    private final SnapshotResponseDefinitionTransformer responseTransformer;

    public SnapshotStubMappingTransformer(
        TemplatedRequestPatternTransformer requestTransformer,
        SnapshotResponseDefinitionTransformer responseTransformer
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    public SnapshotStubMappingTransformer(TemplatedRequestPatternTransformer requestTransformer) {
        this(
            requestTransformer == null ? new TemplatedRequestPatternTransformer() : requestTransformer,
            new SnapshotResponseDefinitionTransformer()
        );
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        RequestPattern requestPattern = requestTransformer.apply(event.getRequest());
        ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());
        // create (hopefully) unique ID for the stub mapping using JSON representation of the RequestPattern and
        // ResponseDefinition, which will be used in SnapshotTask to de-dupe StubMappings
        byte[] hashCode = Bytes.concat(Json.toByteArray(requestPattern), Json.toByteArray(responseDefinition));

        StubMapping stubMapping = new StubMapping(requestPattern, responseDefinition);
        stubMapping.setId(UUID.nameUUIDFromBytes(hashCode));
        return stubMapping;
    }
}
