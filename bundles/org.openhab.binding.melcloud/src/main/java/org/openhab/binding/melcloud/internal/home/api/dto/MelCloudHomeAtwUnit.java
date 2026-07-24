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
package org.openhab.binding.melcloud.internal.home.api.dto;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An Air-to-Water (heat pump) unit, as returned nested under a building's {@code airToWaterUnits} by
 * {@code GET /context} (see ADR-003).
 *
 * <p>
 * As with {@link MelCloudHomeAtaUnit}, dynamic state is read out of {@link #settings}. {@code operationStatus}
 * ("what the 3-way valve is doing right now": {@code Stop}, {@code HotWater}, or a zone mode string) is read-only
 * and distinct from {@code operationModeZone1}/{@code operationModeZone2} (the configured heating strategy).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtwUnit {

    public String id = "";
    public String givenDisplayName = "";
    public @Nullable Integer rssi;
    public MelCloudHomeAtwCapabilities capabilities = new MelCloudHomeAtwCapabilities();
    public List<MelCloudHomeSetting> settings = List.of();
    public @Nullable MelCloudHomeToggleState holidayMode;
    public @Nullable MelCloudHomeToggleState frostProtection;

    public boolean isHolidayModeEnabled() {
        MelCloudHomeToggleState state = holidayMode;
        return state != null && state.enabled;
    }

    public boolean isFrostProtectionEnabled() {
        MelCloudHomeToggleState state = frostProtection;
        return state != null && state.enabled;
    }

    public boolean isPower() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "Power");
    }

    public boolean isInStandbyMode() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "InStandbyMode");
    }

    public String getOperationStatus() {
        return MelCloudHomeSettingsParser.findString(settings, "OperationMode").orElseGet(() -> "Stop");
    }

    public String getOperationModeZone1() {
        return MelCloudHomeSettingsParser.findString(settings, "OperationModeZone1")
                .orElseGet(() -> "HeatRoomTemperature");
    }

    public Optional<Double> getSetTemperatureZone1() {
        return MelCloudHomeSettingsParser.findDouble(settings, "SetTemperatureZone1");
    }

    public Optional<Double> getRoomTemperatureZone1() {
        return MelCloudHomeSettingsParser.findDouble(settings, "RoomTemperatureZone1");
    }

    public boolean hasZone2() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "HasZone2");
    }

    public Optional<String> getOperationModeZone2() {
        return hasZone2() ? MelCloudHomeSettingsParser.findString(settings, "OperationModeZone2") : Optional.empty();
    }

    public Optional<Double> getSetTemperatureZone2() {
        return hasZone2() ? MelCloudHomeSettingsParser.findDouble(settings, "SetTemperatureZone2") : Optional.empty();
    }

    public Optional<Double> getRoomTemperatureZone2() {
        return hasZone2() ? MelCloudHomeSettingsParser.findDouble(settings, "RoomTemperatureZone2") : Optional.empty();
    }

    public Optional<Double> getSetTankWaterTemperature() {
        return MelCloudHomeSettingsParser.findDouble(settings, "SetTankWaterTemperature");
    }

    public Optional<Double> getTankWaterTemperature() {
        return MelCloudHomeSettingsParser.findDouble(settings, "TankWaterTemperature");
    }

    public boolean isForcedHotWaterMode() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "ForcedHotWaterMode");
    }

    public boolean isInError() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "IsInError");
    }

    public Optional<String> getErrorCode() {
        return MelCloudHomeSettingsParser.findString(settings, "ErrorCode");
    }

    public Optional<Double> getOutdoorTemperature() {
        return MelCloudHomeSettingsParser.findDouble(settings, "OutdoorTemperature");
    }
}
