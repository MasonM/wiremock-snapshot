package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.VeryShortIdGenerator;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.jsonResponse;
import static com.google.common.collect.FluentIterable.from;
import static java.net.HttpURLConnection.HTTP_OK;

public class SnapshotTask implements AdminTask {
    private IdGenerator idGenerator = new VeryShortIdGenerator();

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        final SnapshotDefinition snapshotDefinition = Json.read(request.getBodyAsString(), SnapshotDefinition.class);

        List<ServeEvent> serveEventList = admin.getServeEvents().getServeEvents();
        FluentIterable<ServeEvent> serveEvents = from(serveEventList).filter(onlyProxied());

        if (snapshotDefinition.getFilters() != null) {
            serveEvents = serveEvents.filter(snapshotDefinition.getFilters());
        }

        FluentIterable<StubMapping> stubMappings = serveEvents.transform(
            new SnapshotStubMappingTransformer(snapshotDefinition.getCaptureFields())
        );

        if (snapshotDefinition.getSortFields() != null) {
            stubMappings = from(stubMappings.toSortedSet(snapshotDefinition.getSortFields()));
        }

        for (StubMapping stubMapping : stubMappings) {
            String stubId = idGenerator.generate();
            stubMapping.setUuid(UUID.nameUUIDFromBytes(stubId.getBytes()));
            admin.addStubMapping(stubMapping);
        }

        return jsonResponse(
            stubMappings.transform(toResponse(snapshotDefinition.getOutputFormat())).toList(),
            HTTP_OK
        );
    }

    private Function<StubMapping, Object> toResponse(final String outputFormat) {
        return new Function<StubMapping, Object>() {
            @Override
            public Object apply(StubMapping stubMapping) {
                if (outputFormat != null && outputFormat.equals("full")) {
                    return stubMapping;
                } else {
                    return stubMapping.getUuid();
                }
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
