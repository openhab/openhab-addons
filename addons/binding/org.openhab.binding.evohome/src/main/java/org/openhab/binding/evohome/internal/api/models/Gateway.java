package org.openhab.binding.evohome.internal.api.models;

public class Gateway {

    private int id;
    private String name;

    public Gateway(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
