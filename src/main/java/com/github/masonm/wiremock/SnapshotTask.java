package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;


public class SnapshotTask implements AdminTask {
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        SnapshotSpec snapshotSpec = request.getBody().length == 0
            ? new SnapshotSpec()
            : Json.read(request.getBodyAsString(), SnapshotSpec.class);
        return execute(admin, snapshotSpec);
    }

    /**
     * Central method, mainly glue code
     *
     * @param admin Admin instance
     * @param snapshotSpec User input parameters/options
     * @return ResponseDefinition
     */
    private ResponseDefinition execute(Admin admin, SnapshotSpec snapshotSpec) {
        final Iterable<ServeEvent> serveEvents = filterServeEvents(
            admin.getServeEvents(),
            snapshotSpec.getFilters()
        );

        List<StubMapping> stubMappings = new SnapshotStubMappingGenerator(snapshotSpec.getCaptureHeaders())
            .generateFrom(serveEvents);

        stubMappings = new SnapshotRepeatedRequestHandler(snapshotSpec.shouldRecordRepeatsAsScenarios())
            .processStubMappings(stubMappings);

        final ArrayList<Object> response = new ArrayList<>();

        for (StubMapping stubMapping : stubMappings) {
            if (snapshotSpec.shouldPersist()) {
                stubMapping.setPersistent(true);
                admin.addStubMapping(stubMapping);
            }
            response.add(snapshotSpec.getOutputFormat().format(stubMapping));
        }

        return jsonResponse(response.toArray(), HTTP_OK);
    }

    private Iterable<ServeEvent> filterServeEvents(
        GetServeEventsResult serveEventsResult,
        SnapshotFilters snapshotFilters
    ) {
        FluentIterable<ServeEvent> serveEvents = from(serveEventsResult.getServeEvents())
            .filter(onlyProxied());

        if (snapshotFilters != null) {
            serveEvents = serveEvents.filter(snapshotFilters);
        }

        return serveEvents;
    }

    private Predicate<ServeEvent> onlyProxied() {
        return new Predicate<ServeEvent>() {
            @Override
            public boolean apply(ServeEvent serveEvent) {
                return serveEvent.getResponseDefinition().isProxyResponse();
            }
        };
    }
}
