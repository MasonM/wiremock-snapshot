package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.PaginatedResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.IdGenerator;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.toomuchcoding.jsonassert.JsonAssertion;
import com.toomuchcoding.jsonassert.JsonVerifiable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static java.net.HttpURLConnection.HTTP_OK;
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
    public void returnsEmptyArrayWithNoServeEventsOrOptions() {
        setServeEvents();
        assertEquals("[ ]", execute("{}"));
    }

    @Test
    public void returnsOneMappingWithOneServeEvent() {
        setServeEvents(serveEvent(
            mockRequest().url("/foo").method(RequestMethod.GET),
            response().body("hello").build(),
            true
        ));

        JsonVerifiable check = JsonAssertion.assertThat(execute("{}"));
        check.hasSize(1);
        check.contains(UUID.nameUUIDFromBytes("101".getBytes()));
    }

    private String execute(String requestBody) {
        Request request = mockRequest().body(requestBody);

        SnapshotTask snapshotTask = new SnapshotTask();
        snapshotTask.setIdGenerator(fixedIdGenerator("101"));
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

    private static ServeEvent serveEvent(Request request, Response response, boolean wasProxied) {
        ResponseDefinitionBuilder responseDefinition = responseDefinition();
        if (wasProxied) {
            responseDefinition.proxiedFrom("/foo");
        }
        return new ServeEvent(
            UUID.randomUUID(),
            LoggedRequest.createFrom(request),
            null,
            responseDefinition.build(),
            LoggedResponse.from(response),
            false
        );
    }

    private IdGenerator fixedIdGenerator(final String id) {
        return new IdGenerator() {
            public String generate() {
                return id;
            }
        };
    }
}
