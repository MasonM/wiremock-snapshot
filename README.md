# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-snapshot.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-snapshot)

wiremock-snapshot is an admin extension for [WireMock](http://wiremock.org) that adds a new endpoint, `/_admin/snapshot`, for creating stub mappings from recorded requests. It's an alternative to the
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

If you're using this as a replacement for the [Record and Playback](http://wiremock.org/docs/record-playback/) feature, you'll need to manually create the proxy mapping that's normally done automatically with the `--proxy-all` option. This can be done with by calling the `_admin/snapshots/` endpoint with the following stub mapping:

```sh
curl -d '{
    "response": {
        "proxyBaseUrl": "http://www.example.com"
    }
}' http://localhost:8080/__admin/mappings
```

Replace `http://www.example.com` with the proxy base URL, and `http://localhost:8080` with the Wiremock hostname.

## Calling the Snapshot API

The `__admin/snapshot` endpoint can be accessed via POST and creates stub mappings from the requests and responses in the request journal. It accepts the following options:
* `"filters"` - Array of request patterns to use for determining which requests to create stub mappings for.
  * Possible values: Identical to those accepted by the `__admin/requests/find`. See [Request Matching](http://wiremock.org/docs/request-matching/) for details.
  * Default: no filtering.
* `"sortFields"` - Array of fields in the request to use for sorting stub mappings, mainly for output.
  * Possible values:  `"url"`, `"method"`, or a header name (e.g. `"Accept"`)
  * Default: no sorting.
* `"captureFields"` - Array of fields in the request to include in stub mappings. Any duplicate stub mappings will be skipped.
  * Possible values: Same as `"sortFields"`
  * Default: `["url", "method"]`
* `"outputFormat"` - Determines response body.
  * Possible values: `"ids"` to return aray of stub mapping IDs, `"full"` to return array of stub mapping objects
  * Default: `"ids"`

## Persisting the mappings

Stub mappings are not persisted automatically. Call `/__admin/mappings/save` to save them:

    curl -X POST http://localhost:8080/__admin/mappings/save
    
# Examples

* Record mappings with defaults: `curl -d '{}' http://localhost:8080/__admin/snapshot`
* Filter by URL and header values (i.e. only create stub mappings for mathing requests) and ouput array of stub mappings:

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
* Only include URL and the Content-Type header in stub mappings, and sort output by URL:

         curl -d '{
            "captureFields": [ "url", "Content-Type" ],
            "sortFields": [ "url" ]
         }' http://localhost:8080/__admin/snapshot`
# Todo

* [Add ability to extract response body to file](https://github.com/MasonM/wiremock-snapshot/issues/1) (will give feature-parity with "Record and Playback")
* Intelligent de-duplication/consolidation of stub mappings
* More field options for `captureFields` and `sortFields`?
