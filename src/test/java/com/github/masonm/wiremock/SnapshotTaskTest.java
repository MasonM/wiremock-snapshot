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
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
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
        "            \"A\": { \"equalTo\": \"B\" }         \n" +
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

    private static final String CAPTURE_HEADERS_SNAPSHOT_REQUEST =
        "{                                  \n" +
        "    \"outputFormat\": \"full\",    \n" +
        "    \"captureHeaders\": {          \n" +
        "        \"Accept\": {              \n" +
        "            \"anything\": true     \n" +
        "        },                         \n" +
        "        \"X-NoMatch\": {           \n" +
        "            \"equalTo\": \"!\"     \n" +
        "        }                          \n" +
        "    }                              \n" +
        "}                                    ";

    private static final String CAPTURE_HEADERS_SNAPSHOT_RESPONSE =
        "[                                                           \n" +
        "    {                                                       \n" +
        "        \"id\" : \"57c8262a-0f2a-3c06-b82e-2d7b4e361cf0\",  \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar\",                         \n" +
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
        "        \"uuid\" : \"57c8262a-0f2a-3c06-b82e-2d7b4e361cf0\" \n" +
        "    }                                                       \n" +
        "]                                                             ";

    @Test
    public void returnsStubMappingWithCapturedHeaders() {
        setServeEvents(
            serveEvent(
                mockRequest()
                    .url("/foo/bar")
                    .method(POST)
                    .header("Accept","B")
                    .header("X-NoMatch","should be ignored"),
                response(),
                true
            )
        );
        setReturnForGetStubMapping(null);
        assertThat(
            execute(CAPTURE_HEADERS_SNAPSHOT_REQUEST),
            equalToJson(CAPTURE_HEADERS_SNAPSHOT_RESPONSE)
        );
    }

    private static final String SORTED_SNAPSHOT_REQUEST =
        "{                                  \n" +
        "    \"outputFormat\": \"full\",    \n" +
        "    \"sortFields\": [ \"url\" ],   \n" +
        "    \"captureHeaders\": {          \n" +
        "        \"X-Foo\": {               \n" +
        "            \"matches\": \".ar\"   \n" +
        "        }                          \n" +
        "    }                              \n" +
        "}                                    ";

    private static final String SORTED_SNAPSHOT_RESPONSE =
        "[                                                                \n" +
        "    {                                                            \n" +
        "        \"id\" : \"c10fff25-486c-3c1d-9f29-66579377d14e\",       \n" +
        "        \"request\" : {                                          \n" +
        "            \"url\" : \"/a\",                                    \n" +
        "            \"method\" : \"POST\"                                \n" +
        "        },                                                       \n" +
        "        \"response\" : {                                         \n" +
        "            \"status\" : 200                                     \n" +
        "        },                                                       \n" +
        "        \"uuid\" : \"c10fff25-486c-3c1d-9f29-66579377d14e\"      \n" +
        "    },                                                           \n" +
        "    {                                                            \n" +
        "        \"id\" : \"40bd9ca3-18c9-3723-b3b9-bf0cdc214e51\",       \n" +
        "        \"request\" : {                                          \n" +
        "            \"url\" : \"/b\",                                    \n" +
        "            \"method\" : \"GET\",                                \n" +
        "            \"headers\" : {                                      \n" +
        "                \"X-Foo\" : {                                    \n" +
        "                    \"equalTo\" : \"bar\"                        \n" +
        "                }                                                \n" +
        "            }                                                    \n" +
        "        },                                                       \n" +
        "        \"response\" : {                                         \n" +
        "            \"status\" : 200                                     \n" +
        "        },                                                       \n" +
        "        \"uuid\" : \"40bd9ca3-18c9-3723-b3b9-bf0cdc214e51\"      \n" +
        "    },                                                           \n" +
        "    {                                                            \n" +
        "        \"id\" : \"dc234a23-a4c5-31dc-96f1-6913f6e8527a\",       \n" +
        "        \"request\" : {                                          \n" +
        "            \"url\" : \"/z\",                                    \n" +
        "            \"method\" : \"GET\"                                 \n" +
        "        },                                                       \n" +
        "        \"response\" : {                                         \n" +
        "            \"status\" : 200                                     \n" +
        "        },                                                       \n" +
        "        \"uuid\" : \"dc234a23-a4c5-31dc-96f1-6913f6e8527a\"      \n" +
        "    }                                                            \n" +
        "]                                                                  ";

    @Test
    public void returnsSortedStubMappings() {
        setServeEvents(
            serveEvent(
                mockRequest().method(GET).url("/b").header("X-Foo", "bar"),
                response(),
                true
            ),
            serveEvent(
                mockRequest().method(POST).url("/a").header("X-Foo", "no match"),
                response(),
                true
            ),
            serveEvent(
                mockRequest().method(GET).url("/z"),
                response(),
                true
            )
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
