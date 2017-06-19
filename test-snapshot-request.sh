#!/bin/bash

set -e

WIREMOCK_BASE_URL="http://localhost:8080"

REQUEST_JSON=$1
if [[ -z $REQUEST_JSON ]]; then
	REQUEST_JSON='{
		"outputFormat": "full",
		"persist": false,
		"extractBodyCriteria": {
			"textSizeThreshold": "2000"
		}
	}'
fi

curl -s -X POST -d "${REQUEST_JSON}" "${WIREMOCK_BASE_URL}/__admin/recordings/snapshot"
