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
package org.openhab.binding.loqed.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of a LOQED Local Bridge API connection.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedLocalConfiguration {
    public String host = "";
    public String bridgeKey = "";
    public String callbackBaseUrl = "";
    public int refreshInterval = 60;

    /**
     * Validates the required local bridge configuration.
     *
     * @return an error message, or an empty string if the configuration is valid
     */
    public String validate() {
        return host.isBlank() || bridgeKey.isBlank() ? "Host and bridge key are required" : "";
    }
}
