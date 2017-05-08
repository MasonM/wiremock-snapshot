package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.google.common.collect.FluentIterable.from;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static java.net.HttpURLConnection.HTTP_OK;

public class SnapshotTask implements AdminTask {
    private IdGenerator idGenerator;

    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
        //idGenerator = new VeryShortIdGenerator();
        final Snapshot snapshot = Json.read(request.getBodyAsString(), Snapshot.class);

        Iterable<ServeEvent> requests = admin.getServeEvents().getRequests();

        if (snapshot.getFilters() != null) {
            requests = Iterables.filter(requests, snapshot.getFilters());
        }

        Iterable<StubMapping> stubMappings = Iterables.transform(requests, toStubMappings());

        if (snapshot.getSortFields() != null) {
            stubMappings = ImmutableSortedSet.copyOf(snapshot.getSortFields(), stubMappings);
        } else {
            stubMappings = ImmutableSet.copyOf(stubMappings);
        }

        ResponseDefinitionBuilder response = responseDefinition()
                .withStatus(HTTP_OK)
                .withHeader("Content-Type", "application/json");

        return response
                .withBody(Json.write(stubMappings))
                .build();
    }

    private Function<ServeEvent, StubMapping> toStubMappings() {
        return new Function<ServeEvent, StubMapping>() {
            public StubMapping apply(ServeEvent event) {
                RequestPattern request = buildRequestPatternFrom(event.getRequest())
                ResponseDefinition response = buildResponseDefinitionFrom(event.getResponse())
                return new StubMapping(request, response);
            }

        };
    }

    private RequestPattern buildRequestPatternFrom(LoggedRequest request) {
        RequestPatternBuilder builder = newRequestPattern(request.getMethod(), urlEqualTo(request.getUrl()));

        if (!headersToMatch.isEmpty()) {
            for (HttpHeader header: request.getHeaders().all()) {
                if (headersToMatch.contains(header.caseInsensitiveKey())) {
                    builder.withHeader(header.key(), equalTo(header.firstValue()));
                }
            }
        }

        String body = request.getBodyAsString();
        if (!body.isEmpty()) {
            builder.withRequestBody(valuePatternForContentType(request));
        }

        return builder.build();
    }

    private ResponseDefinition buildResponseDefinitionFrom(LoggedResponse response) {
        ResponseDefinitionBuilder responseDefinitionBuilder = responseDefinition()
                .withStatus(response.getStatus())
                .withBodyFile(bodyFileName);
        if (response.getHeaders().size() > 0) {
            responseDefinitionBuilder.withHeaders(withoutContentEncodingAndContentLength(response.getHeaders()));
        }

        return responseDefinitionBuilder.build();
    }
}
