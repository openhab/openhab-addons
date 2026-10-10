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
package org.openhab.binding.veluxactive.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for a VELUX ACTIVE account.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record VeluxActiveAccountConfiguration(String username, String password, int refreshInterval) {

    public static final int MINIMUM_REFRESH_INTERVAL = 30;
    public static final int DEFAULT_REFRESH_INTERVAL = 60;

    public VeluxActiveAccountConfiguration {
        username = username != null ? username : "";
        password = password != null ? password : "";
        refreshInterval = refreshInterval > 0 ? refreshInterval : DEFAULT_REFRESH_INTERVAL;
    }

    public boolean isComplete() {
        return !username.isBlank() && !password.isBlank();
    }

    public boolean hasSameCredentials(VeluxActiveAccountConfiguration other) {
        return username.equals(other.username) && password.equals(other.password);
    }
}
