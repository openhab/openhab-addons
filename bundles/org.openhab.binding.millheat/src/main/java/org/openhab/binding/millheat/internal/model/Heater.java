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
package org.openhab.binding.millheat.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.millheat.internal.dto.DeviceDTO;
import org.openhab.binding.millheat.internal.dto.DeviceMetricsDTO;
import org.openhab.binding.millheat.internal.dto.HeaterShadowDTO;

/**
 * A heater, either assigned to a room or controlled independently.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Rebuild on the cloud API's device model
 */
@NonNullByDefault
public class Heater {
    /** Device family name the cloud API uses for heaters. */
    public static final String FAMILY_HEATERS = "Heaters";

    private final String id;
    private final String name;
    private final @Nullable String macAddress;
    private final String family;
    private final boolean online;
    private final @Nullable Room room;

    private final @Nullable Double currentTemp;
    private final @Nullable Double currentPower;
    private final @Nullable Double energyUsage;
    private final boolean heatingActive;
    private final boolean windowOpen;

    private @Nullable Double targetTemp;
    private boolean powerStatus;
    private boolean fanActive;
    private final boolean canChangeTemp;

    public Heater(final DeviceDTO dto, final @Nullable Room room) {
        this.room = room;
        id = dto.deviceId();
        final String customName = dto.customName();
        name = customName == null ? dto.deviceId() : customName;
        macAddress = dto.macAddress();
        family = dto.family();
        online = Boolean.TRUE.equals(dto.isConnected());

        final DeviceMetricsDTO metrics = dto.lastMetrics();
        if (metrics != null) {
            currentTemp = metrics.temperatureAmbient();
            currentPower = metrics.currentPower();
            energyUsage = metrics.energyUsage();
            heatingActive = metrics.heating();
            windowOpen = metrics.windowOpen();
            powerStatus = metrics.powered();
            targetTemp = metrics.temperature();
        } else {
            currentTemp = null;
            currentPower = null;
            energyUsage = null;
            heatingActive = false;
            windowOpen = false;
            powerStatus = false;
            targetTemp = null;
        }

        final HeaterShadowDTO reported = dto.reported();
        if (reported != null) {
            fanActive = reported.fanActive();
            if (targetTemp == null) {
                targetTemp = reported.temperatureNormal();
            }
        }

        // A heater accepts a setpoint of its own only when it is not following a room program.
        canChangeTemp = room == null
                || (reported != null && HeaterShadowDTO.MODE_CONTROL_INDIVIDUALLY.equals(reported.operationMode()));

        if (room != null && targetTemp == null) {
            targetTemp = room.getTargetTemperature();
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getMacAddress() {
        return macAddress;
    }

    /** Device family, for example {@code Heaters}. Needed when writing settings back. */
    public String getFamily() {
        return family.isEmpty() ? FAMILY_HEATERS : family;
    }

    public boolean isOnline() {
        return online;
    }

    public @Nullable Room getRoom() {
        return room;
    }

    /** True when the heater belongs to no room and is therefore controlled directly. */
    public boolean isIndependent() {
        return room == null;
    }

    public @Nullable Double getCurrentTemp() {
        return currentTemp;
    }

    /** Measured power draw in watts. The old service could not report this. */
    public @Nullable Double getCurrentPower() {
        return currentPower;
    }

    public @Nullable Double getEnergyUsage() {
        return energyUsage;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public boolean windowOpen() {
        return windowOpen;
    }

    public boolean canChangeTemp() {
        return canChangeTemp;
    }

    public @Nullable Double getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(final @Nullable Double targetTemp) {
        this.targetTemp = targetTemp;
    }

    public boolean powerStatus() {
        return powerStatus;
    }

    public void setPowerStatus(final boolean powerStatus) {
        this.powerStatus = powerStatus;
    }

    public boolean fanActive() {
        return fanActive;
    }

    public void setFanActive(final boolean fanActive) {
        this.fanActive = fanActive;
    }

    @Override
    public String toString() {
        final Room localRoom = room;
        return "Heater [id=" + id + ", name=" + name + ", mac=" + macAddress + ", family=" + family + ", online="
                + online + ", room=" + (localRoom == null ? "<independent>" : localRoom.getId()) + ", currentTemp="
                + currentTemp + ", targetTemp=" + targetTemp + ", heating=" + heatingActive + ", power=" + powerStatus
                + ", currentPower=" + currentPower + ", windowOpen=" + windowOpen + "]";
    }
}
