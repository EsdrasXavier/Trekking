package br.org.catolicasc.trekking.models;

import java.io.Serializable;

public class Point implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ACTIVITY_PARAM = "SERIALIZED_GEO_POINT";

    private long id;
    private double lat = 0.0;
    private double lon = 0.0;
    private int typeId;
    private PointType type;

    public Point() {}

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public String getPreciseLat(int precision) {
        return Point.preciseLatLon(precision, lat);
    }

    public String getPreciseLon(int precision) {
        return Point.preciseLatLon(precision, lon);
    }

    public boolean isValid(){
        return lat != 0.0 && lon != 0.0;
    }
    public boolean isPersisted() { return id > 0; }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public PointType getType() {
        return new PointType(getTypeId());
    }

    public static String preciseLatLon(int precision, double val) {
        String mask = "%."+ precision + "f";
        return String.format(mask, val);
    }
}
