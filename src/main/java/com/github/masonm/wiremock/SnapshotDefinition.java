package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.Arrays;

public class SnapshotDefinition {
    private SnapshotFilters filters;
    private RequestFields sortFields;
    private RequestFields captureFields;
    boolean download;

    @JsonCreator
    public SnapshotDefinition(@JsonProperty("filters") SnapshotFilters filters ,
                              @JsonProperty("sortFields") RequestFields sortFields,
                              @JsonProperty("captureFields") RequestFields captureFields,
                              @JsonProperty("download") boolean download) {
        this.filters = filters;
        this.sortFields = sortFields;
        this.captureFields = captureFields;
        if (captureFields == null) {
            this.captureFields = new RequestFields(Arrays.asList("url", "method"));
        }
        this.download = download;
    }

    public SnapshotFilters getFilters() { return filters; }

    public RequestFields getSortFields() { return sortFields; }

    public RequestPatternBuilder createRequestPatternBuilderFrom(LoggedRequest request) {
        return this.captureFields.createRequestPatternBuilderFrom(request);
    }

    public boolean isDownload() { return download; }
}
