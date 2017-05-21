package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.PaginatedResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class SnapshotTaskTest {
    private Mockery context;
    private Admin mockAdmin;

    @Before
    public void init() {
        context = new Mockery();
        mockAdmin = context.mock(Admin.class);
    }

    @Test
    public void returnsEmptyArrayWithNoServeEvents() {
        setServeEvents();
        assertEquals("[ ]", execute("{}"));
    }

    @Test
    public void returnsEmptyArrayWithUnproxiedServeEvent() {
        setServeEvents(serveEvent(mockRequest(), response(), false));
        assertEquals("[ ]", execute("{}"));
    }

    @Test
    public void returnsEmptyArrayForExistingStubMapping() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setGetStubMappingMock(true);
        assertEquals("[ ]", execute("{}"));
    }

    @Test
    public void returnsOneMappingWithOneServeEvent() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setGetStubMappingMock(false);
        assertThat(execute("{}"), equalToJson("[\"19652ad8-cad8-3b2d-9846-05e6a790fbfb\"]"));
    }

    private String execute(String requestBody) {
        Request request = mockRequest().body(requestBody);

        SnapshotTask snapshotTask = new SnapshotTask();
        ResponseDefinition responseDefinition = snapshotTask.execute(mockAdmin, request, PathParams.empty());

        assertEquals(HTTP_OK, responseDefinition.getStatus());
        assertEquals("application/json", responseDefinition.getHeaders().getContentTypeHeader().firstValue());

        return responseDefinition.getBody();
    }

    private void setServeEvents(ServeEvent... serveEvents) {
        final GetServeEventsResult results = new GetServeEventsResult(
            Arrays.asList(serveEvents),
            new PaginatedResult.Meta(0), // ignored parameter
            false
        );
        context.checking(new Expectations() {{
            allowing(mockAdmin).getServeEvents(); will(returnValue(results));
            allowing(mockAdmin).addStubMapping(with(any(StubMapping.class)));
        }});
    }

    private void setGetStubMappingMock(boolean isPresent) {
        final StubMapping stubMapping = isPresent ? StubMapping.NOT_CONFIGURED : null;
        context.checking(new Expectations() {{
            allowing(mockAdmin).getStubMapping(with(any(UUID.class)));
            will(returnValue(new SingleStubMappingResult(stubMapping)));
        }});
    }

    private static ServeEvent serveEvent(Request request, Response.Builder responseBuilder, boolean wasProxied) {
        ResponseDefinitionBuilder responseDefinition = responseDefinition();
        if (wasProxied) {
            responseDefinition.proxiedFrom("/foo");
        }
        return new ServeEvent(
            UUID.randomUUID(),
            LoggedRequest.createFrom(request),
            null,
            responseDefinition.build(),
            LoggedResponse.from(responseBuilder.build()),
            false
        );
    }
}
