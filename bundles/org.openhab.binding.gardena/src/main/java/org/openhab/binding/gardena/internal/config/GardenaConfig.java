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

    private @Nullable String email;
    private @Nullable String password;
    private @Nullable String apiKey;

    private transient Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    public GardenaConfig() {
    }

    public GardenaConfig(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the email to connect to Gardena smart system.
     */
    public @Nullable String getEmail() {
        return email;
    }

    /**
     * Sets the email to connect to Gardena smart system.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the password to connect to Gardena smart system.
     */
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * Sets the password to connect to Gardena smart system.
     */
    public void setPassword(String password) {
        this.password = password;
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
        final String email = this.email;
        final String password = this.password;
        final String apiKey = this.apiKey;
        return email != null && !email.isBlank() && password != null && !password.isBlank() && apiKey != null
                && !apiKey.isBlank();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GardenaConfig.class.getSimpleName()).append("[");
        sb.append("email: ").append(email).append(", ");
        sb.append("connectionTimeout: ").append(connectionTimeout).append(", ");
        sb.append("apiKey: ").append(apiKey);
        return sb.append("]").toString();
    }
}
