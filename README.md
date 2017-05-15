# Overview

wiremock-snapshot is an admin extension for [WireMock](http://wiremock.org) that adds a new endpoint, `/_admin/snapshot`, for creating stub mappings from recorded requests. It's an alternative to the
[Record and Playback](http://wiremock.org/docs/record-playback/) feature that doesn't require
restarting the server, and provides more customization options.

WARNING: This is currently alpha. Backwards compatibility is not guaranteed.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle fatJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Standalone server:
```
java -jar build/libs/wiremock-snapshot-standalone-0.1a.jar
```

With WireMock standalone JAR:
```
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

The `__admin/snapshot` endpoint accepts POST requests with following optional parameters:
* `"filters"` - Array of request patterns to filter by.
  * Possible values: Identical to those accepted by the `__admin/requests/find`. See [Request Matching](http://wiremock.org/docs/request-matching/) for details.
  * Default: no filtering.
* `"sortFields"` - Array of fields in the request to use for sorting stub mappings.
  * Possible values:  `"url"`, `"method"`, or a header name (e.g. `"Accept"`)
  * Default: no sorting.
  * Examples: `["url", "Accept-Encoding"]`, `["url"]`, `["url", "method", "Host"]`
* `"captureFields"` - Array of fields in the request to include in stub mappings.  Any duplicate stub mappings will be skipped
  * Possible values: Same as `"sortFields"`
  * Default: `["url", "method"]`
* `"outputFormat"` - Determines response body.
  * Possible values: `"ids"` to return aray of stub mapping IDs, `"full"` to return array of stub mapping objects
  * Default: `"ids"`

# Examples

* Record mappings with defaults: `curl -d '{}' http://localhost:8080/__admin/snapshot`

# Todo

* Intelligent de-duplication/consolidation of stub mappings
* Allow filtering by proxied requests
* More field options for `captureFields` and `sortFields`
* Add ability to extract response body to file (will give feature-parity with "Record and Playback")
* Add more output formats (e.g. "zip")
