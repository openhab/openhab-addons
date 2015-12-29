package org.openhab.binding.kostal.inverter;

public class ChannelConfig {
    public ChannelConfig(String id, String tag, int num) {
        this.id = id;
        this.tag = tag;
        this.num = num;
    }

    String id;
    String tag;
    int num;
}
