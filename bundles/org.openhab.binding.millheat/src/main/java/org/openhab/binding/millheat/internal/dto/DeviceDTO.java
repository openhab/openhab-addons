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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A device as returned by {@code GET /houses/&#123;houseId&#125;/devices} and
 * {@code GET /houses/&#123;houseId&#125;/devices/independent}. Both endpoints embed the full
 * settings shadow and last telemetry, so one request per house covers all device state.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record DeviceDTO(String deviceId, @Nullable String macAddress, @Nullable DeviceTypeDTO deviceType,
        @Nullable Boolean isConnected, @Nullable String customName, @Nullable String houseId, @Nullable String roomId,
        @Nullable Boolean isEnabled, @Nullable DeviceSettingsDTO deviceSettings, @Nullable DeviceMetricsDTO lastMetrics,
        @Nullable Double energyUsageForCurrentDay) {

    /** Device family name, for example {@code Heaters}. Empty when the API omitted it. */
    public String family() {
        final DeviceTypeDTO type = deviceType;
        return type == null ? "" : type.parentTypeName();
    }

    /** State the device last reported, or {@code null} when it has never reported. */
    public @Nullable HeaterShadowDTO reported() {
        final DeviceSettingsDTO settings = deviceSettings;
        return settings == null ? null : settings.reported();
    }
}
