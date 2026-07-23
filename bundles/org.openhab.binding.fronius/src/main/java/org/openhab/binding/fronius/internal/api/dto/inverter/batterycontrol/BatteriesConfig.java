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
package org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Record representing the data received from the <code>/config/batteries</code> HTTP endpoint of Fronius hybrid
 * inverters.
 *
 * @author Christian Jonak-Möchel - Initial contribution
 */
public record BatteriesConfig(@SerializedName(SOC_MIN_PARAMETER) @Nullable Integer minSoc,
        @SerializedName(SOC_MAX_PARAMETER) @Nullable Integer maxSoc,
        @SerializedName(BACKUP_RESERVED_CAPACITY_PARAMETER) @Nullable Integer backupReservedCapacity,
        @SerializedName(BACKUP_CRITICAL_SOC_PARAMETER) @Nullable Integer backupCriticalSoc,
        @SerializedName(CHARGE_FROM_GRID_PARAMETER) @Nullable Boolean chargeFromGrid,
        @SerializedName(CALIBRATION_PARAMETER) @Nullable Boolean calibrating) {

    public static final String SOC_MIN_PARAMETER = "BAT_M0_SOC_MIN";
    public static final String SOC_MAX_PARAMETER = "BAT_M0_SOC_MAX";
    public static final String BACKUP_RESERVED_CAPACITY_PARAMETER = "HYB_BACKUP_RESERVED";
    public static final String BACKUP_CRITICAL_SOC_PARAMETER = "HYB_BACKUP_CRITICALSOC";
    public static final String CHARGE_FROM_GRID_PARAMETER = "HYB_EVU_CHARGEFROMGRID";
    public static final String CALIBRATION_PARAMETER = "BAT_CALIBRATION";
}
