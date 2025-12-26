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
package org.openhab.binding.bluelink.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * EV-specific status.
 *
 * @author Marcus Better - Initial contribution
 */
public record EvStatus(boolean batteryCharge, int batteryStatus, int batteryPlugin, ReserveChargeInfo reservChargeInfos,
        List<DrivingDistance> drvDistance, ChargeRemainingTime remainTime2) {

    public record ReserveChargeInfo(@SerializedName("targetSOCList") List<TargetSOC> targetSocList) {
        /**
         * Target state of charge setting.
         */
        public record TargetSOC(int plugType, // 0 = DC, 1 = AC
                @SerializedName("targetSOCLevel") int targetSocLevel) {
        }
    }

    public record DrivingDistance(RangeByFuel rangeByFuel) {

        /**
         * Range by fuel type.
         */
        public record RangeByFuel(DrivingRange totalAvailableRange, DrivingRange evModeRange,
                DrivingRange gasModeRange) {
        }
    }

    public record ChargeRemainingTime(
            // Current
            TimeValue atc,
            // Fast
            TimeValue etc1,
            // Portable
            TimeValue etc2,
            // Station
            TimeValue etc3) {

        public record TimeValue(int value, int unit) {
        }
    }
}
