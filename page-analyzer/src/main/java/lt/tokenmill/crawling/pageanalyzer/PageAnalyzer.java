package lt.tokenmill.crawling.pageanalyzer;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import lt.tokenmill.crawling.data.HtmlAnalysisResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PageAnalyzer {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    public static final String CONFIG_USER_AGENT = "UserAgent";
    public static final String CONFIG_ANALYZE_ROBOTS_TXT = "RobotsTxt";

    public static HtmlAnalysisResult analyze(Map<String, String> config, String url) {
        try {
            String userAgent = config.getOrDefault(CONFIG_USER_AGENT, DEFAULT_USER_AGENT);
            HttpResponse<String> response = Unirest.get(url)
                    .header("User-Agent", userAgent)
                    .asString();
            return analyze(config, url, response.getBody(), response.getStatus(), response.getHeaders());
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public static HtmlAnalysisResult analyze(Map<String, String> config, String url, String html) {
        return analyze(config, url, html, null, Maps.newHashMap());
    }

    public static HtmlAnalysisResult analyze(Map<String, String> config, String url, String html, Integer status, Map<String, List<String>> headers) {
        try {
            HtmlAnalysisResult result = new HtmlAnalysisResult();
            result.setUrl(url);
            result.setHttpStatus(status);
            result.setHeaders(headers.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> Joiner.on("\n").join(e.getValue()))));

            Document document = Jsoup.parse(html, url);
            result.setTitle(document.title());

            List<String> meta = document.select("meta").stream().map(Element::toString).collect(Collectors.toList());
            result.setMetaValues(meta);

            List<String> links = document.select("a").stream().map(e -> e.attr("abs:href")).collect(Collectors.toList());
            result.setLinks(links);

            if (Boolean.parseBoolean(config.get(CONFIG_ANALYZE_ROBOTS_TXT))) {
                String robotsUrl = robotsTxtUrl(url);
                String userAgent = config.getOrDefault(CONFIG_USER_AGENT, DEFAULT_USER_AGENT);
                HttpResponse<String> response = Unirest.get(robotsUrl)
                        .header("User-Agent", userAgent)
                        .asString();
                String robotsTxt = response.getBody();
                parseRobotsTxt(userAgent, robotsUrl, robotsTxt, result);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void parseRobotsTxt(String userAgent, String robotsUrl, String robotsTxt, HtmlAnalysisResult result) {
        result.setRobotsTxt(robotsTxt);
        SimpleRobotRulesParser robotsParser = new SimpleRobotRulesParser();
        BaseRobotRules robotRules = robotsParser.parseContent(robotsUrl, robotsTxt.getBytes(), null, userAgent);
        result.setRobotsAllowedAll(robotRules.isAllowAll());
        result.setRobotsAllowedNone(robotRules.isAllowNone());
        result.setRobotsAllowedHome(robotRules.isAllowed("/"));
        result.setRobotsSitemaps(robotRules.getSitemaps());
        result.setRobotsCrawlDelay(robotRules.getCrawlDelay());
    }

    private static String robotsTxtUrl(String url) {
        try {
            URL urlObject = new URL(url);
            String portPart = urlObject.getPort() > 0 ? ":" + urlObject.getPort() : "";
            return String.format("%s://%s%s/robots.txt", urlObject.getProtocol(),
                    urlObject.getHost(), portPart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
