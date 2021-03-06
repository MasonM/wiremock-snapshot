# DEPRECATED
An improved version of this extension [was integrated into Wiremock 2.7](https://github.com/tomakehurst/wiremock/pull/674), so it's no longer needed.

# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-snapshot.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-snapshot)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-snapshot/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-snapshot)

wiremock-snapshot is an admin extension for [WireMock](http://wiremock.org) that adds a new endpoint, `/__admin/recordings/snapshot`, for creating stub mappings from recorded requests. It's an alternative to the
[Record and Playback](http://wiremock.org/docs/record-playback/) feature that doesn't require
restarting the server, and provides more customization options.

WARNING: This is currently alpha. Backwards compatibility is not guaranteed.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle fatJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Standalone server:
```sh
java -jar build/libs/wiremock-snapshot-standalone-0.3a.jar
```

With WireMock standalone JAR:
```sh
java \
        -cp wiremock-standalone.jar:build/libs/wiremock-snapshot-0.3a.jar \
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

The `/__admin/recordings/snapshot` endpoint can be accessed via POST and creates stub mappings from the requests and responses in the request journal. It accepts the following options:
* `"filters"` - Request patterns and IDs to use for determining which requests for which to create stub mappings.
  * Possible values: Same request patterns accepted by `/__admin/requests/find`. See [Request Matching](http://wiremock.org/docs/request-matching/) for details. Also accepts an array of IDs to match against.
  * Default: no filtering.
* `"persist"` - If set to true, persist stub mappings to disk. Otherwise, just output
  * Possible values: true, false
  * Default: true
* `"captureHeaders"` - Header matchers for including headers in the StubMapping. The request is matched against each matcher, and the associated header is added to the stub mapping if there's a match.
  * Possible values: [Request matchers](http://wiremock.org/docs/request-matching/) for headers.
  * Default: none
* `"outputFormat"` - Determines response body.
  * Possible values: `"ids"` to return array of stub mapping IDs, `"full"` to return array of stub mapping objects
  * Default: `"full"`
* `"repeatsAsScenarios"` - Whether to record duplicate requests as scenarios, or just ignore them.
  * Possible values: `true` to use scenarios for duplicate requests, `false` to discard them
  * Default: `false`

# Examples

* Record mappings with defaults: `curl -X POST http://localhost:8080/__admin/recordings/snapshot`
* Filter by URL and header values (i.e. only create stub mappings for matching requests) and output array of stub mappings:

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
        }' http://localhost:8080/__admin/recordings/snapshot`
* Filter by URL and IDs, and output array of stub mappings:

        curl -d '{
            "outputFormat": "full",
            "filters": {
		"ids": [
		    "bff18359-a74e-4c3e-95f0-dab304cd3a5a",
		    "e88ab645-69d5-34d1-8e4a-382ad56be0e4"
		],
                "urlPattern": "/foo"
            }
        }' http://localhost:8080/__admin/recordings/snapshot`
* Always include "Content-Type" header in stub mapping, and include "Accept" header if it's equal to "bar".

         curl -d '{
            "captureHeaders": {
                "url": "/foo",
                "method": "ANY",
                "headers": {
                    "Content-Type": { "anything": true },
                    "Accept": { "equalTo": "Bar" }
                 }
            }
         }' http://localhost:8080/__admin/recordings/snapshot`
* Output an array IDs, without persisting.

         curl -d '{
            "persist": false,
            "outputFormat": "ids"
         }' http://localhost:8080/__admin/recordings/snapshot`
# Todo

* [Add ability to extract response body to file](https://github.com/MasonM/wiremock-snapshot/issues/1) (will give feature-parity with "Record and Playback")
* Intelligent de-duplication/consolidation of stub mappings
