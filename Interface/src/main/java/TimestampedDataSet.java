import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;

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

        lowerThresholds[dataIndex] = Integer.valueOf(dataInfos[0].toString());
        higherThresholds[dataIndex] = Integer.valueOf(dataInfos[1].toString());
    }

    @Override
    public Chart makeChart() {

        if (dataset == null || dataset[0] == null || dataset[1] == null || dataset[0].length != dataset[1].length || dataset[0].length == 0)
            return null;

        // TODO échelle de temps
        return QuickChart.getChart("Graphique",
                                   typeX + " (" + units[0] + ")",
                                   typeY + " (" + units[1] + ")",
                                   stationName,
                                   dataset[0], dataset[1]);
    }
}
