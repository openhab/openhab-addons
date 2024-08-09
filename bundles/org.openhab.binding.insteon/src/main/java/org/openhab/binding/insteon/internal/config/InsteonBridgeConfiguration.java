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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InsteonBridgeConfiguration} is the base configuration for insteon bridges.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class InsteonBridgeConfiguration {

    private int devicePollIntervalInSeconds = 300;
    private boolean deviceDiscoveryEnabled = true;
    private boolean sceneDiscoveryEnabled = false;
    private boolean deviceSyncEnabled = false;

    public int getDevicePollInterval() {
        return devicePollIntervalInSeconds * 1000; // in milliseconds
    }

    public boolean isDeviceDiscoveryEnabled() {
        return deviceDiscoveryEnabled;
    }

    public boolean isSceneDiscoveryEnabled() {
        return sceneDiscoveryEnabled;
    }

    public boolean isDeviceSyncEnabled() {
        return deviceSyncEnabled;
    }

    public abstract String getId();

    @Override
    public String toString() {
        String s = "";
        s += " devicePollIntervalInSeconds=" + devicePollIntervalInSeconds;
        s += " deviceDiscoveryEnabled=" + deviceDiscoveryEnabled;
        s += " sceneDiscoveryEnabled=" + sceneDiscoveryEnabled;
        s += " deviceSyncEnabled=" + deviceSyncEnabled;
        return s;
    }
}
