STORM_HOME=/opt/storm/apache-storm-1.1.1
mvn clean install -Pbigjar -Dstorm.scope=provided
$STORM_HOME/bin/storm jar crawler/target/crawler-standalone.jar lt.tokenmill.crawling.crawler.CrawlerTopology -conf crawler/conf/local.yaml
