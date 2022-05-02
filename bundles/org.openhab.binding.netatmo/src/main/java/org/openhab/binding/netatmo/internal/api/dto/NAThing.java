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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link NAThing} is the base class for devices and modules.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAThing extends NAObject implements NAModule {
    private static final int UNREACHABLE_DELAY_S = 1800;
    @SerializedName(value = "rf_status", alternate = { "wifi_status", "rf_strength", "wifi_strength" })
    private int radioStatus = -1;
    @SerializedName(value = "last_seen", alternate = { "last_therm_seen", "last_status_store", "last_plug_seen",
            "last_message", "last_activity" })
    private @Nullable ZonedDateTime lastSeen;
    @SerializedName(value = "firmware", alternate = { "firmware_revision" })
    private @Nullable String firmware;
    private @Nullable Boolean reachable;
    private @Nullable Dashboard dashboardData;

    private @Nullable String roomId;
    private @Nullable String bridge;
    private ModuleType type = ModuleType.UNKNOWN;

    @Override
    public ModuleType getType() {
        return type;
    }

    public boolean isReachable() {
        // This is not implemented on all devices/modules, so if absent we consider it is reachable
        Boolean localReachable = this.reachable;
        boolean result = localReachable != null ? localReachable : true;
        // and we double check by comparing data freshness
        ZonedDateTime localLastSeen = lastSeen;
        if (result && localLastSeen != null && !type.isLogical()) {
            result = Duration.between(localLastSeen, ZonedDateTime.now().withZoneSameInstant(localLastSeen.getZone()))
                    .getSeconds() < UNREACHABLE_DELAY_S;
        }
        return result;
    }

    public @Nullable Dashboard getDashboardData() {
        return dashboardData;
    }

    public @Nullable String getFirmware() {
        return firmware;
    }

    public int getRadioStatus() {
        return radioStatus;
    }

    public Optional<ZonedDateTime> getLastSeen() {
        return Optional.ofNullable(lastSeen);
    }

    /**
     * @return true if the equipment has no parent, meaning its a device.
     */
    public boolean isDevice() {
        return bridge == null;
    }

    public @Nullable String getBridge() {
        return bridge;
    }

    public @Nullable String getRoomId() {
        return roomId;
    }
}
