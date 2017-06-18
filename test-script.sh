#!/bin/bash

# Basic test script. Supply JSON string with parameters.

set -e

echo "Launching Wiremock and setting up proxying"

python -m SimpleHTTPServer 1>/dev/null 2>/dev/null &
PYTHON_PID=$!
java -jar build/libs/wiremock-snapshot-standalone-*.jar 1>/dev/null 2>/dev/null & 
WIREMOCK_PID=$!

echo -n "Waiting for Wiremock to start up."

until $(curl --output /dev/null --silent --head http://localhost:8080); do
   echo -n '.'
   sleep 5
done

echo -e "done\nCreating proxy mapping"

curl -s -d '{
   "request": { "urlPattern": ".*" },
   "response": {
      "proxyBaseUrl": "http://localhost:8000"
    }
}' http://localhost:8080/__admin/mappings > /dev/null

echo "Making requests"
curl -X POST -s http://localhost:8080/src/main/java/com/github/masonm/wiremock/SnapshotExtension.java > /dev/null
curl -s http://localhost:8080/README.md > /dev/null
curl -s http://localhost:8080/LICENSE > /dev/null
curl -s http://localhost:8080/README.md > /dev/null
curl -s http://localhost:8080/README.md > /dev/null

JSON=$1
if [[ -z $JSON ]]; then
   JSON='{ "outputFormat": "full", "persist": false, "repeatsAsScenarios": true }'
fi
echo "Calling snapshot API with '${JSON}'"
curl -s -X POST -d "${JSON}" http://localhost:8080/__admin/recordings/snapshot | jq

kill $PYTHON_PID $WIREMOCK_PID
