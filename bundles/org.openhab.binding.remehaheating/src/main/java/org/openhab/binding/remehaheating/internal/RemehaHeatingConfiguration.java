/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.remehaheating.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RemehaHeatingConfiguration} class contains fields mapping thing configuration parameters.
 * 
 * This configuration class holds the parameters required to connect to a Remeha Home account:
 * - Email and password for authentication
 * - Refresh interval for periodic data updates
 *
 * @author Michael Fraedrich - Initial contribution
 */
@NonNullByDefault
public class RemehaHeatingConfiguration {

    /**
     * Remeha Home account email address.
     * This is the same email used for the Remeha Home mobile app.
     */
    public String email = "";

    /**
     * Remeha Home account password.
     * This is the same password used for the Remeha Home mobile app.
     */
    public String password = "";

    /**
     * Refresh interval in seconds for polling the Remeha API.
     * Default is 60 seconds. Valid range is 30-3600 seconds.
     */
    public int refreshInterval = 60;
}
