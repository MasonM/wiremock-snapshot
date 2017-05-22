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
        setReturnForGetStubMapping(StubMapping.NOT_CONFIGURED);
        assertEquals("[ ]", execute("{}"));
    }

    @Test
    public void returnsOneMappingWithOneServeEvent() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setReturnForGetStubMapping(null);
        // the UUID shouldn't change, as it's based on the hash of the request and response
        assertThat(execute("{}"), equalToJson("[\"19652ad8-cad8-3b2d-9846-05e6a790fbfb\"]"));
    }

    @Test
    public void returnsTwoMappingsForTwoServeEvents() {
        setServeEvents(
            serveEvent(mockRequest(), response(), true),
            serveEvent(mockRequest().url("/foo"), response(), true)
        );
        setReturnForGetStubMapping(null);
        assertThat(
            execute("{}"),
            equalToJson("[\"19652ad8-cad8-3b2d-9846-05e6a790fbfb\", \"257aa42c-75c2-3a8e-8688-f137d75847c7\"]")
        );
    }

    private static final String FILTERED_SNAPSHOT_REQUEST =
        "{                                                 \n" +
        "    \"outputFormat\": \"full\",                   \n" +
        "    \"filters\": {                                \n" +
        "        \"urlPattern\": \"/foo.*\",               \n" +
        "        \"headers\": {                            \n" +
        "            \"A\": { \"equalTo\": \"B\" }         \n"+
        "        }                                         \n" +
        "    }                                             \n" +
        "}                                                   ";

    private static final String FILTERED_SNAPSHOT_RESPONSE =
        "[                                                           \n" +
        "    {                                                       \n" +
        "        \"id\" : \"dad048a8-d4ce-3302-bdef-ff2f4c4620ce\",  \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar\",                         \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        },                                                  \n" +
        "        \"uuid\" : \"dad048a8-d4ce-3302-bdef-ff2f4c4620ce\" \n" +
        "    },                                                      \n" +
        "    {                                                       \n" +
        "        \"id\" : \"749570ee-baf9-39cb-a91f-6f4b66f9508a\",  \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar/baz\",                     \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        },                                                  \n" +
        "        \"uuid\" : \"749570ee-baf9-39cb-a91f-6f4b66f9508a\" \n" +
        "    }                                                       \n" +
        " ]                                                            ";

    @Test
    public void returnsFilteredRequestsWithFullOutputFormat() {
        setServeEvents(
            // Matches both
            serveEvent(mockRequest().url("/foo/bar").header("A","B"), response(), true),
            // Fails header match
            serveEvent(mockRequest().url("/foo"), response(), true),
            // Fails URL match
            serveEvent(mockRequest().url("/bar").header("A", "B"), response(), true),
            // Fails header match
            serveEvent(mockRequest().url("/foo/").header("A", "C"), response(), true),
            // Matches both
            serveEvent(mockRequest().url("/foo/bar/baz").header("A","B"), response(), true)
        );
        setReturnForGetStubMapping(null);
        assertThat(execute(FILTERED_SNAPSHOT_REQUEST), equalToJson(FILTERED_SNAPSHOT_RESPONSE));
    }

    private static final String CAPTURE_FIELDS_SNAPSHOT_RESPONSE =
            "[                                                           \n" +
            "    {                                                       \n" +
            "        \"id\" : \"a29b49ea-a0fc-3fa6-8367-ef3be2d9bfd9\",  \n" +
            "        \"request\" : {                                     \n" +
            "            \"method\" : \"POST\",                          \n" +
            "            \"headers\": {                                  \n" +
            "                \"Accept\": {                               \n" +
            "                    \"equalTo\": \"B\"                      \n" +
            "                }                                           \n" +
            "            }                                               \n" +
            "        },                                                  \n" +
            "        \"response\" : {                                    \n" +
            "            \"status\" : 200                                \n" +
            "        },                                                  \n" +
            "        \"uuid\" : \"a29b49ea-a0fc-3fa6-8367-ef3be2d9bfd9\" \n" +
            "    }                                                       \n" +
            "]                                                             ";

    @Test
    public void returnsStubMappingWithCaptureFields() {
        setServeEvents(
            serveEvent(
                mockRequest()
                    .url("/foo/bar")
                    .method(RequestMethod.POST)
                    .header("Accept","B"),
                response(),
                true
            )
        );
        setReturnForGetStubMapping(null);
        assertThat(
            execute("{ " +
                "\"outputFormat\": \"full\", " +
                "\"captureFields\": [ \"method\", \"Accept\" ] " +
            "}"),
            equalToJson(CAPTURE_FIELDS_SNAPSHOT_RESPONSE)
        );
    }

    private static final String SORTED_SNAPSHOT_REQUEST =
        "{                                  \n" +
        "    \"outputFormat\": \"full\",    \n" +
        "    \"sortFields\": [ \"url\" ],   \n" +
        "    \"captureFields\": [ \"url\" ] \n" +
        "}                                  \n";

    private static final String SORTED_SNAPSHOT_RESPONSE =
            "[                                                                \n" +
            "    {                                                            \n" +
            "        \"id\" : \"0bb32d87-039c-3a23-a611-11977b18f835\",       \n" +
            "        \"request\" : { \"method\": \"ANY\", \"url\" : \"/a\" }, \n" +
            "        \"response\" : { \"status\" : 200 },                     \n" +
            "        \"uuid\" : \"0bb32d87-039c-3a23-a611-11977b18f835\"      \n" +
            "    },                                                           \n" +
            "    {                                                            \n" +
            "        \"id\" : \"ad6afa05-dc87-3925-9578-cb8a78e2e15e\",       \n" +
            "        \"request\" : { \"method\": \"ANY\", \"url\" : \"/b\" }, \n" +
            "        \"response\" : { \"status\" : 200 },                     \n" +
            "        \"uuid\" : \"ad6afa05-dc87-3925-9578-cb8a78e2e15e\"      \n" +
            "    },                                                           \n" +
            "    {                                                            \n" +
            "        \"id\" : \"d17d08cf-96da-37f5-b40b-71420c7f2940\",       \n" +
            "        \"request\" : { \"method\": \"ANY\", \"url\" : \"/z\" }, \n" +
            "        \"response\" : { \"status\" : 200 },                     \n" +
            "        \"uuid\" : \"d17d08cf-96da-37f5-b40b-71420c7f2940\"      \n" +
            "    }                                                            \n" +
            "]                                                             ";

    @Test
    public void returnsSortedStubMappings() {
        setServeEvents(
            serveEvent(mockRequest().url("/b"), response(), true ),
            serveEvent(mockRequest().url("/a"), response(), true ),
            serveEvent(mockRequest().url("/z"), response(), true )
        );
        setReturnForGetStubMapping(null);
        assertThat(execute(SORTED_SNAPSHOT_REQUEST), equalToJson(SORTED_SNAPSHOT_RESPONSE));
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

    private void setReturnForGetStubMapping(final StubMapping stubMapping) {
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
