package org.openhab.binding.isy.internal;

public class Program {

    protected String id;
    protected String name;
    protected String running;
    protected String enabled;
    protected String folder;

    protected String status;

    public Program() {

    }

    public Program(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // protected String uri;
    @Override
    public String toString() {
        return new StringBuilder("Isy Program: name=").append(name).append(", id=").append(id).append(", running=")
                .append(running).toString();
    }

}
