mvn clean install
( cd crawler && mvn package -Pbigjar && java -cp target/crawler-0.1.4-standalone.jar lt.tokenmill.crawling.crawler.CrawlerTopology -local -conf conf/local.yaml )