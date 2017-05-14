package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class SnapshotDefinition {
    private SnapshotFilters filters;
    private RequestFields sortFields;
    private RequestFields captureFields;
    private String outputFormat;

    @JsonCreator
    public SnapshotDefinition(@JsonProperty("filters") SnapshotFilters filters ,
                              @JsonProperty("sortFields") RequestFields sortFields,
                              @JsonProperty("captureFields") RequestFields captureFields,
                              @JsonProperty("outputFormat") String outputFormat) {
        this.filters = filters;
        this.sortFields = sortFields;
        this.captureFields = captureFields;
        this.outputFormat = outputFormat;
    }

    public SnapshotDefinition() {
        //Concession to Jackson
    }

    public SnapshotFilters getFilters() { return filters; }

    public RequestFields getSortFields() { return sortFields; }

    public RequestFields getCaptureFields() { return captureFields; }

    public String renderStubMapping(StubMapping stubMapping) {
        if (outputFormat != null && outputFormat.equals("full")) {
            return Json.write(stubMapping);
        } else {
            return stubMapping.getUuid().toString();
        }

    }

}
