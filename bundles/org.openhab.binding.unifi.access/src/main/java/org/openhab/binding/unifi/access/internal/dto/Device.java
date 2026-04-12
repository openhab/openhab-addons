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
package org.openhab.binding.unifi.access.internal.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Device details from the bootstrap topology.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Device {
    public @Nullable String alias;
    public @Nullable String id;
    public @Nullable String name;
    public @Nullable String type;
    public @Nullable String locationId;
    public @Nullable Boolean isAdopted;
    public @Nullable Boolean isConnected;
    public @Nullable Boolean isManaged;
    public @Nullable Boolean isOnline;
    public @Nullable String mac;
    public @Nullable String ip;
    public @Nullable String firmware;
    public @Nullable String connectedUahId;
    public @Nullable String displayModel;
    public @Nullable List<String> capabilities;

    /**
     * Flat key-value map from the device's configs array in the bootstrap.
     * Keys include things like "face", "nfc", "pin_code" (tag: open_door_mode),
     * "input_d1_dps", "output_d1_lock_relay" (tag: hub_action), etc.
     */
    public Map<String, String> configMap = new HashMap<>();

    /**
     * Returns the config value for the given key, or null if not present.
     */
    public @Nullable String getConfig(String key) {
        return configMap.get(key);
    }

    /**
     * Returns true if this device has the "is_hub" capability.
     */
    public boolean isHub() {
        return capabilities != null && capabilities.contains("is_hub");
    }

    /**
     * Returns true if this device has the "is_reader" capability.
     */
    public boolean isReader() {
        return capabilities != null && capabilities.contains("is_reader");
    }
}
