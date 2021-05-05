package org.openhab.binding.nuki.internal.dto;

public class WebApiBridgeDto {
    private String bridgeId;
    private String ip;
    private int port;
    private String dateUpdated;

    public String getBridgeId() {
        return bridgeId;
    }

    public void setBridgeId(String bridgeId) {
        this.bridgeId = bridgeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public String toString() {
        return "WebApiBridgeDto{" + "bridgeId='" + bridgeId + '\'' + ", ip='" + ip + '\'' + ", port=" + port
                + ", dateUpdated='" + dateUpdated + '\'' + '}';
    }
}
