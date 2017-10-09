package lt.tokenmill.crawling.adminui.view;


import com.byteowls.vaadin.chartjs.ChartJs;
import com.byteowls.vaadin.chartjs.config.BarChartConfig;
import com.byteowls.vaadin.chartjs.data.BarDataset;
import com.byteowls.vaadin.chartjs.data.Dataset;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.byteowls.vaadin.chartjs.options.Position;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.es.model.DateHistogramValue;

import java.util.List;
import java.util.stream.Collectors;

public class HttpSourceStatsWindow extends Window {

    public HttpSourceStatsWindow(String sourceUrl) {
        setModal(true);
        center();
        setCaption(String.format("%s crawling statistics", sourceUrl));
        setWidth(50, Unit.PERCENTAGE);
        setHeight(50, Unit.PERCENTAGE);
        List<DateHistogramValue> urls = ElasticSearch.getUrlOperations().calculateStats(sourceUrl);
        List<DateHistogramValue> documents = ElasticSearch.getDocumentOperations().calculateStats(sourceUrl);
        Component layout = getChart(sourceUrl, urls, documents);
        layout.setWidth(100, Unit.PERCENTAGE);
        setContent(layout);
    }

    public Component getChart(String sourceUrl, List<DateHistogramValue> urls, List<DateHistogramValue> documents) {
        BarChartConfig config = new BarChartConfig();

        BarDataset docsDataset = new BarDataset().type().label("Fetched Documents")
                .borderColor("rgb(54, 162, 235)")
                .backgroundColor("rgb(54, 162, 235)")
                .borderWidth(2);
        documents.forEach(d -> docsDataset.addLabeledData(d.getDate(), Double.valueOf(d.getValue())));

        LineDataset urlsDataset = new LineDataset().type().label("Discovered Urls")
                .borderColor("rgb(75, 192, 192)")
                .backgroundColor("white")
                .borderWidth(2);
        urls.forEach(d -> urlsDataset.addLabeledData(d.getDate(), Double.valueOf(d.getValue())));

        config.data()
                .labelsAsList(urls.stream().map(DateHistogramValue::getDate).collect(Collectors.toList()))
                .addDataset(docsDataset)
                .addDataset(urlsDataset)
                .and();

        config.options()
                .responsive(true)
                .title()
                .display(true)
                .position(Position.LEFT)
                .and()
                .done();

        ChartJs chart = new ChartJs(config);
        chart.setJsLoggingEnabled(true);
        return chart;
    }
}
