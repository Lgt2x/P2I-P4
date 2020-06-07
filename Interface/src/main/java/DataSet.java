import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class DataSet {

    // TODO stocker le chart pour ne pas le rebuild entièrement à chaque fois
    LectureBase source;
    double[][] dataset;
    String typeX, typeY, stationName;
    String[] units = new String[2];

    // {X, Y}
    Integer[] lowerThresholds = new Integer[2], higherThresholds = new Integer[2];

    XYSeriesRenderStyle graphType;

    public DataSet(LectureBase bd, String stationName, String typeX, String typeY) {

        this.source = bd;
        this.typeX = typeX;
        this.typeY = typeY;
        this.stationName = stationName;
    }

    public static DataSet buildDataSet(LectureBase bd, String stationName, String typeX, String typeY) throws Exception {

        if (typeX.equalsIgnoreCase("Temps") || typeY.equalsIgnoreCase("Temps"))
            return new TimestampedDataSet(bd, stationName, typeX, typeY);
        else
            return new BiQuantityDataSet(bd, stationName, typeX, typeY);
    }

    public abstract XYChart makeChart();

    protected void makeXYThreshold(XYChart chart, double[] otherDataValues, Integer threshold, String thresholdType, String typeName, boolean isXTime) {

        if (threshold == null)
            return;

        boolean thresholdOnX = typeName.equalsIgnoreCase(typeX);

        String seriesName = "Seuil " + thresholdType + " de " + threshold + units[thresholdOnX ? 0 : 1];
        double[] thresholdHeight = new double[] { (double) threshold, (double) threshold };
        double[] thresholdWidth = new double[] { Arrays.stream(otherDataValues).min().getAsDouble(), Arrays.stream(otherDataValues).max().getAsDouble() };

        XYSeries series;

        if (isXTime) {
            List<Date> thresholdWidthDate = List.of(new Date((long) thresholdWidth[0]), new Date((long) thresholdWidth[1]));
            List<Double> thresholdHeightDouble = List.of(thresholdHeight[0], thresholdHeight[1]);
            series = chart.addSeries(seriesName, thresholdWidthDate, thresholdHeightDouble);
        } else {
            series = chart.addSeries(seriesName, thresholdOnX ? thresholdHeight : thresholdWidth, thresholdOnX ? thresholdWidth : thresholdHeight);
        }

        series.setXYSeriesRenderStyle(XYSeriesRenderStyle.Line);
    }

    protected void makeXYThreshold(XYChart chart, double[] otherDataValues, Integer threshold, String thresholdType, String typeName) {
        makeXYThreshold(chart, otherDataValues, threshold, thresholdType, typeName, false);
    }
}