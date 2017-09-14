package org.openhab.binding.fronius.internal.configuration;

public class ServiceConfiguration {

    private final String hostname;
    private final int device;

    public ServiceConfiguration(String hostname, int device) {
        super();
        this.hostname = hostname;
        this.device = device;
    }

    public String getHostname() {
        return hostname;
    }

    public int getDevice() {
        return device;
    }
}
