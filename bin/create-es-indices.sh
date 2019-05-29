bin/create-es-index.sh docs document.json ${1:-localhost} $2
bin/create-es-index.sh named_queries query.json ${1:-localhost} $2
bin/create-es-index.sh http_sources http_source.json ${1:-localhost} $2
bin/create-es-index.sh http_source_tests http_source_test.json ${1:-localhost} $2
bin/create-es-index.sh urls url.json ${1:-localhost} $2
