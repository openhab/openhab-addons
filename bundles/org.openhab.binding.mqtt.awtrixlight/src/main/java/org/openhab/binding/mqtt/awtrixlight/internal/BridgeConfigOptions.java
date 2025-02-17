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
package org.openhab.binding.mqtt.awtrixlight.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BridgeConfigOptions} Holds the config for the bridge settings.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class BridgeConfigOptions {
    public String basetopic = "awtrix";
    public int appLockTimeout = 10;
    public boolean discoverDefaultApps = false;
    public int lowBatteryThreshold = 25;
}
