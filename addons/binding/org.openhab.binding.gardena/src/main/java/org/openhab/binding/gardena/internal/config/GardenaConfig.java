/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    private static final Integer DEFAULT_SESSION_TIMEOUT = 30;
    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 10;
    private static final Integer DEFAULT_REFRESH = 60;

    private String email;
    private String password;

    private transient Integer sessionTimeout = DEFAULT_SESSION_TIMEOUT;
    private transient Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private transient Integer refresh = DEFAULT_REFRESH;

    public GardenaConfig() {
    }

    public GardenaConfig(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the email to connect to Gardena Smart Home.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email to connect to Gardena Smart Home.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the password to connect to Gardena Smart Home.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password to connect to Gardena Smart Home.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the session timeout to Gardena Smart Home.
     */
    public int getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * Sets the session timeout to Gardena Smart Home.
     */
    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Returns the connection timeout to Gardena Smart Home.
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout to Gardena Smart Home.
     */
    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Returns the refresh interval to fetch new data from Gardena Smart Home.
     */
    public Integer getRefresh() {
        return refresh;
    }

    /**
     * Returns the refresh interval to fetch new data from Gardena Smart Home.
     */
    public void setRefresh(Integer refresh) {
        this.refresh = refresh;
    }

    /**
     * Validate the config, if at least email and password is specified.
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(email) && StringUtils.isNotBlank(password);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("email", email)
                .append("password", StringUtils.isBlank(password) ? "" : StringUtils.repeat("*", password.length()))
                .append("sessionTimeout", sessionTimeout).append("connectionTimeout", connectionTimeout)
                .append("refresh", refresh).toString();
    }

}
