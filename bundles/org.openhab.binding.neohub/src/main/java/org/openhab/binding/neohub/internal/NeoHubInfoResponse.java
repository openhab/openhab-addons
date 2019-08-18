/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * A wrapper around the JSON response to the JSON INFO request
 *
 * @author Sebastian Prehn - Initial contribution
 * @author Andrew Fiddian-Green - Refactoring for openHAB v2.x
 * 
 */
class NeoHubInfoResponse {

    private static final Gson GSON = new Gson();

    @SerializedName("devices")
    private List<DeviceInfo> deviceInfos;

    static class StatMode {
        @SerializedName("MANUAL_OFF")
        private Boolean manualOff;
        @SerializedName("MANUAL_ON")
        private Boolean manualOn;

        private Boolean stateManualOn() {
            return (manualOn != null && manualOn);
        }

        private Boolean stateManualOff() {
            return (manualOff != null && manualOff);
        }
    }

    static class DeviceInfo {

        @SerializedName("device")
        private String deviceName;
        @SerializedName("CURRENT_SET_TEMPERATURE")
        private BigDecimal currentSetTemperature;
        @SerializedName("CURRENT_TEMPERATURE")
        private BigDecimal currentTemperature;
        @SerializedName("CURRENT_FLOOR_TEMPERATURE")
        private BigDecimal currentFloorTemperature;
        @SerializedName("AWAY")
        private Boolean away;
        @SerializedName("HOLIDAY")
        private Boolean holiday;
        @SerializedName("HOLIDAY_DAYS")
        private BigDecimal holidayDays;
        @SerializedName("STANDBY")
        private Boolean standby;
        @SerializedName("HEATING")
        private Boolean heating;
        @SerializedName("PREHEAT")
        private Boolean preHeat;
        @SerializedName("TIMER")
        private Boolean timerOn;
        @SerializedName("DEVICE_TYPE")
        private BigDecimal deviceType;
        @SerializedName("OFFLINE")
        private Boolean offline;
        @SerializedName("STAT_MODE")
        private StatMode statMode = new StatMode();

        protected Boolean safeBoolean(Boolean value) {
            return (value != null && value);
        }

        protected BigDecimal safeBigDecimal(BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
        }

        public String getDeviceName() {
            return deviceName != null ? deviceName : "";
        }

        public BigDecimal getTargetTemperature() {
            return safeBigDecimal(currentSetTemperature);
        }

        public BigDecimal getRoomTemperature() {
            return safeBigDecimal(currentTemperature);
        }

        public BigDecimal getFloorTemperature() {
            return safeBigDecimal(currentFloorTemperature);
        }

        public Boolean isAway() {
            return safeBoolean(away);
        }

        public Boolean isHoliday() {
            return safeBoolean(holiday);
        }

        public BigDecimal getHolidayDays() {
            return safeBigDecimal(holidayDays);
        }

        public BigDecimal getDeviceType() {
            return safeBigDecimal(deviceType);
        }

        public Boolean isStandby() {
            return safeBoolean(standby);
        }

        public Boolean isHeating() {
            return safeBoolean(heating);
        }

        public Boolean isPreHeating() {
            return safeBoolean(preHeat);
        }

        public Boolean isTimerOn() {
            return safeBoolean(timerOn);
        }

        public Boolean isOffline() {
            return safeBoolean(offline);
        }

        public Boolean stateManual() {
            return (statMode != null && statMode.stateManualOn());
        }

        public Boolean stateAuto() {
            return (statMode != null && statMode.stateManualOff());
        }

        public Boolean hasStatMode() {
            return statMode != null;
        }

    }

    /**
     * Create wrapper around the JSON response
     * 
     * @param response the JSON INFO request
     * @return a NeoHubInfoResponse wrapper around the JSON response
     * @throws JsonSyntaxException
     * 
     */
    static @Nullable NeoHubInfoResponse createInfoResponse(String response) throws JsonSyntaxException {
        return GSON.fromJson(response, NeoHubInfoResponse.class);
    }

    /*
     * returns the DeviceInfo corresponding to a given device name
     * 
     * @param deviceName the device name
     * 
     * @return its respective DeviceInfo
     */
    public DeviceInfo getDeviceInfo(String deviceName) {
        for (DeviceInfo d : deviceInfos) {
            if (deviceName.equals(d.getDeviceName())) {
                return d;
            }
        }
        return null;
    }

    /*
     * @return the full list of DeviceInfo objects
     */
    public List<DeviceInfo> getDevices() {
        return deviceInfos;
    }
}
