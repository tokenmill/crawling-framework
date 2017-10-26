if [ $# -eq 2 ] ; then

  bin/create-es-index.sh docs document.json $1 $2
  bin/create-es-index.sh named_queries query.json $1 $2
  bin/create-es-index.sh http_sources http_source.json $1 $2
  bin/create-es-index.sh http_source_tests http_source_test http_source_test.json $1 $2
  bin/create-es-index.sh urls url.json $1 $2

else
  echo "Usage: create-es-indices.sh [ES-HOST] [APPLICATION-NAME]"
  echo "Example create-es-indices.sh localhost demo"
  exit 1
fi
