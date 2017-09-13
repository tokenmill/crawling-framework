package lt.tokenmill.crawling.es;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {

    public static byte[] readResourceAsBytes(String filename) throws URISyntaxException, IOException {
        return Files.readAllBytes(Paths.get(TestUtils.class.getClassLoader().getResource(filename).toURI()));
    }

    public static String readResourceAsString(String filename) throws URISyntaxException, IOException {
        return new String(readResourceAsBytes(filename), StandardCharsets.UTF_8);
    }

}
