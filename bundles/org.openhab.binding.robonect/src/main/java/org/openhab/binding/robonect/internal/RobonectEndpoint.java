/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.StringUtil;

/**
 * Connection details for a Robonect endpoint.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class RobonectEndpoint {

    private final String ipAddress;

    private final @Nullable String user;

    private final @Nullable String password;

    private boolean useAuthentication = false;

    /**
     * Creates connection settings for a Robonect endpoint.
     *
     * @param ipAddress the endpoint address
     * @param user the username
     * @param password the password
     */
    public RobonectEndpoint(String ipAddress, @Nullable String user, @Nullable String password) {
        this.ipAddress = ipAddress;
        this.user = user;
        this.password = password;
        this.useAuthentication = StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password);
    }

    /**
     * Returns the endpoint address.
     *
     * @return the configured address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns the endpoint username.
     *
     * @return the configured username
     */
    public @Nullable String getUser() {
        return user;
    }

    /**
     * Returns the endpoint password.
     *
     * @return the configured password
     */
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * Checks whether authentication is configured.
     *
     * @return {@code true} if both username and password are set
     */
    public boolean isUseAuthentication() {
        return useAuthentication;
    }
}
