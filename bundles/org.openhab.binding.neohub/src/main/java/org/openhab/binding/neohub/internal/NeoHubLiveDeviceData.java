/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * A wrapper around the JSON response to the JSON GET_LIVE_DATA request
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class NeoHubLiveDeviceData extends NeoHubAbstractDeviceData {

    private static final Gson GSON = new Gson();

    @SerializedName("TIMESTAMP_ENGINEERS")
    private @Nullable BigDecimal timestampEngineers;
    @SerializedName("TIMESTAMP_SYSTEM")
    private @Nullable BigDecimal timestampSystem;

    @SerializedName("devices")
    private @Nullable List<LiveDataRecord> deviceRecords;

    @SuppressWarnings("null")
    @NonNullByDefault
    public static class LiveDataRecord extends AbstractRecord {

        // "alternate" is a special kludge for technical devices
        @SerializedName(value = "ZONE_NAME", alternate = { "device" })
        private @Nullable String deviceName;
        @SerializedName("SET_TEMP")
        private @Nullable BigDecimal currentSetTemperature;
        @SerializedName("ACTUAL_TEMP")
        private @Nullable BigDecimal currentTemperature;
        @SerializedName("CURRENT_FLOOR_TEMPERATURE")
        private @Nullable BigDecimal currentFloorTemperature;
        @SerializedName("WINDOW_OPEN")
        private @Nullable Boolean windowOpen;
        @SerializedName("LOW_BATTERY")
        private @Nullable Boolean batteryLow;
        @SerializedName("STANDBY")
        private @Nullable Boolean standby;
        @SerializedName("HEAT_ON")
        private @Nullable Boolean heating;
        @SerializedName("PREHEAT_ACTIVE")
        private @Nullable Boolean preHeat;
        @SerializedName("TIMER_ON")
        private @Nullable Boolean timerOn;
        @SerializedName("OFFLINE")
        private @Nullable Boolean offline;
        @SerializedName("MANUAL_OFF")
        private @Nullable Boolean manualOff;
        @SerializedName("MANUAL_ON")
        private @Nullable Boolean manualOn;

        private boolean safeBoolean(@Nullable Boolean value) {
            return (value == null ? false : value.booleanValue());
        }

        @Override
        public String getDeviceName() {
            String deviceName = this.deviceName;
            return deviceName != null ? deviceName : "";
        }

        @Override
        public BigDecimal getTargetTemperature() {
            return safeBigDecimal(currentSetTemperature);
        }

        @Override
        public BigDecimal getActualTemperature() {
            return safeBigDecimal(currentTemperature);
        }

        @Override
        public BigDecimal getFloorTemperature() {
            return safeBigDecimal(currentFloorTemperature);
        }

        @Override
        public boolean isStandby() {
            return safeBoolean(standby);
        }

        @Override
        public boolean isHeating() {
            return safeBoolean(heating);
        }

        @Override
        public boolean isPreHeating() {
            return safeBoolean(preHeat);
        }

        @Override
        public boolean isTimerOn() {
            return safeBoolean(timerOn);
        }

        @Override
        public boolean offline() {
            return safeBoolean(offline);
        }

        @Override
        public boolean stateManual() {
            return safeBoolean(manualOn);
        }

        @Override
        public boolean stateAuto() {
            return safeBoolean(manualOff);
        }

        @Override
        public boolean isWindowOpen() {
            return safeBoolean(windowOpen);
        }

        @Override
        public boolean isBatteryLow() {
            return safeBoolean(batteryLow);
        }
    }

    public long getTimestampEngineers() {
        BigDecimal timestampEngineers = this.timestampEngineers;
        return timestampEngineers != null ? timestampEngineers.longValue() : 0;
    }

    public long getTimestampSystem() {
        BigDecimal timestampSystem = this.timestampSystem;
        return timestampSystem != null ? timestampSystem.longValue() : 0;
    }

    /**
     * Create wrapper around a JSON string
     * 
     * @param fromJson the JSON string
     * @return a NeoHubGetLiveDataResponse wrapper around the JSON string
     * @throws JsonSyntaxException
     * 
     */
    public static @Nullable NeoHubLiveDeviceData createDeviceData(String fromJson) throws JsonSyntaxException {
        return GSON.fromJson(fromJson, NeoHubLiveDeviceData.class);
    }

    /**
     * returns the device record corresponding to a given device name
     * 
     * @param deviceName the device name
     * @return its respective device record
     */
    @Override
    public @Nullable AbstractRecord getDeviceRecord(String deviceName) {
        List<LiveDataRecord> deviceRecords = this.deviceRecords;
        if (deviceRecords != null) {
            for (AbstractRecord deviceRecord : deviceRecords) {
                if (deviceName.equals(deviceRecord.getDeviceName())) {
                    return deviceRecord;
                }
            }
        }
        return null;
    }

    /**
     * @return the full list of device records
     */
    @Override
    public @Nullable List<LiveDataRecord> getDevices() {
        return deviceRecords;
    }
}
