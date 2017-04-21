package org.openhab.binding.antiferencematrix.internal.model;

public abstract class Port {

    private int bay;
    private String mode;
    private String type;
    private int status;
    private String name;
    private int dps;

    public int getBay() {
        return bay;
    }

    public String getMode() {
        return mode;
    }

    public String getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public int getDps() {
        return dps;
    }

}
