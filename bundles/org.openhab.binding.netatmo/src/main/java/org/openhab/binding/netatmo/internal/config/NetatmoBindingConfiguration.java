/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.netatmo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;

/**
 * The {@link NetatmoBindingConfiguration} is responsible for holding configuration
 * informations needed to access Netatmo API and general binding behavior setup
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoBindingConfiguration {
    public @Nullable String clientId;
    public @Nullable String clientSecret;
    public @Nullable String username;
    public @Nullable String password;
    public @Nullable String webHookUrl;
    public int reconnectInterval = 5400;
    public boolean backgroundDiscovery;

    public void update(NetatmoBindingConfiguration newConfiguration) {
        this.clientId = newConfiguration.clientId;
        this.clientSecret = newConfiguration.clientSecret;
        this.username = newConfiguration.username;
        this.password = newConfiguration.password;
        this.webHookUrl = newConfiguration.webHookUrl;
        this.reconnectInterval = newConfiguration.reconnectInterval;
        this.backgroundDiscovery = newConfiguration.backgroundDiscovery;
    }

    public void checkIfValid() throws NetatmoException {
        String clientId = this.clientId;
        if (clientId == null || clientId.isEmpty()) {
            throw new NetatmoException("@text/conf-error-no-client-id");
        }
        String username = this.username;
        if (username == null || username.isEmpty()) {
            throw new NetatmoException("@text/conf-error-no-username");
        }
        String password = this.password;
        if (password == null || password.isEmpty()) {
            throw new NetatmoException("@text/conf-error-no-password");
        }
        String clientSecret = this.clientSecret;
        if (clientSecret == null || clientSecret.isEmpty()) {
            throw new NetatmoException("@text/conf-error-no-client-secret");
        }
    }
}
