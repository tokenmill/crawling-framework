( cd crawler && mvn package -Pbigjar -DskipTests && java -cp target/crawler-standalone.jar lt.tokenmill.crawling.crawler.CrawlerTopology -local -conf conf/local.yaml )
