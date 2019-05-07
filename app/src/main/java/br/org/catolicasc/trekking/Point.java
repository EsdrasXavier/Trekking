package br.org.catolicasc.trekking;

public class Point {

    private double latitude;
    private double longitude;
    private boolean isGhost;

    Point(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
        this.isGhost = false;
    }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }
}
