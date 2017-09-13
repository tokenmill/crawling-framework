package lt.tokenmill.crawling.commonui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    public static final Configuration INSTANCE = new Configuration();

    private static final String DEFAULT_CONFIG_FILE_LOCATION = "conf/development.properties";
    private final Properties properties = new Properties();

    private Configuration() {
        try {
            properties.load(new FileInputStream(new File(System.getProperty("config", DEFAULT_CONFIG_FILE_LOCATION))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, Integer.toString(defaultValue)));
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "properties='" + properties + "'" +
                "}";
    }
}