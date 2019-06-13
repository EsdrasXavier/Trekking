package br.org.catolicasc.trekking.db.tables;

public class PointsTable {

    public static final String TABLE_NAME = "points";
    public static final String PRIMARY_KEY = "_id";

    // Database Columns
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_TYPE_ID = "type_id";
    public static final String[] COLUMNS = {
            PRIMARY_KEY,
            COLUMN_LAT,
            COLUMN_LON,
            COLUMN_TYPE_ID
    };
}
