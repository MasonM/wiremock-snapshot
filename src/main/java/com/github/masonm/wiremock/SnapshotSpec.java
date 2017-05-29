package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;

import java.util.Map;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
class SnapshotSpec {
    // Whitelist requests to generate StubMappings for
    private SnapshotFilters filters;
    // How to sort the StubMappings (mainly for output purposes)
    private RequestFieldsComparator sortFields;
    // Headers from the request to include in the stub mapping, if they match the corresponding matcher
    private SnapshotRequestPatternTransformer captureHeaders;
    // How to format StubMappings in the response body
    // Either "full" (meaning return an array of rendered StubMappings) or "ids", which returns an array of UUIDs
    private String outputFormat;
    // Whether to persist stub mappings
    private boolean persist = true;

    @JsonCreator
    public SnapshotSpec(@JsonProperty("filters") SnapshotFilters filters ,
                        @JsonProperty("sortFields") String[] sortFields,
                        @JsonProperty("captureHeaders") Map<String, MultiValuePattern> captureHeaders,
                        @JsonProperty("outputFormat") String outputFormat,
                        @JsonProperty("persist") boolean persist) {
        this.filters = filters;
        this.outputFormat = outputFormat;
        this.captureHeaders = new SnapshotRequestPatternTransformer(captureHeaders);
        this.persist = persist;
        if (sortFields != null) this.sortFields = new RequestFieldsComparator(sortFields);
    }

    public SnapshotSpec() {}

    public SnapshotFilters getFilters() { return filters; }

    public RequestFieldsComparator getSortFields() { return sortFields; }

    public SnapshotRequestPatternTransformer getCaptureHeaders() { return captureHeaders; }

    public String getOutputFormat() { return outputFormat; }

    public boolean shouldPersist() { return persist; }
}
