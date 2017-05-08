package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Predicate;

public class Snapshot {
    private SnapshotFilters filters;
    private SnapshotMatchFields sortFields;
    private SnapshotMatchFields matchFields;
    boolean download;

    @JsonCreator
    public Snapshot(@JsonProperty("filters") SnapshotFilters filters ,
                    @JsonProperty("sortFields") SnapshotMatchFields sortFields,
                    @JsonProperty("matchFields") SnapshotMatchFields matchFields,
                    @JsonProperty("download") boolean download) {
        this.filters = filters;
        this.sortFields = sortFields;
        this.matchFields = matchFields;
        this.download = download;
    }

    public SnapshotFilters getFilters() { return filters; }

    public SnapshotMatchFields getSortFields() { return sortFields; }

    public SnapshotMatchFields getMatchFields() { return matchFields; }

    public boolean isDownload() { return download; }
}
