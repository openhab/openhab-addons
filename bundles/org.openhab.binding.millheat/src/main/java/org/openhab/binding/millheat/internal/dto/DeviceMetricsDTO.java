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
 * Last reported telemetry for a device.
 * <p>
 * The API specification models this as a {@code oneOf} with one variant per device family, but the
 * live API does not honour that split: a heater returns the socket variant's field set plus
 * {@code energyUsageSinceLastReport}, {@code houseId}, {@code roomId}, {@code receivedAt} and
 * {@code sentAt}, none of which appear in the specification at all. This record is therefore the
 * permissive union of everything observed, with every field nullable.
 * <p>
 * Note that several logically boolean values are transported as numbers, so they must be compared
 * against zero rather than bound to {@code boolean}.
 *
 * @author Petter L. H. Eide - Initial contribution
 */
public record DeviceMetricsDTO(@Nullable String deviceId, @Nullable String houseId, @Nullable String roomId,
        @Nullable Long time, @Nullable String sentAt, @Nullable String receivedAt, @Nullable Double temperature,
        @Nullable Double temperatureAmbient, @Nullable Double floorTemperature, @Nullable Double humidity,
        @Nullable Double currentPower, @Nullable Double controlSignal, @Nullable Integer currentOperationMode,
        @Nullable Double energyUsage, @Nullable Double energyUsageSinceLastReport,
        @Nullable Long timeSinceHeaterStartup, @Nullable Integer openWindowsStatus,
        @Nullable Integer currentTemperatureTypeInWeeklyProgram, @Nullable Integer heaterFlag,
        @Nullable Integer powerStatus, @Nullable Integer remoteControlTimerMinutesLeft,
        @Nullable Boolean remoteControlEnabled) {

    public boolean heating() {
        final Integer flag = heaterFlag;
        return flag != null && flag > 0;
    }

    public boolean powered() {
        final Integer status = powerStatus;
        return status != null && status > 0;
    }

    /** 0 is disabled, 2 is enabled and triggered, 3 is enabled but not triggered. */
    public boolean windowOpen() {
        final Integer status = openWindowsStatus;
        return status != null && status == 2;
    }
}
