package lt.tokenmill.crawling.parser;

import lt.tokenmill.crawling.parser.utils.JsonLdParser;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JsonLdParserTest extends BaseArticleExtractorTest {


    @Test
    public void parseBBC1() throws Exception {
        String html = loadArticle("bbc1");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertEquals("Venezuela: New assembly approves treason trials for opposition", result.getHeadline());
        assertEquals("2017-08-30T10:32:11+01:00", result.getDatePublished());
    }

    @Test
    public void parseFortune1() throws Exception {
        String html = loadArticle("fortune1");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertEquals("The Engineer Who Blogged About Sexism At Uber Has a New Gig", result.getHeadline());
        assertEquals("2017-04-13T20:40:18", result.getDatePublished());
    }

    @Test
    public void parseReuters1() throws Exception {
        String html = loadArticle("reuters1");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertNull(result);
    }

    @Test
    public void parseReuters2() throws Exception {
        String html = loadArticle("reuters2");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertEquals("BRIEF-Canadian Solar unit Recurrent Energy reached commercial operation of 100 MWac/134 MWp", result.getHeadline());
        assertEquals("2016-08-23T12:24:03+0000", result.getDatePublished());
    }

    @Test
    public void parseFt1() throws Exception {
        String html = loadArticle("ft1");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertEquals("CBA accused of 50,000 breaches of money laundering laws", result.getHeadline());
        assertEquals("2017-08-03T08:09:14.000Z", result.getDatePublished());
    }

    @Test
    public void parseUsaNews1() throws Exception {
        String html = loadArticle("usanews1");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertEquals("Trump to Announce DACA Decision Tuesday", result.getHeadline());
        assertEquals("2017-09-01T04:18:00-04:00", result.getDatePublished());
        assertEquals("The White House said Friday that President Donald Trump would announce his decision Tuesday on whether to cancel an Obama-era program deferring deportation for some young immigrants in the U.S. illegally. \"We're going to make that announcement on Tuesday of next week,\" press secretary Sarah Huckabee Sanders told reporters Friday afternoon about the Deferred Action for Childhood Arrivals program. \"Those decisions are being finalized and when they are we will announce them on Tuesday.\" Trump has been under pressure from some Republicans to cancel DACA, a 2012 order by former President Barack Obama to allow immigrants who were brought to the U.S. illegally as children and meet several other qualifications to apply for protection from deportation. Attorneys general from 10 states, led by Texas's Ken Paxton, said they would sue the Trump administration over the program if the president does not cancel the program. Tennessee Attorney General Herbert Slatery on Friday withdrew his state from the threatened legal challenge, saying he still believes the program is an example of executive overreach but that \"there is a human element to this, however, that is not lost on me and should not be ignored.\" \"We believe there is a better approach,\" Slatery wrote in a letter to Tennessee's senators, Republicans Lamar Alexander and Bob Corker, pointing to bipartisan legislation that would put dreamers on a path to citizenship. Reports this week have indicated Trump was leaning toward canceling the program, which could affect some 800,000 people in the program, often called \"dreamers.\" Several prominent Republicans in Congress, including House Speaker Paul Ryan of Wisconsin, have discouraged him from doing so. \"I actually don't think he should do that and I believe that this is something Congress has to fix,\" Ryan told Wisconsin radio station WCLO-AM on Friday. Asked by a reporter in the Oval Office on Friday whether dreamers should be worried, Trump said only, \"We love the dreamers. We love everybody.\" Sanders said Trump to doing \"diligence\" in going through \"every step of the process.\" \"I think the decision itself is weighing on him,\" she said.", result.getArticleBody());
    }


    @Test
    public void parseNbcNews1() throws Exception {
        String html = loadArticle("nbcnews1");
        List<String> jsons = JsonLdParser.extractJsonLdParts(Jsoup.parse(html));
        JsonLdParser.JsonLdArticle result = JsonLdParser.parse(jsons);
        assertEquals("North Korea Crisis: Russia's Putin Warns of 'Global Catastrophe'", result.getHeadline());
        assertEquals("2017-09-05T13:49:03.000Z", result.getDatePublished());
    }
}
