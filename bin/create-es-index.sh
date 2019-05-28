# $1 - index name (docs, http_sources)
# $2 - ES index config file name
# $3 - ES host
# $4 - application name

if [ -z "$4" ]
then
      export INDEX_URL="http://$3:9200/$1_v1"
else
      export INDEX_URL="http://$3:9200/$4-$1_v1"
fi


curl -H "Content-Type:application/json" -XDELETE "$INDEX_URL"
echo
curl -H "Content-Type:application/json" -XPUT "$INDEX_URL" -d @elasticsearch/src/main/resources/indices/$2
echo
if [ -z "$4" ]
then
      curl -H "Content-Type:application/json" -XPUT "$INDEX_URL/_alias/$1"
      echo
else
      curl -H "Content-Type:application/json" -XPUT "$INDEX_URL/_alias/$4-$1"
      echo
fi
