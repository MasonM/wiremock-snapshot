package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;

public class SnapshotTask implements AdminTask {
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        final SnapshotDefinition snapshotDefinition = Json.read(request.getBodyAsString(), SnapshotDefinition.class);

        List<ServeEvent> serveEventList = admin.getServeEvents().getRequests();
        FluentIterable<ServeEvent> serveEvents = filterEvents(serveEventList, snapshotDefinition.getFilters());

        FluentIterable<StubMapping> stubMappings = serveEvents.transform(
                new SnapshotStubMappingTransformer(snapshotDefinition.getCaptureFields())
        );
        if (snapshotDefinition.getSortFields() != null) {
            stubMappings = from(stubMappings.toSortedSet(snapshotDefinition.getSortFields()));
        }

        ArrayList<String> output = new ArrayList<>(stubMappings.size());
        for (StubMapping stubMapping : stubMappings) {
            admin.addStubMapping(stubMapping);
            output.add(renderStubMapping(snapshotDefinition.getOutputFormat(), stubMapping));
        }

        return responseDefinition()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.write(output))
                .build();
    }

    private FluentIterable<ServeEvent> filterEvents(List<ServeEvent> serveEventList, SnapshotFilters filters) {
        FluentIterable<ServeEvent> serveEvents = from(serveEventList)
                .filter(ServeEvent.NOT_MATCHED); // get only unmatched requests
        // @todo filter by LoggedRequest.isBrowserProxyRequest()
        if (filters != null) {
            serveEvents = serveEvents.filter(filters);
        }
        return serveEvents;
    }

    private String renderStubMapping(String outputFormat, StubMapping stubMapping) {
        if (outputFormat != null && outputFormat.equals("full")) {
            return Json.write(stubMapping);
        } else {
            return stubMapping.getUuid().toString();
        }
    }
}
