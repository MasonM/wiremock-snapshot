package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;

import java.util.Map;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
class SnapshotSpec {
    // Whitelist requests to generate StubMappings for
    private SnapshotFilters filters;
    // Headers from the request to include in the stub mapping, if they match the corresponding matcher
    private SnapshotRequestPatternTransformer captureHeaders;
    // How to format StubMappings in the response body
    // Either "full" (meaning return an array of rendered StubMappings) or "ids", which returns an array of UUIDs
    private SnapshotOutputFormat outputFormat;
    // Whether to persist stub mappings
    private boolean persist = true;
    // Whether duplicate requests should be recorded as scenarios or just discarded
    private boolean repeatsAsScenarios = false;

    @JsonCreator
    public SnapshotSpec(
        @JsonProperty("filters") SnapshotFilters filters ,
        @JsonProperty("captureHeaders") Map<String, MultiValuePattern> captureHeaders,
        @JsonProperty("outputFormat") SnapshotOutputFormat outputFormat,
        @JsonProperty("persist") JsonNode persistNode,
        @JsonProperty("repeatsAsScenarios") JsonNode repeatsNode
    ) {
        this.filters = filters;
        this.outputFormat = outputFormat;
        this.captureHeaders = new SnapshotRequestPatternTransformer(captureHeaders);
        this.persist = persistNode.asBoolean(true);
        this.repeatsAsScenarios = repeatsNode.asBoolean(false);
    }

    public SnapshotSpec() {}

    public SnapshotFilters getFilters() { return filters; }

    public SnapshotRequestPatternTransformer getCaptureHeaders() { return captureHeaders; }

    public SnapshotOutputFormat getOutputFormat() { return outputFormat; }

    public boolean shouldPersist() { return persist; }

    public boolean shouldRecordRepeatsAsScenarios() { return repeatsAsScenarios; }
}
