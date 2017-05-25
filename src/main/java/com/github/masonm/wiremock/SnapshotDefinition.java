package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
class SnapshotDefinition {
    // Whitelist requests to generate StubMappings for
    private SnapshotFilters filters;
    // How to sort the StubMappings (mainly for output purposes)
    private RequestFields sortFields;
    // What fields in the request to include in the StubMapping
    private RequestFields captureFields;
    // How to format StubMappings in the response body
    // Either "full" (meaning return an array of rendered StubMappings) or "ids", which returns an array of UUIDs
    private String outputFormat;

    @JsonCreator
    public SnapshotDefinition(@JsonProperty("filters") SnapshotFilters filters ,
                              @JsonProperty("sortFields") String[] sortFields,
                              @JsonProperty("captureFields") String[] captureFields,
                              @JsonProperty("outputFormat") String outputFormat) {
        this.filters = filters;
        this.outputFormat = outputFormat;
        if (sortFields != null) this.sortFields = new RequestFields(sortFields);
        if (captureFields != null) this.captureFields = new RequestFields(captureFields);
    }

    public SnapshotDefinition() {}

    public SnapshotFilters getFilters() { return filters; }

    public RequestFields getSortFields() { return sortFields; }

    public RequestFields getCaptureFields() { return captureFields; }

    public String getOutputFormat() { return outputFormat; }
}
