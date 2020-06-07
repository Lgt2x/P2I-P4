import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.colors.XChartSeriesColors;

import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class TimestampedDataSet extends DataSet {

    public TimestampedDataSet(LectureBase bd, String stationName, String typeX, String typeY) throws Exception {

        super(bd, stationName, typeX, typeY);

        boolean isXTime;

        dataset = bd.getTimestampedDataset(stationName, typeX, typeY, isXTime = typeX.equalsIgnoreCase("Temps"));
        graphType = XYSeries.XYSeriesRenderStyle.Line;

        Object[] dataInfos = source.getDataTypeInfo(isXTime ? typeY : typeX);

        if (dataInfos == null)
            throw new Exception("Erreur construction du dataset, la DB n'a pas renvoyé un résultat correct");

        int timeIndex = 0;
        int dataIndex = 1;

        units[timeIndex] = "date-heure";
        units[dataIndex] = dataInfos[0].toString();

        lowerThresholds[timeIndex] = null;
        higherThresholds[timeIndex] = null;

        if (dataInfos[1] != null)
            lowerThresholds[dataIndex] = Integer.valueOf(dataInfos[1].toString());

        if (dataInfos[2] != null)
            higherThresholds[dataIndex] = Integer.valueOf(dataInfos[2].toString());
    }

    @Override
    public XYChart makeChart() {

        if (dataset == null || dataset[0] == null || dataset[1] == null || dataset[0].length != dataset[1].length || dataset[0].length == 0)
            return null;

        XYChart chart = new XYChartBuilder()
                                .title("Graphique")
                                .xAxisTitle(typeX + " (" + units[0] + ")")
                                .yAxisTitle(typeY + " (" + units[1] + ")")
                                .build();

        chart.getStyler().setDatePattern("dd-MMM HH:mm");
        chart.getStyler().setLocale(Locale.FRANCE);

        XYSeries series = chart.addSeries("Fake Data",
                DoubleStream.of(dataset[0])
                        .mapToObj(val -> new Date((long) val))
                        .collect(Collectors.toList()),
                DoubleStream.of(dataset[1])
                        .boxed()
                        .collect(Collectors.toList()));
        series.setXYSeriesRenderStyle(graphType);
        series.setLineColor(XChartSeriesColors.BLUE);


        makeXYThreshold(chart, dataset[1], lowerThresholds[0], "bas", typeX);
        makeXYThreshold(chart, dataset[0], lowerThresholds[1], "bas", typeY, true);
        makeXYThreshold(chart, dataset[1], higherThresholds[0], "haut", typeX);
        makeXYThreshold(chart, dataset[0], higherThresholds[1], "haut", typeY, true);

        return chart;
    }
}
