package org.openhab.binding.supla;

import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

import java.util.Arrays;

public final class SuplaCloudConfiguration {
    private String server;
    private String clientId;
    private char[] secret;
    private String username;
    private char[] password;
    private int refreshInterval;

    public SuplaCloudServer toSuplaCloudServer() {
        return new SuplaCloudServer(server, clientId, secret, username, password);
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return new String(secret);
    }

    public void setSecret(String secret) {
        this.secret = secret.toCharArray();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return new String(password);
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Override
    public String toString() {
        return "SuplaCloudConfiguration{" +
                "server='" + server + '\'' +
                ", clientId='" + clientId + '\'' +
                ", secret=" + Arrays.toString(secret) +
                ", username='" + username + '\'' +
                ", password=" + Arrays.toString(password) +
                ", refreshInterval=" + refreshInterval +
                '}';
    }
}
