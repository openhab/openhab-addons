package org.openhab.binding.robonect;

public class RobonectEndpoint {
    
    private String ipAddress;

    public RobonectEndpoint(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
