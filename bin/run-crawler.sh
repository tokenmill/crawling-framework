mvn clean install
( cd crawler && mvn package -Pbigjar && java -cp target/crawler-standalone.jar lt.tokenmill.crawling.crawler.CrawlerTopology -local -conf conf/local.yaml )