package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.FluentIterable;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;

public class SnapshotTask implements AdminTask {
    private IdGenerator idGenerator;

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        //idGenerator = new VeryShortIdGenerator();
        final SnapshotDefinition snapshotDefinition = Json.read(request.getBodyAsString(), SnapshotDefinition.class);

        FluentIterable<ServeEvent> serveEvents = from(admin.getServeEvents().getRequests());
        if (snapshotDefinition.getFilters() != null) {
            serveEvents = serveEvents.filter(snapshotDefinition.getFilters());
        }

        FluentIterable<StubMapping> stubMappings = serveEvents.transform(new SnapshotStubMappingTransformer(snapshotDefinition));
        if (snapshotDefinition.getSortFields() != null) {
            stubMappings = from(stubMappings.toSortedSet(snapshotDefinition.getSortFields()));
        }

        ResponseDefinitionBuilder response = responseDefinition()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json");

        return response
                .withBody(Json.write(stubMappings))
                .build();
    }
}
