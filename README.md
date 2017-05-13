# Overview

wiremock-snapshot is an admin extension for [Wiremock](http://wiremock.org) that adds a new endpoint, `/_admin/snapshot`, for creating stub mappings from recorded requests. It's an alternative to the
[Record and Playback](http://wiremock.org/docs/record-playback/) feature that doesn't require
restarting the server, and provides more customization options.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle farJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Run standalone server:
```
java -jar build/libs/wiremock-snapshot-standalone-0.1.jar
```

Run regular JAR with Wiremock standalone JAR:
```
java \
        -cp wiremock-standalone.jar:build/libs/wiremock-snapshot-0.1.jar \
        com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
        --extensions="com.github.masonm.wiremock.SnapshotExtension"
```

# Usage

To record mappings with the defaults, run `curl -d '{}' 'http://localhost:8080/__admin/snapshot'`
