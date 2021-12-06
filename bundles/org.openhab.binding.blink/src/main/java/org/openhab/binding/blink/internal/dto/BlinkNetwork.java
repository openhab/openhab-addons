package org.openhab.binding.blink.internal.dto;

public class BlinkNetwork {

    public BlinkNetwork(Long id) {
        this.id = id;
    }

    public Long id;
    public String name;
    public boolean armed;

}
