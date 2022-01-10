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
package org.openhab.binding.netatmo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link NetatmoBridgeConfiguration} is responsible for holding
 * configuration informations needed to access Netatmo API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoBridgeConfiguration {
    public @Nullable String clientId;
    public @Nullable String clientSecret;
    public @Nullable String username;
    public @Nullable String password;
    public boolean readStation = true;
    public boolean readThermostat = false;
    public boolean readHealthyHomeCoach = false;
    public boolean readWelcome = false;
    public boolean readPresence = false;
    public @Nullable String webHookUrl;
    public int reconnectInterval = 5400;
}
