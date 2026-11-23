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
package org.openhab.binding.tibber.internal.pulse.config;

import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration of the {@code bridge} thing.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class BridgeConfiguration {

    private static final int MAX_HOSTNAME_LENGTH = 253;

    public String hostname = "";
    public String password = "";

    /**
     * @return the host name or address without surrounding whitespace
     */
    public String normalizedHostname() {
        return hostname.trim();
    }

    /**
     * Checks the parameters without any network access.
     *
     * @return the status description key that names the first invalid parameter, or null if the configuration is valid
     */
    public @Nullable String validate() {
        if (!isValidHost(normalizedHostname())) {
            return TEXT_INVALID_HOSTNAME;
        }
        if (password.isEmpty()) {
            return TEXT_EMPTY_PASSWORD;
        }
        return null;
    }

    /**
     * Only rules out what can never be a host name or would do harm; whether the name or address exists is decided by
     * the resolver when the stream connects. The control character check protects the Host header of the request, the
     * slash catches a pasted URL.
     */
    private static boolean isValidHost(String host) {
        if (host.isEmpty() || host.length() > MAX_HOSTNAME_LENGTH || host.indexOf('/') >= 0) {
            return false;
        }
        for (int i = 0; i < host.length(); i++) {
            char c = host.charAt(i);
            if (Character.isISOControl(c) || Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                return false;
            }
        }
        return true;
    }

    /** Omits the password. */
    @Override
    public String toString() {
        return "BridgeConfiguration[hostname=" + hostname + "]";
    }
}
