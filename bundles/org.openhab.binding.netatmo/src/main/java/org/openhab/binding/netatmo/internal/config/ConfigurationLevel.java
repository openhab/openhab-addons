/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/**
 * The {@link ConfigurationLevel} describes configuration levels of a given account thing
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum ConfigurationLevel {
    EMPTY_CLIENT_ID("@text/conf-error-no-client-id"),
    EMPTY_CLIENT_SECRET("@text/conf-error-no-client-secret"),
    REFRESH_TOKEN_NEEDED("@text/conf-error-grant-needed"),
    COMPLETED("");

    public String message;

    ConfigurationLevel(String message) {
        this.message = message;
    }
}
