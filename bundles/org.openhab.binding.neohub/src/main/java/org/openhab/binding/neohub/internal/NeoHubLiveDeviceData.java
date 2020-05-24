/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

    @Nullable
    @SerializedName("TIMESTAMP_ENGINEERS")
    private BigDecimal timestampEngineers;
    @Nullable
    @SerializedName("TIMESTAMP_SYSTEM")
    private BigDecimal timestampSystem;

    @Nullable
    @SerializedName("devices")
    private List<LiveDataRecord> deviceRecords;

    @SuppressWarnings("null")
    @NonNullByDefault
    public static class LiveDataRecord extends AbstractRecord {

        // "alternate" is a special kludge for technical devices
        @Nullable
        @SerializedName(value = "ZONE_NAME", alternate = { "device" })
        private String deviceName;
        @Nullable
        @SerializedName("SET_TEMP")
        private BigDecimal currentSetTemperature;
        @Nullable
        @SerializedName("ACTUAL_TEMP")
        private BigDecimal currentTemperature;
        @Nullable
        @SerializedName("CURRENT_FLOOR_TEMPERATURE")
        private BigDecimal currentFloorTemperature;
        @Nullable
        @SerializedName("WINDOW_OPEN")
        private Boolean windowOpen;
        @Nullable
        @SerializedName("LOW_BATTERY")
        private Boolean batteryLow;
        @Nullable
        @SerializedName("STANDBY")
        private Boolean standby;
        @Nullable
        @SerializedName("HEATING")
        private Boolean heating;
        @Nullable
        @SerializedName("PREHEAT")
        private Boolean preHeat;
        @Nullable
        @SerializedName("TIMER_ON")
        private Boolean timerOn;
        @Nullable
        @SerializedName("OFFLINE")
        private Boolean offline;
        @Nullable
        @SerializedName("MANUAL_OFF")
        private Boolean manualOff;
        @Nullable
        @SerializedName("MANUAL_ON")
        private Boolean manualOn;

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
    @Nullable
    public static NeoHubLiveDeviceData createDeviceData(String fromJson) throws JsonSyntaxException {
        return GSON.fromJson(fromJson, NeoHubLiveDeviceData.class);
    }

    /**
     * returns the device record corresponding to a given device name
     * 
     * @param deviceName the device name
     * @return its respective device record
     */
    @Nullable
    @Override
    public AbstractRecord getDeviceRecord(String deviceName) {
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
    @Nullable
    @Override
    public List<?> getDevices() {
        return deviceRecords;
    }
}
