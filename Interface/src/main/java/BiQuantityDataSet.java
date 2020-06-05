import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.Color;

public class BiQuantityDataSet extends DataSet {

    public BiQuantityDataSet(LectureBase bd, String stationName,  String typeX, String typeY) {

        super(bd, stationName, typeX, typeY);

        dataset = bd.getBiQuantityDataset(stationName, typeX, typeY);
        graphType = XYSeries.XYSeriesRenderStyle.Scatter;
    }

    @Override
    public Chart makeChart() {

        if (dataset == null || dataset[0] == null || dataset[1] == null || dataset[0].length != dataset[1].length || dataset[0].length == 0)
            return null;

        Chart chart = QuickChart.getChart("Graphique",
                                    typeX + " (" + /* TODO: ajouter unités + */ ")",
                                    typeY + " (" + /* TODO: ajouter unités + */ ")",
                                    stationName,
                                    dataset[0], dataset[1]);

        XYStyler styler = ((XYChart) chart).getStyler();
        styler.setDefaultSeriesRenderStyle(graphType);
        chart.getSeriesMap().values().stream().forEach(series -> {

            if (series instanceof XYSeries) {
                XYSeries xySeries = (XYSeries) series;

                xySeries.setMarker(SeriesMarkers.PLUS);
                xySeries.setMarkerColor(Color.blue);
            }
        });

        return chart;
    }
}
