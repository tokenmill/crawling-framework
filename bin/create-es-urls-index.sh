export ES_HOST="http://localhost:9200"

curl -XDELETE "$ES_HOST/urls_v1"

curl -XPUT "$ES_HOST/urls_v1" -d '
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "url": {
      "_source": {
        "enabled": true
      },
      "properties": {
        "created": {
          "type": "date",
          "index": "not_analyzed",
          "doc_values": true
        },
        "updated": {
          "type": "date",
          "index": "not_analyzed",
          "doc_values": true
        },
        "published": {
          "type": "string",
          "index": "not_analyzed",
          "doc_values": true
        },
        "url": {
          "type": "string",
          "index": "not_analyzed",
          "doc_values": true
        },
        "source": {
          "type": "string",
          "index": "not_analyzed",
          "doc_values": true
        },
        "status": {
          "type": "string",
          "index": "not_analyzed",
          "doc_values": true
        }
      }
    }
  }
}'

curl -XPOST "$ES_HOST/_aliases" -d '
{
    "actions" : [
        { "add" : { "index" : "urls_v1", "alias" : "urls" } }
    ]
}'