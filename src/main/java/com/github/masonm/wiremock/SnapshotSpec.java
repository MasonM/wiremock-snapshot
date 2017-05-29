package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
class SnapshotSpec {
    // Whitelist requests to generate StubMappings for
    private SnapshotFilters filters;
    // How to sort the StubMappings (mainly for output purposes)
    private RequestFields sortFields;
    // Determines the fields to include in the StubMapping request
    private TemplatedRequestPatternTransformer requestTemplate;
    // How to format StubMappings in the response body
    // Either "full" (meaning return an array of rendered StubMappings) or "ids", which returns an array of UUIDs
    private String outputFormat;

    @JsonCreator
    public SnapshotSpec(@JsonProperty("filters") SnapshotFilters filters ,
                        @JsonProperty("sortFields") String[] sortFields,
                        @JsonProperty("requestTemplate") TemplatedRequestPatternTransformer requestTemplate,
                        @JsonProperty("outputFormat") String outputFormat) {
        this.filters = filters;
        this.outputFormat = outputFormat;
        this.requestTemplate = requestTemplate;
        if (sortFields != null) this.sortFields = new RequestFields(sortFields);
    }

    public SnapshotSpec() {}

    public SnapshotFilters getFilters() { return filters; }

    public RequestFields getSortFields() { return sortFields; }

    public TemplatedRequestPatternTransformer getRequestTemplate() { return requestTemplate; }

    public String getOutputFormat() { return outputFormat; }
}
