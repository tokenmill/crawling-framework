package lt.tokenmill.crawling.es;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public class ElasticsearchTestServer {

    private static class MyNode extends Node {
        MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(new Environment(preparedSettings, null), classpathPlugins, false);
        }
    }

    private final Node node;
    private Client client;

    private ElasticsearchTestServer(Builder builder) {
        if (builder.cleanDataDir) {
            try {
                Path rootPath = Paths.get(builder.dataDirectory);
                if (Files.exists(rootPath)) {
                    Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Settings settings = Settings.builder()
                .put("client.transport.ignore_cluster_name", true)
                .put("transport.type", "netty4")
                .put("http.type", "netty4")
                .put("http.enabled", "true")
                .put("http.port", builder.httpPort)
                .put("path.home", builder.dataDirectory)
                .put("transport.tcp.port", builder.transportPort)
                .build();
        this.node = new MyNode(settings, Arrays.asList(Netty4Plugin.class));
    }

    public void start() {
        try {
            this.node.start();
            this.client = this.node.client();
        } catch (NodeValidationException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            this.client.close();
            this.node.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        private boolean cleanDataDir = true;
        private String dataDirectory = "target/elasticsearch-data";
        private int httpPort = 9200;
        private int transportPort = 9305;

        public Builder httpPort(int httpPort) {
            this.httpPort = httpPort;
            return this;
        }

        public Builder transportPort(int transportPort) {
            this.transportPort = transportPort;
            return this;
        }

        public ElasticsearchTestServer build() {
            return new ElasticsearchTestServer(this);
        }


        public Builder dataDirectory(String dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public Builder cleanDataDir(boolean cleanDataDir) {
            this.cleanDataDir = cleanDataDir;
            return this;
        }
    }

}
