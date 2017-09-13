package lt.tokenmill.crawling.adminui.view.imports;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.adminui.utils.CSVUtils;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.NamedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NamedQueryImportExport extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(NamedQueryImportExport.class);

    public static final String[] CSV_COLUMNS = new String[]{
            "name", "nostem_cs", "nostem_ci", "stem_cs", "stem_ci", "advanced"};

    private CheckBox cleanBeforeImportCheckbox = new CheckBox("Clean Before Import");

    public NamedQueryImportExport() {
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
        Button exportButton = new Button("Export Named Queries");
        exportLayout.addComponent(exportButton);

        StreamResource streamResource = getNamedQueriesExportStream();
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
        upload.setButtonCaption("Import Named Queries");
        upload.addStyleName(ValoTheme.BUTTON_LARGE);
        upload.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        upload.addSucceededListener(uploadReceiver);
        upload.addFailedListener(uploadReceiver);

        importLayout.addComponent(upload);
        importPanel.setContent(importLayout);

        addComponent(importPanel);
    }

    public static StreamResource getNamedQueriesExportStream() {
        StreamResource.StreamSource source = (StreamResource.StreamSource) () -> {
            LOG.info("Exporting http sources data...");
            List<NamedQuery> namedQueries = ElasticSearch.getNamedQueryOperations().all();
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = CSVUtils.createDefaultWriter(writer);
            csvWriter.writeNext(CSV_COLUMNS);
            for (NamedQuery nq : namedQueries) {
                csvWriter.writeNext(new String[]{
                        nq.getName(),
                        nq.getNotStemmedCaseSensitive(),
                        nq.getNotStemmedCaseInSensitive(),
                        nq.getStemmedCaseSensitive(),
                        nq.getStemmedCaseInSensitive(),
                        nq.getAdvanced()
                });
            }
            String csv = writer.getBuffer().toString();
            return new ByteArrayInputStream(csv.getBytes(Charsets.UTF_8));

        };
        return new StreamResource(source, "named-queries-exported-" +
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv");
    }

    public static void importCSV(String csv, boolean cleanBeforeImport) {
        LOG.info("Importing CSV. Cleanup old data before import: {}", cleanBeforeImport);
        CSVReader csvReader = CSVUtils.createDefaultReader(csv);
        try {
            if (cleanBeforeImport) {
                ElasticSearch.getNamedQueryOperations().deleteAll();
            }
            List<String[]> data = csvReader.readAll();
            Map<String, Integer> columnIndexes = CSVUtils.resolveColumnIndexes(CSV_COLUMNS, data.get(0));
            data.stream()
                    .skip(1)
                    .map(row -> mapCsvRowToNamedQuery(row, columnIndexes))
                    .forEach(nq -> ElasticSearch.getNamedQueryOperations().save(nq));
            LOG.info("Imported {} rows", data.size() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static NamedQuery mapCsvRowToNamedQuery(String[] row, Map<String, Integer> columnIndexes) {
        NamedQuery nq = new NamedQuery();
        nq.setName(Strings.emptyToNull(row[columnIndexes.get("name")]));
        nq.setNotStemmedCaseSensitive(Strings.emptyToNull(row[columnIndexes.get("nostem_cs")]));
        nq.setNotStemmedCaseInSensitive(Strings.emptyToNull(row[columnIndexes.get("nostem_ci")]));
        nq.setStemmedCaseSensitive(Strings.emptyToNull(row[columnIndexes.get("stem_cs")]));
        nq.setStemmedCaseInSensitive(Strings.emptyToNull(row[columnIndexes.get("stem_ci")]));
        nq.setAdvanced(Strings.emptyToNull(row[columnIndexes.get("advanced")]));
        return nq;
    }




    private class CSVReceiver implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

        private ByteArrayOutputStream stream;

        public OutputStream receiveUpload(String filename,
                                          String mimeType) {
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
