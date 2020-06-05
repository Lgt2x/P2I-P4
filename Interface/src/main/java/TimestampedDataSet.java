import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;

public class TimestampedDataSet extends DataSet {

    public TimestampedDataSet(LectureBase bd, String stationName, String typeX, String typeY) {

        super(bd, stationName, typeX, typeY);

        dataset = bd.getTimestampedDataset(stationName, typeX, typeY, typeX.equalsIgnoreCase("Temps"));
        graphType = XYSeries.XYSeriesRenderStyle.Line;
    }

    @Override
    public Chart makeChart() {

        if (dataset == null || dataset[0] == null || dataset[1] == null || dataset[0].length != dataset[1].length || dataset[0].length == 0)
            return null;

        // TODO échelle de temps
        return QuickChart.getChart("Graphique",
                                   typeX + " (" + /* TODO: ajouter unités + */ ")",
                                   typeY + " (" + /* TODO: ajouter unités + */ ")",
                                   stationName,
                                   dataset[0], dataset[1]);
    }
}
