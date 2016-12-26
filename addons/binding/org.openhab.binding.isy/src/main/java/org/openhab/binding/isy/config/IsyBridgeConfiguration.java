package org.openhab.binding.isy.config;

public class IsyBridgeConfiguration {

    private String user;
    private String password;
    private String ipAddress;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
