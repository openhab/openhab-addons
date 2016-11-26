package org.openhab.binding.edimax.configuration;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EdimaxConfiguration {

    private String ipAddress;
    private String username;
    private String password;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("IP", this.getIpAddress()).append("user", this.getUsername())
                .append("password", this.getPassword()).toString();
    }
}
