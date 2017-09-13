package lt.tokenmill.crawling.adminui.view.imports;

import com.google.common.base.Charsets;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.adminui.utils.CSVUtils;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static lt.tokenmill.crawling.adminui.utils.CSVUtils.resolveColumnIndexes;
import static lt.tokenmill.crawling.adminui.utils.HttpSourceCSVUtils.*;

public class HttpSourceImportExport extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourceImportExport.class);

    private CheckBox cleanBeforeImportCheckbox = new CheckBox("Clean Before Import");

    public HttpSourceImportExport() {
        setSizeFull();
        setSpacing(true);
        setMargin(true);

        Panel importPanel = new Panel("Import");
        importPanel.setSizeFull();

        Panel exportPanel = new Panel("Export");
        exportPanel.setSizeFull();

        VerticalLayout exportLayout = new VerticalLayout();
        exportLayout.setSizeFull();
        exportLayout.setMargin(true);
        exportLayout.setSpacing(true);
        Button exportButton = new Button("Export HTTP Sources");
        exportLayout.addComponent(exportButton);

        StreamResource streamResource = getHttpSourcesExportStream();
        FileDownloader fileDownloader = new FileDownloader(streamResource);
        fileDownloader.extend(exportButton);

        exportPanel.setContent(exportLayout);
        addComponent(exportPanel);

        FormLayout importLayout = new FormLayout();
        importLayout.setSizeFull();
        importLayout.setMargin(true);
        importLayout.setSpacing(true);
        importLayout.addComponent(cleanBeforeImportCheckbox);

        CSVReceiver uploadReceiver = new CSVReceiver();
        Upload upload = new Upload("", uploadReceiver);
        upload.setButtonCaption("Import HTTP Sources");
        upload.addStyleName(ValoTheme.BUTTON_LARGE);
        upload.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        upload.addSucceededListener(uploadReceiver);
        upload.addFailedListener(uploadReceiver);

        importLayout.addComponent(upload);
        importPanel.setContent(importLayout);

        addComponent(importPanel);
    }

    public static StreamResource getHttpSourcesExportStream() {
        StreamResource.StreamSource source = (StreamResource.StreamSource) () -> {
            LOG.info("Exporting http sources data...");
            List<HttpSource> sources = ElasticSearch.getHttpSourceOperations().all();
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = CSVUtils.createDefaultWriter(writer);
            // Header line creation
            csvWriter.writeNext(CSV_COLUMNS);
            for (HttpSource ld : sources) {
                csvWriter.writeNext(mapHttpSourceToCsvRow(ld));
            }
            String csv = writer.getBuffer().toString();
            return new ByteArrayInputStream(csv.getBytes(Charsets.UTF_8));

        };
        return new StreamResource(source, "http-sources-exported-" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv");
    }

    public static void importCSV(String csv, boolean cleanBeforeImport) {
        LOG.info("Importing CSV. Cleanup old data before import: {}", cleanBeforeImport);
        CSVReader csvReader = CSVUtils.createDefaultReader(csv);
        try {
            if (cleanBeforeImport) {
                ElasticSearch.getHttpSourceOperations().deleteAll();
            }
            List<String[]> data = csvReader.readAll();
            String[] headerLine = data.get(0);
            Map<String, Integer> columnIndexes = resolveColumnIndexes(headerLine, CSV_COLUMNS);
            data.stream()
                    .skip(1)
                    .map(row -> mapCsvRowToHttpSource(row, columnIndexes))
                    .forEach(hs -> ElasticSearch.getHttpSourceOperations().save(hs));
            LOG.info("Imported {} rows", data.size() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class CSVReceiver implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

        private ByteArrayOutputStream stream;

        public OutputStream receiveUpload(String filename, String mimeType) {
            this.stream = new ByteArrayOutputStream();
            return this.stream;
        }

        @Override
        public void uploadFailed(Upload.FailedEvent event) {
            LOG.warn("CSV upload failed", event.getReason());
        }

        public void uploadSucceeded(Upload.SucceededEvent event) {
            Boolean cleanBeforeImport = cleanBeforeImportCheckbox.getValue();
            importCSV(new String(stream.toByteArray(), Charsets.UTF_8), cleanBeforeImport);
        }

    }
}
