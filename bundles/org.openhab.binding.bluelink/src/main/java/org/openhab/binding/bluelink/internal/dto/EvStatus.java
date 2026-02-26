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
public record EvStatus(boolean batteryCharge, double batteryStatus, @SerializedName("batteryPlugin") int rawBatteryPlugin,
        ReserveChargeInfo reservChargeInfos, List<DrivingDistance> drvDistance, ChargeRemainingTime remainTime2) {

    public boolean batteryPlugin() {
        return rawBatteryPlugin > 0;
    }

    public record ReserveChargeInfo(@SerializedName("targetSOClist") List<TargetSOC> targetSocList) {
        public enum PlugType {
            DC,
            AC
        }

        /**
         * Target state of charge setting.
         */
        public record TargetSOC(@SerializedName("plugType") int rawPlugType,
                @SerializedName("targetSOClevel") int targetSocLevel, DrivingRange dte) {
            public PlugType plugType() {
                return rawPlugType == PlugType.DC.ordinal() ? PlugType.DC : PlugType.AC;
            }
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

    public record ChargeRemainingTime(@SerializedName("atc") TimeValue current, @SerializedName("etc1") TimeValue fast,
            @SerializedName("etc2") TimeValue portable, @SerializedName("etc3") TimeValue station) {

        public record TimeValue(int value, int unit) {
        }
    }
}
