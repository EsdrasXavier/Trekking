package br.org.catolicasc.trekking;

public class GpsMath {

    private final double EARTH_RADIUS = 6372795.0;

    public static Double courseTo(double lat1, double long1, double lat2, double long2) {
            double dlon = Math.toRadians(long1 - long2);
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);
            double y0 = Math.sin(dlon) * Math.cos(lat2);
            double x0 = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon);
            x0 = Math.atan2(y0, x0);

            return Math.toDegrees(x0);
    }

    public static Double distanceBetween(double lat1, double long1, double lat2, double long2) {
        double delta = Math.toRadians(long1 - long2);
        double sdlong = Math.sin(delta);
        double cdlong = Math.cos(delta);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double slat1 = Math.sin(lat1);
        double clat1 = Math.cos(lat1);
        double slat2 = Math.sin(lat2);
        double clat2 = Math.cos(lat2);

        double y0 = (clat1 * slat2) - (slat1 * clat2 * cdlong);
        y0 = sq(y0) + sq(clat2 * sdlong);
        y0 = Math.sqrt(y0);
        double x0 = (slat1 * slat2) + (clat1 * clat2 * cdlong);
        delta = Math.atan2(y0, x0) * 6372795.0;

        return delta;
    }

    public static Double sq(double x) { return x * x; }
}
