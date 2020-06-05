import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.internal.series.Series;

import java.util.Map;

public class TimestampedDataSet extends DataSet {

    public TimestampedDataSet(LectureBase bd, String stationName, String typeX, String typeY) throws Exception {

        super(bd, stationName, typeX, typeY);

        boolean isXTime;
        dataset = bd.getTimestampedDataset(stationName, typeX, typeY, isXTime = typeX.equalsIgnoreCase("Temps"));
        graphType = XYSeries.XYSeriesRenderStyle.Line;

        Object[] dataInfos = source.getDataTypeInfo(isXTime ? typeY : typeX);

        if(dataInfos == null)
            throw new Exception("Erreur construction du dataset, la DB n'a pas renvoyé un résultat correct");


        int timeIndex = isXTime ? 0 : 1;
        int dataIndex = (timeIndex + 1) % 2;

        units[timeIndex] = "Timestamp";
        units[dataIndex] = dataInfos[0].toString();

        lowerThresholds[timeIndex] = null;
        higherThresholds[timeIndex] = null;

        if (dataInfos[1] != null)
            lowerThresholds[dataIndex] = Integer.valueOf(dataInfos[1].toString());

        if (dataInfos[2] != null)
            higherThresholds[dataIndex] = Integer.valueOf(dataInfos[2].toString());
    }

    @Override
    public Chart makeChart() {

        if (dataset == null || dataset[0] == null || dataset[1] == null || dataset[0].length != dataset[1].length || dataset[0].length == 0)
            return null;

        // TODO échelle de temps
        XYChart chart = QuickChart.getChart("Graphique",
                                   typeX + " (" + units[0] + ")",
                                   typeY + " (" + units[1] + ")",
                                            stationName,
                                            dataset[0], dataset[1]);

        makeXYThreshold(chart, dataset[1], lowerThresholds[0], "bas", typeX);
        makeXYThreshold(chart, dataset[0], lowerThresholds[1], "bas", typeY);
        makeXYThreshold(chart, dataset[1], higherThresholds[0], "haut", typeX);
        makeXYThreshold(chart, dataset[0], higherThresholds[1], "haut", typeY);

        return chart;
    }
}
