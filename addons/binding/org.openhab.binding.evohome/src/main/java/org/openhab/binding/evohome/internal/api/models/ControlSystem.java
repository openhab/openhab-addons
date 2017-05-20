package org.openhab.binding.evohome.internal.api.models;

public class ControlSystem {

    private int id;
    private String name;

    public ControlSystem(int id, String name) {
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
