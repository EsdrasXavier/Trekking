package br.org.catolicasc.trekking.models;

public class PointType {
    private static final String[] NAMES = {"Novo Curso", "Obstáculo"};
    private String name;
    private int id;

    public PointType(int typeId) {
        if (typeId < 1 || typeId > NAMES.length) {
            this.id = 1;
        } else {
            this.id = typeId;
        }
        this.name = NAMES[this.id - 1];
    }

    public boolean isObstacle() {
        return id == 2;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
