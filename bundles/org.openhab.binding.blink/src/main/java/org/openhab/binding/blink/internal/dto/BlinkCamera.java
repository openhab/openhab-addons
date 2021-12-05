package org.openhab.binding.blink.internal.dto;

public class BlinkCamera {

    public Long id;
    public Long network_id;
    public String name;

    public boolean enabled;
    public String thumbnail;
    public String status;
    public String battery;

    public Signals signals;

    public class Signals {
        public long wifi;
        public long temp;
    }
}
