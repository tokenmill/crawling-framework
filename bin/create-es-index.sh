# $1 - type of the index (docs, http_sources)
# $2 - ES index config file name
# $3 - ES host
# $4 - application name
export INDEX_URL="http://$3:9200/$4-$1_v1"


curl -XDELETE "$INDEX_URL"
curl -XPUT "$INDEX_URL" -d @elasticsearch/src/main/resources/indices/$2
curl -XPUT "$INDEX_URL/_alias/$4-$1"
