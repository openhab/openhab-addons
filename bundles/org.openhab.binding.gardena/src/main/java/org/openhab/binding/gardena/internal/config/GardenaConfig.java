/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The main Gardena config class.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaConfig {
    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 10;

    private String email;
    private String password;
    private String apiKey;

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
    public String getEmail() {
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
    public String getPassword() {
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
    public String getApiKey() {
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
        return StringUtils.isNotBlank(email) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(apiKey);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("email", email)
                .append("password", StringUtils.isBlank(password) ? "" : StringUtils.repeat("*", password.length()))
                .append("connectionTimeout", connectionTimeout).append("apiKey", apiKey).toString();
    }
}
