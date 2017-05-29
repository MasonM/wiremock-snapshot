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

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;


public class SnapshotTask implements AdminTask {
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        SnapshotSpec snapshotSpec = Json.read(request.getBodyAsString(), SnapshotSpec.class);
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
        final FluentIterable<StubMapping> stubMappings =
            generateStubMappings(admin.getServeEvents(), snapshotSpec)
            .filter(noDupes(admin));

        final ArrayList<Object> response = new ArrayList<>(stubMappings.size());
        final String format = snapshotSpec.getOutputFormat();

        for (StubMapping stubMapping : stubMappings) {
            if (snapshotSpec.shouldPersist()) {
                stubMapping.setPersistent(true);
                admin.addStubMapping(stubMapping);
            }
            response.add((format != null && format.equals("full")) ? stubMapping : stubMapping.getId());
        }

        return jsonResponse(response.toArray(), HTTP_OK);
    }

    /**
     * Transforms a list of ServeEvents to StubMappings according to the options in SnapshotSpec
     * @param serveEventResult List of ServeEvents from the request journal
     * @param snapshotSpec User input parameters/options
     * @return List of StubMappings
     */
    private FluentIterable<StubMapping> generateStubMappings(GetServeEventsResult serveEventResult, SnapshotSpec snapshotSpec) {
        FluentIterable<ServeEvent> serveEvents = from(serveEventResult.getServeEvents()).filter(onlyProxied());

        if (snapshotSpec.getFilters() != null) {
            serveEvents = serveEvents.filter(snapshotSpec.getFilters());
        }

        FluentIterable<StubMapping> stubMappings = serveEvents.transform(
            new SnapshotStubMappingTransformer(snapshotSpec.getCaptureHeaders())
        );

        if (snapshotSpec.getSortFields() != null) {
            stubMappings = from(stubMappings.toSortedSet(snapshotSpec.getSortFields()));
        }

        return stubMappings;
    }

    private Predicate<StubMapping> noDupes(final Admin admin) {
        return new Predicate<StubMapping>() {
            @Override
            public boolean apply(StubMapping stubMapping) {
                return !admin.getStubMapping(stubMapping.getId()).isPresent();
            }
        };
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
