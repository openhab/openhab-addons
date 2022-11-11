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
package org.openhab.binding.gardena.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The main Gardena config class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaConfig {
    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 10;

    private @Nullable String apiSecret;
    private @Nullable String apiKey;

    private transient Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    public GardenaConfig() {
    }

    public GardenaConfig(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * Returns the apiSecret to connect to Gardena smart system.
     */
    public @Nullable String getApiSecret() {
        return apiSecret;
    }

    /**
     * Sets the apiSecret to connect to Gardena smart system.
     */
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    /**
     * Returns the connection timeout to Gardena smart system.
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout to Gardena smart system.
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Returns the api key.
     */
    public @Nullable String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the api key.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Validate the config if email, password and apiKey is specified.
     */
    public boolean isValid() {
        final String apiSecret = this.apiSecret;
        final String apiKey = this.apiKey;
        return apiSecret != null && !apiSecret.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GardenaConfig.class.getSimpleName()).append("[");
        sb.append("connectionTimeout: ").append(connectionTimeout).append(", ");
        sb.append("apiKey: ").append(apiKey);
        return sb.append("]").toString();
    }
}
