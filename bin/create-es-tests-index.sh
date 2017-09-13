#!/usr/bin/env bash

export ES_HOST="http://127.0.0.1:9200"
export ES_INDEX_NAME="http_source_tests_v1"
export ES_INDEX_ALIAS_NAME="http_source_tests"
export INDEX_CONF_FILE_PATH="elasticsearch/src/main/resources/http_source_tests.json"


curl -XDELETE "$ES_HOST/$ES_INDEX_NAME"

curl -XPUT "$ES_HOST/$ES_INDEX_NAME" -d @${INDEX_CONF_FILE_PATH}

curl -XPUT "$ES_HOST/$ES_INDEX_NAME/_alias/$ES_INDEX_ALIAS_NAME"