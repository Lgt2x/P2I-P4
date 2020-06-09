import org.openstreetmap.gui.jmapviewer.Coordinate;

public class Location {

    public final Coordinate coordinate;
    public final String name;

    public Location(double lat, double longi, String name) {

        this.coordinate = new Coordinate(lat, longi);
        this.name = name;
    }

    public String toString() {

        return name + "[lat=" + coordinate.getLat() + ", long=" + coordinate.getLon() + "]";
    }
}
