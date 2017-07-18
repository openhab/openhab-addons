/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla;

import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

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
