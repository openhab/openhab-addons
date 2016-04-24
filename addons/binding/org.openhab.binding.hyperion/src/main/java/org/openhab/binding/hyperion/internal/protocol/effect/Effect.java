package org.openhab.binding.hyperion.internal.protocol.effect;

public class Effect {

    private String name;

    public Effect(String name) {
        setName(name);
    }
    //

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
