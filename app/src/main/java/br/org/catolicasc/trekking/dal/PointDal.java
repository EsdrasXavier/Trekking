package br.org.catolicasc.trekking.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.org.catolicasc.trekking.db.tables.PointsTable;
import br.org.catolicasc.trekking.models.Point;

public class PointDal extends DatabaseAccessLayer {
    private static final String TAG = "PointDal";

    private Cursor cursor;
    private ContentValues initialValues;

    public PointDal(Context context) {
        super(context);
    }

    public Point findGeographicPointById(long resourceId) {
        final String selectionArgs[] = {String.valueOf(resourceId)};
        final String selection = String.format("%s = ?", PointsTable.PRIMARY_KEY);

        Point resource;
        cursor = super.query(PointsTable.TABLE_NAME, PointsTable.COLUMNS, selection,
                selectionArgs, PointsTable.PRIMARY_KEY);
        if (cursor != null && cursor.moveToFirst()) {
            resource = cursorToEntity(cursor);
            cursor.close();
        } else {
            return null;
        }
        db.close();
        return resource;
    }

    public List<Point> findAllGeographicPoints() {
        List<Point> resources = new ArrayList<>();

        cursor = super.query(PointsTable.TABLE_NAME, PointsTable.COLUMNS, null, null, PointsTable.PRIMARY_KEY);
        if (cursor != null) {
            cursor.moveToFirst();
            Log.d(TAG, "The total is: " + cursor.getCount());
            while (!cursor.isAfterLast()) {
                Point resource = cursorToEntity(cursor);
                resources.add(resource);
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return resources;
    }

    public long createGeographicPoint(Point resource) {
        setContentValue(resource);

        long insertedId = -1;
        try {
            insertedId = super.insert(PointsTable.TABLE_NAME, getContentValue());
        } catch (SQLiteConstraintException ex) {
            Log.e(TAG, ex.getMessage());
        }
        db.close();
        return insertedId;
    }

    public boolean updateGeographicPoint(Point resource) {
        setContentValue(resource);

        final String whereArgs[] = {String.valueOf(resource.getId())};
        final String where = String.format("%s = ?", PointsTable.PRIMARY_KEY);

        boolean result = false;
        try {
            result = super.update(PointsTable.TABLE_NAME, getContentValue(), where, whereArgs) > 0;
        } catch (SQLiteConstraintException ex) {
            Log.e(TAG, ex.getMessage());
        }
        db.close();
        return result;
    }

    public boolean deleteGeographicPoint(long resourceId) {
        final String whereArgs[] = {String.valueOf(resourceId)};
        final String where = String.format("%s = ?", PointsTable.PRIMARY_KEY);

        boolean result = false;
        try {
            result = super.delete(PointsTable.TABLE_NAME, where, whereArgs) > 0;
        } catch (SQLiteConstraintException ex) {
            Log.e(TAG, ex.getMessage());
        }
        db.close();
        return result;
    }

    @Override
    protected Point cursorToEntity(Cursor cursor) {
        Point resource = new Point();

        int pkIndex = -1;
        int latIndex = -1;
        int lonIndex = -1;
        int typeIdIndex = -1;

        if (cursor != null) {
            pkIndex = cursor.getColumnIndex(PointsTable.PRIMARY_KEY);
            latIndex = cursor.getColumnIndex(PointsTable.COLUMN_LAT);
            lonIndex = cursor.getColumnIndex(PointsTable.COLUMN_LON);
            typeIdIndex = cursor.getColumnIndex(PointsTable.COLUMN_TYPE_ID);

            if (pkIndex != -1) {
                resource.setId(cursor.getInt(pkIndex));
            }
            if (latIndex != -1) {
                resource.setLat(cursor.getDouble(latIndex));
            }
            if (lonIndex != -1) {
                resource.setLon(cursor.getDouble(lonIndex));
            }
            if (typeIdIndex != -1) {
                resource.setTypeId(cursor.getInt(typeIdIndex));
            }
        }

        return resource;
    }

    private void setContentValue(Point resource) {
        initialValues = new ContentValues();
        initialValues.put(PointsTable.COLUMN_LAT, resource.getLat());
        initialValues.put(PointsTable.COLUMN_LON, resource.getLon());
        initialValues.put(PointsTable.COLUMN_TYPE_ID, resource.getTypeId());
    }

    private ContentValues getContentValue() {
        return initialValues;
    }
}
