/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.supla.entities;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class SuplaCloudServer {
    private final String server;
    private final String clientId;
    private final char[] secret;
    private final String username;
    private final char[] password;

    public SuplaCloudServer(String server, String clientId, char[] secret, String username, char[] password) {
        this.server = checkNotNull(server);
        this.clientId = checkNotNull(clientId);
        this.secret = checkNotNull(secret);
        this.username = checkNotNull(username);
        this.password = checkNotNull(password);

        checkArgument(!server.isEmpty());
        checkArgument(!clientId.isEmpty());
        checkArgument(secret.length != 0);
        checkArgument(!username.isEmpty());
        checkArgument(password.length != 0);
    }

    public String getServer() {
        return server;
    }

    public String getClientId() {
        return clientId;
    }

    public char[] getSecret() {
        return secret;
    }

    public String getSecretAsString() {
        return join(secret);
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public String getPasswordAsString() {
        return join(password);
    }

    private static String join(char[] chars) {
        StringBuilder sb = new StringBuilder();
        for(char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaCloudServer)) return false;

        SuplaCloudServer that = (SuplaCloudServer) o;

        if (!server.equals(that.server)) return false;
        if (!clientId.equals(that.clientId)) return false;
        if (!Arrays.equals(secret, that.secret)) return false;
        if (!username.equals(that.username)) return false;
        return Arrays.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return clientId.hashCode();
    }

    @Override
    public String toString() {
        return "SuplaCloudServer{" +
                "server='" + server + '\'' +
                ", clientId='" + clientId + '\'' +
                ", secret=[PROTECTED]" +
                ", username='" + username + '\'' +
                ", password=[PROTECTED]" +
                '}';
    }
}
