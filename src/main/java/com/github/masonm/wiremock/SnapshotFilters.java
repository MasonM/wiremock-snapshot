package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Predicate;

public class SnapshotFilters implements Predicate<ServeEvent> {
    @JsonUnwrapped
    private final RequestPattern filters;

    @JsonCreator
    public SnapshotFilters(@JsonProperty("filters") RequestPattern filters) {
        this.filters = filters;
    }

    @Override
    public boolean apply(ServeEvent serveEvent) {
        return filters
            .match(serveEvent.getRequest())
            .isExactMatch();
    }
}
