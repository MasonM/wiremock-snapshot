package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Transforms ServeEvents to StubMappings using SnapshotRequestPatternTransformer and SnapshotResponseDefinitionTransformer
 */
public class SnapshotStubMappingGenerator {
    private final SnapshotRequestPatternTransformer requestTransformer;
    private final SnapshotResponseDefinitionTransformer responseTransformer;
    private final boolean shouldRecordRepeatsAsScenarios;
    private HashMap<RequestPattern, SnapshotStubMappingScenarioHandler> requestScenarioHandlerMap;

    public SnapshotStubMappingGenerator(
        SnapshotRequestPatternTransformer requestTransformer,
        SnapshotResponseDefinitionTransformer responseTransformer,
        boolean shouldRecordRepeatsAsScenarios
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
        this.shouldRecordRepeatsAsScenarios = shouldRecordRepeatsAsScenarios;
    }

    public SnapshotStubMappingGenerator(
        SnapshotRequestPatternTransformer requestTransformer,
        boolean shouldRecordRepeatsAsScenarios
    ) {
        this(
            requestTransformer == null ? new SnapshotRequestPatternTransformer() : requestTransformer,
            new SnapshotResponseDefinitionTransformer(),
            shouldRecordRepeatsAsScenarios
        );
    }

    public List<StubMapping> generateFrom(Iterable<ServeEvent> events) {
        this.requestScenarioHandlerMap = new HashMap<>(Iterables.size(events));

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
            if (requestScenarioHandlerMap.containsKey(stubMapping.getRequest())) {
                requestScenarioHandlerMap.get(stubMapping.getRequest()).trackStubMapping(stubMapping);
            } else {
                requestScenarioHandlerMap.put(stubMapping.getRequest(), new SnapshotStubMappingScenarioHandler(stubMapping));
            }
        }

        return stubMapping;
    }
}
