#!/bin/bash

# Basic test script. Supply JSON string with parameters.

set -e

PROXY_BASE_URL="http://localhost:8000"
REQUEST_JSON=$1

echo "Launching Wiremock and setting up proxying"
python -m SimpleHTTPServer 1>/dev/null 2>/dev/null &
PYTHON_PID=$!
java -jar build/libs/wiremock*standalone*.jar 1>/dev/null 2>/dev/null & 
WIREMOCK_PID=$!
trap "kill $PYTHON_PID $WIREMOCK_PID" exit


echo -n "Waiting for Wiremock to start up."
until $(curl --output /dev/null --silent --head http://localhost:8080); do
	echo -n '.'
	sleep 1
done


echo -e "done\nCreating proxy mapping"
curl -s -d '{
	"request": { "urlPattern": ".*" },
	"response": {
		"proxyBaseUrl": "'${PROXY_BASE_URL}'"
	}
}' http://localhost:8080/__admin/mappings > /dev/null


echo "Making requests"
curl -X POST -s http://localhost:8080/build.gradle > /dev/null
curl -s http://localhost:8080/README.md > /dev/null
curl -s http://localhost:8080/LICENSE > /dev/null
curl -s http://localhost:8080/README.md > /dev/null
curl -s http://localhost:8080/README.md > /dev/null


echo "Calling snapshot API with '${REQUEST_JSON}'"
./test-snapshot-request.sh "${REQUEST_JSON}"
