package br.org.catolicasc.trekking.db.migrations;


import br.org.catolicasc.trekking.db.DatabaseHelper;

public class CreatePoints implements DatabaseHelper.DatabaseMigration {
    @Override
    public String up() {
        return "CREATE TABLE points (" +
                "_id integer primary key autoincrement," +
                "lat double, " +
                "lon double, " +
                "type_id int)";
    }

    @Override
    public String down() {
        return "DROP TABLE IF EXISTS points";
    }
}
