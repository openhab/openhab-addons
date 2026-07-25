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
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An Air-to-Air unit, as returned nested under a building's {@code airToAirUnits} by {@code GET /context}.
 *
 * <p>
 * {@code id}/{@code givenDisplayName}/{@code rssi}/{@code capabilities} are top-level, camelCase JSON fields; every
 * other piece of dynamic state is read out of {@link #settings} via the typed accessor methods below, which also
 * normalize a few known API quirks (fan speed and vertical vane direction are sometimes returned as numeric strings
 * rather than the word form the control endpoint expects; horizontal vane direction is sometimes returned with
 * American instead of British spelling).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeAtaUnit {

    private static final Map<String, String> FAN_SPEED_NUMERIC_TO_WORD = Map.of("0", "Auto", "1", "One", "2", "Two",
            "3", "Three", "4", "Four", "5", "Five");
    private static final Map<String, String> VANE_VERTICAL_NUMERIC_TO_WORD = Map.of("0", "Auto", "1", "One", "2", "Two",
            "3", "Three", "4", "Four", "5", "Five", "7", "Swing");
    private static final Map<String, String> VANE_HORIZONTAL_AMERICAN_TO_BRITISH = Map.of("CenterLeft", "LeftCentre",
            "Center", "Centre", "CenterRight", "RightCentre");

    public String id = "";
    public String givenDisplayName = "";
    public @Nullable Integer rssi;
    public MelCloudHomeAtaCapabilities capabilities = new MelCloudHomeAtaCapabilities();
    public List<MelCloudHomeSetting> settings = List.of();

    public boolean isPower() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "Power");
    }

    public String getOperationMode() {
        return MelCloudHomeSettingsParser.findString(settings, "OperationMode").orElseGet(() -> "Heat");
    }

    public Optional<Double> getSetTemperature() {
        return MelCloudHomeSettingsParser.findDouble(settings, "SetTemperature");
    }

    public Optional<Double> getRoomTemperature() {
        return MelCloudHomeSettingsParser.findDouble(settings, "RoomTemperature");
    }

    public Optional<String> getFanSpeed() {
        return MelCloudHomeSettingsParser.findString(settings, "SetFanSpeed")
                .map(value -> FAN_SPEED_NUMERIC_TO_WORD.getOrDefault(value, value));
    }

    public Optional<String> getVaneVerticalDirection() {
        return MelCloudHomeSettingsParser.findString(settings, "VaneVerticalDirection")
                .map(value -> VANE_VERTICAL_NUMERIC_TO_WORD.getOrDefault(value, value));
    }

    public Optional<String> getVaneHorizontalDirection() {
        return MelCloudHomeSettingsParser.findString(settings, "VaneHorizontalDirection")
                .map(value -> VANE_HORIZONTAL_AMERICAN_TO_BRITISH.getOrDefault(value, value));
    }

    public boolean isInStandbyMode() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "InStandbyMode");
    }

    public boolean isInError() {
        return MelCloudHomeSettingsParser.findBoolean(settings, "IsInError");
    }

    public Optional<String> getErrorCode() {
        return MelCloudHomeSettingsParser.findString(settings, "ErrorCode");
    }
}
