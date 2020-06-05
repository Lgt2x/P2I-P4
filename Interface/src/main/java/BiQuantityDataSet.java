import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.Color;

public class BiQuantityDataSet extends DataSet {

    public BiQuantityDataSet(LectureBase bd, String stationName,  String typeX, String typeY) throws Exception{

        super(bd, stationName, typeX, typeY);

        dataset = bd.getBiQuantityDataset(stationName, typeX, typeY);
        graphType = XYSeries.XYSeriesRenderStyle.Scatter;

        Object[] xDataInfo = source.getDataTypeInfo(typeX);
        Object[] yDataInfo = source.getDataTypeInfo(typeY);

        if(xDataInfo == null || yDataInfo == null)
            throw new Exception("Erreur construction du dataset, la DB n'a pas renvoyé un résultat correct");

        units[0] = xDataInfo[0].toString();
        units[1] = yDataInfo[0].toString();

        lowerThresholds[0] = Integer.valueOf(xDataInfo[1].toString());
        lowerThresholds[1] = Integer.valueOf(yDataInfo[1].toString());

        higherThresholds[0] = Integer.valueOf(xDataInfo[2].toString());
        higherThresholds[1] = Integer.valueOf(yDataInfo[2].toString());
    }

    @Override
    public Chart makeChart() {

        if (dataset == null || dataset[0] == null || dataset[1] == null || dataset[0].length != dataset[1].length || dataset[0].length == 0)
            return null;

        Chart chart = QuickChart.getChart("Graphique",
                                    typeX + " (" + units[0] + ")",
                                    typeY + " (" + units[1] + ")",
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
