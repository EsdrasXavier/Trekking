package br.org.catolicasc.trekking.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import br.org.catolicasc.trekking.db.DatabaseHelper;

// The CRUD functionality is abstracted in a DatabaseAccessLayer class which is
// inherited by the *Model*Dao class
public abstract class DatabaseAccessLayer {
    private static final String TAG = "DAL";

    protected SQLiteDatabase db;
    protected DatabaseHelper helper;
    protected abstract <T> T cursorToEntity(Cursor cursor);

    public DatabaseAccessLayer(Context context) {
        this.helper = new DatabaseHelper(context);
    }

    public int delete(String tableName, String selection, String[] selectionArgs) {
        db = helper.getWritableDatabase();
        int result = db.delete(tableName, selection, selectionArgs);
        return result;
    }

    public int update(String tableName, ContentValues values,
                      String selection, String[] selectionArgs) {
        db = helper.getWritableDatabase();
        int result = db.update(tableName, values, selection, selectionArgs);

        return result;
    }

    public long insert(String tableName, ContentValues values) {
        db = helper.getWritableDatabase();
        long result = db.insert(tableName, null, values);
        return result;
    }

    public Cursor query(String tableName, String[] columns, String selection,
                        String[] selectionArgs, String sortOrder) {
        db = helper.getReadableDatabase();
        final Cursor cursor = db.query(tableName, columns, selection, selectionArgs,
                null, null, sortOrder);
        return cursor;
    }

    public Cursor query(String tableName, String[] columns, String selection, String[]
            selectionArgs, String sortOrder, String limit) {
        db = helper.getReadableDatabase();
        final Cursor cursor = db.query(tableName, columns, selection, selectionArgs, null,
                null, sortOrder, limit);
        db.close();
        return cursor;
    }

    public Cursor query(String tableName, String[] columns, String selection, String[]
            selectionArgs, String groupBy, String having, String orderBy, String limit) {
        db = helper.getReadableDatabase();
        final Cursor cursor = db.query(tableName, columns, selection,
                selectionArgs, groupBy, having, orderBy, limit);
        return cursor;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        db = helper.getReadableDatabase();
        final Cursor cursor = db.rawQuery(sql, selectionArgs);
        return cursor;
    }
}
