# Crawling Framework

Crawling Framework aims at providing instruments to configure and run your [Storm Crawler](http://stormcrawler.net/) based crawler. It mainly aims at easing crawling of article content publishing sites like news portals or blog sites. With the help of GUI tool Crawling Framework provides you can:

1. Specify which sites to crawl.
1. Configure URL inclusion and exclusion filters, thus controlling which sections of the site will be fetched.
1. Specify which elements of the page provide information about article publication name, its title and main body.
1. Define tests which validate that extraction rules are working.

Once configuration is done the Crawling Framework runs [Storm Crawler](http://stormcrawler.net/) based crawling following the rules specified in the configuration.

## Introduction

We have recorded a video on how to setup and use Crawling Framework. Click on the image below to watch in on Youtube.

[![Crawling Framework Intro](https://img.youtube.com/vi/AvO4lmmIuis/0.jpg)](https://www.youtube.com/watch?v=AvO4lmmIuis)

## Requirements

Framework writes its configuration and stores crawled data to ElasticSearch. Before starting crawl project [install ElasticSearch](https://www.elastic.co/guide/en/elasticsearch/reference/current/_installation.html) (Crawling Framework is tested to work with Elastic v5.5.x).

Crawling Framework is a Java lib which will have to be extended to run Storm Crawler topology, thus Java (JDK8, Maven) infrastructure will be needed. 

## Configuring and Running a crawl

See [Crawling Framework Example](https://github.com/tokenmill/crawling-framework-example) project's documentation.


## License

Copyright © 2017 [TokenMill](http://www.tokenmill.co/)

Distributed under the The Apache License, Version 2.0

