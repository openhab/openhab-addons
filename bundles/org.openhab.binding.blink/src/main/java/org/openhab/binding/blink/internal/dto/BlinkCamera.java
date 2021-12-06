package org.openhab.binding.blink.internal.dto;

public class BlinkCamera {

    public BlinkCamera(Long networkId, Long cameraId) {
        this.network_id = networkId;
        this.id = cameraId;
    }

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
        public double temp;
    }
}
