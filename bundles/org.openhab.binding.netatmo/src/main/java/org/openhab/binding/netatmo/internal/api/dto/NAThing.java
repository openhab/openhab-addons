/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ModuleType;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAThing extends NAObject {
    @SerializedName(value = "rf_status", alternate = { "wifi_status", "rf_strength" })
    private int radioStatus;
    @SerializedName(value = "last_seen", alternate = { "last_therm_seen", "last_status_store", "last_plug_seen",
            "last_message", "last_activity" })
    private @Nullable ZonedDateTime lastSeen;
    @SerializedName(value = "firmware", alternate = { "firmware_revision" })
    private int firmware = -1;
    private @Nullable ZonedDateTime setupDate;
    private @NonNullByDefault({}) ModuleType type;
    private @Nullable String roomId;
    private @Nullable Boolean reachable;
    private @Nullable NADashboard dashboardData;
    private @Nullable String bridge;

    public boolean isReachable() {
        // This is not implemented on all devices/modules, so if absent
        // we consider it is reachable
        Boolean localReachable = this.reachable;
        return localReachable != null ? localReachable : true;
    }

    public @Nullable NADashboard getDashboardData() {
        return dashboardData;
    }

    public ModuleType getType() {
        return type;
    }

    public void setType(ModuleType type) {
        this.type = type;
    }

    public int getFirmware() {
        return firmware;
    }

    public int getRadioStatus() {
        return radioStatus;
    }

    public Optional<ZonedDateTime> getLastSeen() {
        return Optional.ofNullable(lastSeen);
    }

    public @Nullable ZonedDateTime getSetupDate() {
        return setupDate;
    }

    public @Nullable String getBridge() {
        return bridge;
    }

    public @Nullable String getRoomId() {
        return roomId;
    }
}
