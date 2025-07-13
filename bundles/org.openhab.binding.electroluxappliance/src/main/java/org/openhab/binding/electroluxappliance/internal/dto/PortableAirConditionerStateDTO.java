/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.electroluxappliance.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PortableAirConditionerStateDTO} class defines the DTO for the Electrolux Purifiers.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class PortableAirConditionerStateDTO extends ApplianceStateDTO {

    private static final String NOT_READ_STRING = "DATA NOT READ FROM API";

    @SerializedName("properties")
    private Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    // Inner class for Properties
    public static class Properties {
        @SerializedName("reported")
        private Reported reported = new Reported();

        public Reported getReported() {
            return reported;
        }
    }

    // Inner class for Reported properties
    public static class Reported {

        @SerializedName("VmNo_MCU")
        private String vmNoMCU = NOT_READ_STRING;

        public boolean getIsReadVmNoMCU() {
            return !NOT_READ_STRING.equals(vmNoMCU);
        }

        public String getVmNoMCU() {
            return vmNoMCU;
        }

        @SerializedName("VmNo_NIU")
        private String getVmNoNIO = NOT_READ_STRING;

        public boolean getIsReadVmNoNIO() {
            return !NOT_READ_STRING.equals(getVmNoNIO);
        }

        public String getVmNoNIO() {
            return getVmNoNIO;
        }

        @SerializedName("networkInterface")
        public NetworkInterface networkInterface = new NetworkInterface();

        public NetworkInterface getNetworkInterface() {
            return networkInterface;
        }

        @SerializedName("applianceState")
        private String applianceState = NOT_READ_STRING;

        public String getApplianceState() {
            return applianceState;
        }

        public boolean getIsReadApplianceState() {
            return !NOT_READ_STRING.equals(applianceState);
        }

        public boolean getApplicanceStateRunning() {
            return "running".equals(applianceState);
        }

        @SerializedName("startTime")
        private int startTime = Integer.MIN_VALUE;

        public boolean getIsReadStartTime() {
            return startTime != Integer.MIN_VALUE;
        }

        public int getStartTime() {
            return startTime;
        }

        @SerializedName("stopTime")
        private int stopTime = Integer.MIN_VALUE;

        public boolean getIsReadStopTime() {
            return stopTime != Integer.MIN_VALUE;
        }

        public int getStopTime() {
            return stopTime;
        }

        @SerializedName("temperatureRepresentation")
        private String temperatureRepresentation = "";

        public String getTemperatureRepresentation() {
            return temperatureRepresentation;
        }

        @SerializedName("sleepMode")
        private String sleepMode = NOT_READ_STRING;

        public boolean getIsReadSleepMode() {
            return !NOT_READ_STRING.equals(sleepMode);
        }

        public String getSleepMode() {
            return sleepMode;
        }

        public boolean getSleepModeOn() {
            return "on".equals(sleepMode);
        }

        @SerializedName("targetTemperatureC")
        private Integer targetTemperatureC = Integer.MIN_VALUE;

        public int getTargetTemperature() {
            return targetTemperatureC;
        }

        public boolean getIsReadTargetTemperature() {
            return targetTemperatureC != Integer.MIN_VALUE;
        }

        @SerializedName("uiLockMode")
        private @Nullable Boolean uiLockMode = null;

        public boolean getIsReadUiLockMode() {
            return uiLockMode != null;
        }

        public boolean getUiLockModeOn() {
            final @Nullable Boolean lockMode = uiLockMode;
            return lockMode != null && lockMode;
        }

        @SerializedName("mode")
        private String mode = NOT_READ_STRING;

        public boolean getIsReadMode() {
            return !NOT_READ_STRING.equals(mode);
        }

        public String getMode() {
            return mode;
        }

        @SerializedName("fanSpeedSetting")
        private String fanSpeedSetting = NOT_READ_STRING;

        public boolean getIsReadFanSpeedSetting() {
            return !NOT_READ_STRING.equals(fanSpeedSetting);
        }

        public String getFanSpeedSetting() {
            return fanSpeedSetting;
        }

        @SerializedName("verticalSwing")
        private String verticalSwing = NOT_READ_STRING;

        public boolean getIsReadVerticalSwing() {
            return !NOT_READ_STRING.equals(verticalSwing);
        }

        public String getVerticalSwing() {
            return verticalSwing;
        }

        public boolean getVerticalSwingOn() {
            return "on".equalsIgnoreCase(verticalSwing);
        }

        @SerializedName("filterState")
        private String filterState = NOT_READ_STRING;

        public boolean getIsReadFilterState() {
            return !NOT_READ_STRING.equals(filterState);
        }

        public String getFilterState() {
            return filterState;
        }

        @SerializedName("dataModelVersion")
        private String dataModelVersion = "";

        @SerializedName("schedulerSession")
        private String schedulerSession = "";

        @SerializedName("schedulerMode")
        private String schedulerMode = "";

        @SerializedName("upgradeState")
        private String upgradeState = "";

        @SerializedName("TimeZoneDaylightRule")
        private String timeZoneDaylightRule = "";

        @SerializedName("TimeZoneStandardName")
        private String timeZoneStandardName = "";

        @SerializedName("logW")
        private long logW;

        @SerializedName("$version")
        private long version;

        @SerializedName("deviceId")
        private String deviceId = "";

        @SerializedName("ambientTemperatureC")
        private Integer ambientTemperatureC = Integer.MIN_VALUE;

        public int getAmbientTemperatureC() {
            return ambientTemperatureC;
        }

        public boolean getIsReadAmbientTemperatureC() {
            return ambientTemperatureC != Integer.MIN_VALUE;
        }

        @SerializedName("ambientTemperatureF")
        private Integer ambientTemperatureF = Integer.MIN_VALUE;

        public int getAmbientTemperatureF() {
            return ambientTemperatureF;
        }

        public boolean getIsReadAmbientTemperatureF() {
            return ambientTemperatureF != Integer.MIN_VALUE;
        }

        @SerializedName("compressorState")
        private String compressorState = NOT_READ_STRING;

        public boolean getIsReadCompressorState() {
            return !NOT_READ_STRING.equals(compressorState);
        }

        public boolean getCompressorStateOn() {
            return "on".equals(compressorState);
        }

        @SerializedName("totalRuntime")
        private long totalRuntime;

        @SerializedName("compressorCoolingRuntime")
        private long compressorCoolingRuntime;

        @SerializedName("compressorHeatingRuntime")
        private long compressorHeatingRuntime;

        @SerializedName("filterRuntime")
        private long filterRuntime;

        @SerializedName("hepaFilterLifeTime")
        private long hepaFilterLifeTime;

        @SerializedName("fourWayValveState")
        private String fourWayValveState = NOT_READ_STRING;

        public boolean getIsReadFourWayValveState() {
            return !NOT_READ_STRING.equals(fourWayValveState);
        }

        public boolean getFourWayValveStateOn() {
            return "on".equals(fourWayValveState);
        }

        @SerializedName("evapDefrostState")
        private String evapDefrostState = NOT_READ_STRING;

        public boolean getIsReadEvapDefrostState() {
            return !NOT_READ_STRING.equals(evapDefrostState);
        }

        public boolean getEvapDefrostStateOn() {
            return "on".equals(evapDefrostState);
        }

        // private Capabilities capabilities = new Capabilities();

        // private Tasks tasks = new Tasks();
    }

    // Inner class for NetworkInterface
    public static class NetworkInterface {
        @SerializedName("linkQualityIndicator")
        private String linkQualityIndicator = NOT_READ_STRING;

        public boolean getIsReadLinkQualityIndicator() {
            return !NOT_READ_STRING.equals(linkQualityIndicator);
        }

        public String getLinkQualityIndicator() {
            return linkQualityIndicator;
        }

        @SerializedName("rssi")
        private Integer rssi = Integer.MIN_VALUE;

        public boolean getIsReadRSSI() {
            return Integer.MIN_VALUE != rssi;
        }

        public int getRSSI() {
            return rssi;
        }
    }

    // Inner class for Capabilities
    public static class Capabilities {
        @SerializedName("tasks")
        private Tasks tasks = new Tasks();

        public Tasks getTasks() {
            return tasks;
        }
    }

    // Inner class for Tasks (assuming it's empty as shown in the JSON)
    public static class Tasks {
        // No fields; can be extended as needed
    }
}
