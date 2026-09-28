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
package org.openhab.binding.dreame.internal.model;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Writable L50 cleaning properties and their protocol values.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public enum DreameVacuumSetting {
    SUCTION_LEVEL("suction-level", 4, 4, Map.of("QUIET", 0, "STANDARD", 1, "STRONG", 2, "TURBO", 3)),
    WATER_VOLUME("water-volume", 4, 5, Map.of("LOW", 1, "MEDIUM", 2, "HIGH", 3)),
    CLEANING_MODE("cleaning-mode", 4, 23, Map.of("SWEEPING", 0, "MOPPING", 1, "SWEEPING_AND_MOPPING", 2)),
    DRYING_TIME("drying-time", 4, 40, Map.of("2H", 2, "3H", 3, "4H", 4)),
    CLEAN_GENIUS("clean-genius", 4, 50, "SmartHost", Map.of("OFF", 0, "ROUTINE", 1, "DEEP", 2)),
    CLEANING_ROUTE("cleaning-route", 4, 50, "CleanRoute", Map.of("STANDARD", 1, "QUICK", 4));

    private final String channelId;
    private final int serviceId;
    private final int propertyId;
    private final @Nullable String autoSwitchKey;
    private final Map<String, Integer> values;

    DreameVacuumSetting(String channelId, int serviceId, int propertyId, Map<String, Integer> values) {
        this(channelId, serviceId, propertyId, null, values);
    }

    DreameVacuumSetting(String channelId, int serviceId, int propertyId, @Nullable String autoSwitchKey,
            Map<String, Integer> values) {
        this.channelId = channelId;
        this.serviceId = serviceId;
        this.propertyId = propertyId;
        this.autoSwitchKey = autoSwitchKey;
        this.values = values;
    }

    public String channelId() {
        return channelId;
    }

    public int serviceId() {
        return serviceId;
    }

    public int propertyId() {
        return propertyId;
    }

    public @Nullable String autoSwitchKey() {
        return autoSwitchKey;
    }

    public @Nullable Integer value(String command) {
        return values.get(command.trim().toUpperCase(Locale.ROOT));
    }

    public @Nullable String state(int value) {
        return values.entrySet().stream().filter(entry -> entry.getValue() == value).map(Map.Entry::getKey).findFirst()
                .orElse(null);
    }
}
