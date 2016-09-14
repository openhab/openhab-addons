package org.openhab.binding.edimax.config;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EdimaxConfiguration {

    protected static final int PORT = 10000;

    private String ipAddress;
    private String password;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("IP", this.getIpAddress())
                // .append("port", this.getPort())
                // .append("proto", this.getProtocol())
                // .append("user", this.getUser())
                .append("password", this.getPassword())
                // .append("pollingInterval", this.getPollingInterval())
                // .append("asyncTimeout", this.getAsyncTimeout())
                // .append("syncTimeout", this.getSyncTimeout())
                .toString();
    }
}
