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

    public void setLatitude(double lat) { this.latitude = lat; }

    public void setLongitude(double lon) { this.longitude = lon; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }
}
