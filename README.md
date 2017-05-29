# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-snapshot.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-snapshot)

wiremock-snapshot is an admin extension for [WireMock](http://wiremock.org) that adds a new endpoint, `/__admin/snapshot`, for creating stub mappings from recorded requests. It's an alternative to the
[Record and Playback](http://wiremock.org/docs/record-playback/) feature that doesn't require
restarting the server, and provides more customization options.

WARNING: This is currently alpha. Backwards compatibility is not guaranteed.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle fatJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Standalone server:
```sh
java -jar build/libs/wiremock-snapshot-standalone-0.1a.jar
```

With WireMock standalone JAR:
```sh
java \
        -cp wiremock-standalone.jar:build/libs/wiremock-snapshot-0.1a.jar \
        com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
        --extensions="com.github.masonm.wiremock.SnapshotExtension"
```

Programmatically in Java:
```java
new WireMockServer(wireMockConfig()
    .extensions("com.github.masonm.wiremock.SnapshotExtension"))
```

# Usage

## Creating proxy for recording

If you're using this as a replacement for the [Record and Playback](http://wiremock.org/docs/record-playback/) feature, you'll need to manually create the proxy mapping that's normally done automatically with the `--proxy-all` option. This can be done with by calling `/__admin/mappings` with the following stub mapping:

```sh
curl -d '{
    "response": {
        "proxyBaseUrl": "http://www.example.com"
    }
}' http://localhost:8080/__admin/mappings
```

Replace `http://www.example.com` with the proxy base URL and `http://localhost:8080` with the Wiremock base URL.

## Calling the Snapshot API

The `/__admin/snapshot` endpoint can be accessed via POST and creates stub mappings from the requests and responses in the request journal. It accepts the following options:
* `"filters"` - Request patterns to use for determining which requests for which to create stub mappings.
  * Possible values: Identical to those accepted by `/__admin/requests/find`. See [Request Matching](http://wiremock.org/docs/request-matching/) for details.
  * Default: no filtering.
* `"sortFields"` - Array of fields in the request to use for sorting stub mappings, mainly for output.
  * Possible values:  `"url"`, `"method"`, or a header name (e.g. `"Accept"`)
  * Default: no sorting.
* `"requestTemplate"` - Changes the request matcher in the stub mapping to only contain the fields in the given request matcher object. Each field in a request is evaluated against the matcher and only included in the stub mapping if there's a match.
  * Possible values: [Request matchers](http://wiremock.org/docs/request-matching/) for `"url"`, `"urlPath"`, `"urlPattern"`, `"urlPathPattern"`, `"method"`, and `"headers"`
  * Default: `{ "urlPattern": ".*", "method": "ANY" }` (i.e. the `"url"` and `"method`" are always included in stub mappings)
  * Example: `{ "urlPattern": ".*foo", "method": "GET" }` means the stub mapping will only match against the `"url"` if the request URL matches `.*foo` and `"method"` if the request is `GET`. So if the request is `GET /foo`, then the stub mapping will have `{ "url": /foo", "method": "GET" }`. If the request is `POST /bar/foo`, then the stub mapping will have `{ "request": { "url": "/bar/foo" } }`.
* `"outputFormat"` - Determines response body.
  * Possible values: `"ids"` to return array of stub mapping IDs, `"full"` to return array of stub mapping objects
  * Default: `"ids"`

## Persisting the mappings

Stub mappings are not persisted automatically. Call `/__admin/mappings/save` to save them:

    curl -X POST http://localhost:8080/__admin/mappings/save
    
# Examples

* Record mappings with defaults: `curl -d '{}' http://localhost:8080/__admin/snapshot`
* Filter by URL and header values (i.e. only create stub mappings for matching requests) and ouput array of stub mappings:

        curl -d '{
            "outputFormat": "full",
            "filters": {
                "urlPattern": "/foo/(bar|baz)",
                "headers": {
                    "Content-Type": {
                        "equalTo": "application/json"
                    }
                }
            }
        }' http://localhost:8080/__admin/snapshot`
* Only include URL in the stub mapping if it matches `"/foo"`, always include the method, always include the header `Content-Type`, and only include the header `Accept` if it's equal to "bar".

         curl -d '{
            "requestTemplate": {
                "url": "/foo",
                "method": "ANY",
                "headers": {
                    "Content-Type": { "anything": true },
                    "Accept": { "equalTo": "Bar" }
                 }
            }
         }' http://localhost:8080/__admin/snapshot`
* Sort stub mappings by the URL and output an array IDs.

         curl -d '{
            "sortFields": [ "url" ],
            "outputFormat": "ids"
         }' http://localhost:8080/__admin/snapshot`
# Todo

* [Add ability to extract response body to file](https://github.com/MasonM/wiremock-snapshot/issues/1) (will give feature-parity with "Record and Playback")
* Intelligent de-duplication/consolidation of stub mappings
