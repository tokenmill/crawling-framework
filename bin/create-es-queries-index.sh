export ES_HOST="http://localhost:9200"

curl -XDELETE "$ES_HOST/named_queries_v1"

curl -XPUT "$ES_HOST/named_queries_v1" -d '
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "named_query": {
      "_source": {
        "enabled": true
      },
      "properties": {
        "updated": {
          "type": "date",
          "format": "date_optional_time",
          "index": "not_analyzed"
        },
        "name": {
          "type": "string",
          "index": "not_analyzed"
        },
        "name_suggest": {
            "type": "completion"
        },
        "stemmed_case_sensitive": {
          "type": "string",
          "index": "not_analyzed"
        },
        "stemmed_case_insensitive": {
          "type": "string",
          "index": "not_analyzed"
        },
        "not_stemmed_case_sensitive": {
          "type": "string",
          "index": "not_analyzed"
        },
        "not_stemmed_case_insensitive": {
          "type": "string",
          "index": "not_analyzed"
        },
        "advanced": {
          "type": "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'

curl -XPOST "$ES_HOST/_aliases" -d '
{
    "actions" : [
        { "add" : { "index" : "named_queries_v1", "alias" : "named_queries" } }
    ]
}'