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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureType;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAThing extends NAObject {
    @SerializedName(value = "rf_status", alternate = { "wifi_status" })
    private int radioStatus;
    @SerializedName(value = "last_seen", alternate = { "last_therm_seen", "last_status_store", "last_plug_seen" })
    private long lastSeen;
    private int firmware = -1;
    private List<MeasureType> dataType = List.of();
    private @NonNullByDefault({}) ModuleType type;
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

    public List<MeasureType> getDataType() {
        return dataType;
    }

    public boolean canDeriveWeather() {
        return dataType.contains(MeasureType.TEMP) && dataType.contains(MeasureType.HUM);
    }

    public ModuleType getType() {
        return type;
    }

    public int getFirmware() {
        return firmware;
    }

    public int getRadioStatus() {
        return radioStatus;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public @Nullable String getBridge() {
        return bridge;
    }
}
