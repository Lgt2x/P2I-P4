import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.internal.chartpart.Chart;

public abstract class DataSet {

    // TODO stocker le chart pour ne pas le rebuild entièrement à chaque fois
    String typeX, typeY, stationName;
    double[][] dataset;
    LectureBase source;

    XYSeriesRenderStyle graphType;

    public DataSet(LectureBase bd, String stationName, String typeX, String typeY) {

        this.source = bd;
        this.typeX = typeX;
        this.typeY = typeY;
        this.stationName = stationName;
    }

    public abstract Chart makeChart();

    public static DataSet buildDataSet(LectureBase bd, String stationName, String typeX, String typeY) {

        if (typeX.equalsIgnoreCase("Temps") || typeY.equalsIgnoreCase("Temps"))
            return new TimestampedDataSet(bd, stationName, typeX, typeY);
        else
            return new BiQuantityDataSet(bd, stationName, typeX, typeY);
    }
}
