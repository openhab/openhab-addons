package org.openhab.binding.supla;

import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

import java.util.Arrays;

public final class SuplaCloudConfiguration {
    public String server;
    public String clientId;
    public String secret;
    public String username;
    public String password;
    public int refreshInterval;

    public SuplaCloudServer toSuplaCloudServer() {
        return new SuplaCloudServer(server, clientId, secret.toCharArray(), username, password.toCharArray());
    }

    @Override
    public String toString() {
        return "SuplaCloudConfiguration{" +
                "server='" + server + '\'' +
                ", clientId='" + clientId + '\'' +
                ", secret='" + secret + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", refreshInterval=" + refreshInterval +
                '}';
    }
}
