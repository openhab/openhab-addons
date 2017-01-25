package org.openhab.binding.isy.internal;

public class Variable {

    public String type;
    public String id;

    public Integer init;
    public Integer val;
    // protected String ts;

    public Variable() {

    }

    public Variable(String id, String type, int value) {
        this.id = id;
        this.type = type;
        this.val = value;
    }

    @Override
    public String toString() {
        return new StringBuilder("Isy Variable: ts=").append(id).append(", id=").append(id).append(", enabled=")
                .append(id).toString();
    }

}
