/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;

/**
 * The {@link NetatmoBindingConfiguration} is responsible for holding configuration
 * information needed to access Netatmo API and general binding behavior setup
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoBindingConfiguration {
    public class NACredentials {
        public final String clientId, clientSecret, username, password;

        NACredentials(String clientId, String clientSecret, String username, String password) {
            this.clientSecret = clientSecret;
            this.username = username;
            this.password = password;
            this.clientId = clientId;
        }
    }

    public @Nullable String webHookUrl;
    public int reconnectInterval = 5400;
    private @Nullable NACredentials credentials;

    public void update(Map<String, Object> config) throws NetatmoException {
        this.webHookUrl = (String) config.get("webHookUrl");
        String interval = (String) config.get("reconnectInterval");
        this.reconnectInterval = interval != null ? Integer.parseInt(interval) : 5400;

        credentials = null;
        String clientId = checkMandatory(config, "clientId", "@text/conf-error-no-client-id");
        String username = checkMandatory(config, "username", "@text/conf-error-no-username");
        String password = checkMandatory(config, "password", "@text/conf-error-no-password");
        String clientSecret = checkMandatory(config, "clientSecret", "@text/conf-error-no-client-secret");
        credentials = new NACredentials(clientId, clientSecret, username, password);
    }

    private String checkMandatory(Map<String, Object> config, String key, String error) throws NetatmoException {
        Object confElement = config.get(key);
        if (!(confElement instanceof String)) {
            throw new NetatmoException(error);
        }
        String value = (String) confElement;
        if (value.isBlank()) {
            throw new NetatmoException(error);
        }
        return value;
    }

    public @Nullable NACredentials getCredentials() {
        return credentials;
    }
}
