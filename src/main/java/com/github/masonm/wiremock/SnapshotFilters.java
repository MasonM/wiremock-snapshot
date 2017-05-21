package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Predicate;

public class SnapshotFilters implements Predicate<ServeEvent> {
    private final RequestPattern filters;

    public SnapshotFilters(RequestPattern filters) {
        this.filters = filters;
    }

    @Override
    public boolean apply(ServeEvent serveEvent) {
        return filters
            .match(serveEvent.getRequest())
            .isExactMatch();
    }
}
