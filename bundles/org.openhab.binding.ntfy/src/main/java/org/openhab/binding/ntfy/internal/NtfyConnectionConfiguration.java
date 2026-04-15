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
package org.openhab.binding.ntfy.internal;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NtfyConnectionConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyConnectionConfiguration {

    /**
     * Hostname of the ntfy server, e.g. "https://ntfy.sh"
     */
    public String hostname = "";

    /**
     * Username for basic authentication
     */
    public @Nullable String username;

    /**
     * Password for basic authentication
     */
    public @Nullable String password;

    /**
     * Connection timeout in milliseconds
     */
    public long connectionTimeout = 60000;

    /**
     * Checks whether a Basic Authorization header should be provided based on the configured
     * username and password values.
     *
     * @return {@code true} when both username and password are non-null and non-blank,
     *         {@code false} otherwise
     */
    public boolean isAuthHeaderNeeded() {
        final @Nullable String username = this.username;
        final @Nullable String password = this.password;
        return username != null && !username.isBlank() && password != null && !password.isBlank();
    }

    /**
     * Builds the HTTP Basic Authorization header value from the configured username and password.
     *
     * <p>
     * The credentials are combined as "username:password" and Base64-encoded using UTF-8.
     * The returned value is prefixed with "Basic ".
     *
     * @return the full value for the HTTP Authorization header
     */
    public String buildAuthHeader() {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;
        return authHeader;
    }
}
