package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms ServeEvents to StubMappings using SnapshotRequestPatternTransformer and SnapshotResponseDefinitionTransformer
 */
public class SnapshotStubMappingGenerator {
    private final SnapshotRequestPatternTransformer requestTransformer;
    private final SnapshotResponseDefinitionTransformer responseTransformer;
    private final boolean shouldRecordRepeatsAsScenarios;
    private final SnapshotStubMappingScenarioHandler stubMappingScenarioHandler;

    public SnapshotStubMappingGenerator(
        SnapshotRequestPatternTransformer requestTransformer,
        SnapshotResponseDefinitionTransformer responseTransformer,
        boolean shouldRecordRepeatsAsScenarios,
        SnapshotStubMappingScenarioHandler stubMappingScenarioHandler
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
        this.shouldRecordRepeatsAsScenarios = shouldRecordRepeatsAsScenarios;
        this.stubMappingScenarioHandler = stubMappingScenarioHandler;
    }

    public SnapshotStubMappingGenerator(
        SnapshotRequestPatternTransformer requestTransformer,
        boolean shouldRecordRepeatsAsScenarios
    ) {
        this(
            requestTransformer == null ? new SnapshotRequestPatternTransformer() : requestTransformer,
            new SnapshotResponseDefinitionTransformer(),
            shouldRecordRepeatsAsScenarios,
            new SnapshotStubMappingScenarioHandler()
        );
    }

    public List<StubMapping> generateFrom(Iterable<ServeEvent> events) {
        this.stubMappingScenarioHandler.reset();

        final ArrayList<StubMapping> stubMappings = new ArrayList<>();
        for (ServeEvent event : events) {
            stubMappings.add(generateFrom(event));
        }
        return stubMappings;
    }

    private StubMapping generateFrom(ServeEvent event) {
        final RequestPattern requestPattern = requestTransformer.apply(event.getRequest()).build();
        final ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());

        final StubMapping stubMapping = new StubMapping(requestPattern, responseDefinition);

        if (shouldRecordRepeatsAsScenarios) {
            this.stubMappingScenarioHandler.trackStubMapping(stubMapping);
        }

        return stubMapping;
    }
}
