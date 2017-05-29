#!/bin/sh

# Basic test script

set -e

#echo "Launching Wiremock and setting up proxying"
#python -m SimpleHTTPServer &
#java -jar build/libs/wiremock-snapshot-standalone-*.jar &

curl -s -d '{
    "request": { "urlPattern": ".*" },
    "response": {
	"proxyBaseUrl": "http://localhost:8000"
    }
}' http://localhost:8080/__admin/mappings

echo "Making request to LICENSE and README.md"
curl -s http://localhost:8080/LICENSE > /dev/null
curl -s http://localhost:8080/README.md > /dev/null

echo "Calling snapshot API. Should return two stub mappings:"
#curl -s -X POST http://localhost:8080/__admin/snapshot | jq
curl -s -d '{ "outputFormat": "full" }' http://localhost:8080/__admin/snapshot | jq
