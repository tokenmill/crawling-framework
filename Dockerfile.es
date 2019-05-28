FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.3.0 as builder

ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/e1f115e4ca285c3c24e847c4dd4be955e0ed51c2/wait-for-it.sh /utils/wait-for-it.sh

COPY bin/ bin/
COPY elasticsearch/ elasticsearch/

RUN /usr/local/bin/docker-entrypoint.sh elasticsearch -p /tmp/epid & /bin/bash /utils/wait-for-it.sh -t 0 localhost:9200 -- \
    ./bin/create-es-indices.sh ; \
    kill $(cat /tmp/epid) && wait $(cat /tmp/epid); exit 0;

FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.3.0

COPY --from=builder /usr/share/elasticsearch/data /usr/share/elasticsearch/data
