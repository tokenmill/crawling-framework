package lt.tokenmill.crawling.parser.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TextFilterTest {

    @Test
    public void testTextFilterEdgeCases() {
        String text = null;
        List<String> normalizers = Arrays.asList("a-->>b");
        assertEquals("", TextFilters.normalizeText(text, normalizers));

        text = null;
        normalizers = null;
        assertEquals("", TextFilters.normalizeText(text, normalizers));

        text = null;
        normalizers = Arrays.asList();
        assertEquals("", TextFilters.normalizeText(text, normalizers));

        text = "aaabbb";
        normalizers = Arrays.asList("a");
        assertEquals("aaabbb", TextFilters.normalizeText(text, normalizers));

        text = "[[[bbb";
        normalizers = Arrays.asList("[-->>a");
        assertEquals("[[[bbb", TextFilters.normalizeText(text, normalizers));

        text = "[[[bbb";
        normalizers = Arrays.asList("\\[-->>a");
        assertEquals("aaabbb", TextFilters.normalizeText(text, normalizers));
    }

    @Test
    public void testTextFilter000() {
        String text = "aaa";
        List<String> normalizers = Arrays.asList("a-->>b");
        assertEquals("bbb", TextFilters.normalizeText(text, normalizers));
    }

    @Test
    public void testTextFilter001() {
        String text = "Malwarebytes, a San Jose, CA-based provider of an advanced malware prevention and remediation solution, raised $50m in Series B funding. Fidelity Management and Research Company made the investment. Founded in 2008 by Marcin Kleczynski CEO, Malwarebytes protects millions of consumers and more than 70,000 SMBs and enterprise businesses against online malicious threats via its flagship product, Malwarebytes Anti-Malware. In 2015, Malwarebytes launched its news business suite of products – Malwarebytes Anti-Malware for Business; Malwarebytes Anti-Exploit joined Anti-Malware Premium for Business and Malwarebytes Endpoint Security, which ran on 250 million unique endpoints worldwide while the company surpassed $100 million in annualized billings. Malwarebytes, which also operates offices in Estonia and Ireland, has a team of over 300 people. It was bootstrapped until raising a $30m Series A funding round in July 2014, led by Highland Capital Partners. FinSMEs 21/01/2016";
        List<String> normalizers = Arrays.asList("FinSMEs\\s\\d{2}/\\d{2}/\\d{4}-->>");
        assertEquals("Malwarebytes, a San Jose, CA-based provider of an advanced malware prevention and remediation solution, raised $50m in Series B funding. Fidelity Management and Research Company made the investment. Founded in 2008 by Marcin Kleczynski CEO, Malwarebytes protects millions of consumers and more than 70,000 SMBs and enterprise businesses against online malicious threats via its flagship product, Malwarebytes Anti-Malware. In 2015, Malwarebytes launched its news business suite of products – Malwarebytes Anti-Malware for Business; Malwarebytes Anti-Exploit joined Anti-Malware Premium for Business and Malwarebytes Endpoint Security, which ran on 250 million unique endpoints worldwide while the company surpassed $100 million in annualized billings. Malwarebytes, which also operates offices in Estonia and Ireland, has a team of over 300 people. It was bootstrapped until raising a $30m Series A funding round in July 2014, led by Highland Capital Partners.", TextFilters.normalizeText(text, normalizers));
    }

    @Test
    public void testTextFilter002() {
        String text = "Talari Networks, a San Jose, CA-based provider of network reliability and business continuity solutions, received a $7.5m growth capital facility. Square 1 Bank, the premier banking partner to entrepreneurs and the venture capital community, provided the loan. The company intends to use the proceeds to supplement working capital. Led by President & CEO Emerick Woods, Talari Networks provides patented Adaptive Private Networking (APN) overlay technology that enables high-bandwidth, low-cost, enterprise quality WANs for business and government customers. It is backed by Menlo Ventures, Silver Creek Ventures and Four Rivers Group.  Related News 18/11/2013: Talari Networks Raises $15M in Series D Financing 24/01/2012: Talari Networks Secures $4.5M in Financing";
        List<String> normalizer = Arrays.asList("Related News \\d{2}.*-->>");
        assertEquals("Talari Networks, a San Jose, CA-based provider of network reliability and business continuity solutions, received a $7.5m growth capital facility. Square 1 Bank, the premier banking partner to entrepreneurs and the venture capital community, provided the loan. The company intends to use the proceeds to supplement working capital. Led by President & CEO Emerick Woods, Talari Networks provides patented Adaptive Private Networking (APN) overlay technology that enables high-bandwidth, low-cost, enterprise quality WANs for business and government customers. It is backed by Menlo Ventures, Silver Creek Ventures and Four Rivers Group.", TextFilters.normalizeText(text, normalizer));
    }
}
