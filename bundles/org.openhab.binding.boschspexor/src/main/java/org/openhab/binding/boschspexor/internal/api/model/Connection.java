package org.openhab.binding.boschspexor.internal.api.model;

public class Connection {

    public enum ConnectionType {
        MobileNetwork,
        Wifi
    }

    private String lastConnection;
    private boolean online;
    private ConnectionType connectionType;

    public String getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(String lastConnection) {
        this.lastConnection = lastConnection;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
}
