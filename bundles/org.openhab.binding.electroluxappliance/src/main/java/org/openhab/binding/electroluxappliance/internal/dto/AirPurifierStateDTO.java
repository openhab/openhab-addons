/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirPurifierStateDTO} class defines the DTO for the Electrolux Purifiers.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class AirPurifierStateDTO extends ApplianceStateDTO {

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
        @SerializedName("FrmVer_NIU")
        private String frmVerNIU = "";

        @SerializedName("Workmode")
        private String workmode = "";

        @SerializedName("FilterRFID")
        private String filterRFID = "";

        @SerializedName("FilterLife")
        private int filterLife;

        @SerializedName("Fanspeed")
        private int fanspeed;

        @SerializedName("UILight")
        private boolean uiLight;

        @SerializedName("SafetyLock")
        private boolean safetyLock;

        @SerializedName("Ionizer")
        private boolean ionizer;

        @SerializedName("Sleep")
        private boolean sleep;

        @SerializedName("Scheduler")
        private boolean scheduler;

        @SerializedName("FilterType")
        private int filterType;

        @SerializedName("DspIcoPM2_5")
        private boolean dspIcoPM25;

        @SerializedName("DspIcoPM1")
        private boolean dspIcoPM1;

        @SerializedName("DspIcoPM10")
        private boolean dspIcoPM10;

        @SerializedName("DspIcoTVOC")
        private boolean dspIcoTVOC;

        @SerializedName("ErrPM2_5")
        private boolean errPM25;

        @SerializedName("ErrTVOC")
        private boolean errTVOC;

        @SerializedName("ErrTempHumidity")
        private boolean errTempHumidity;

        @SerializedName("ErrFanMtr")
        private boolean errFanMtr;

        @SerializedName("ErrCommSensorDisplayBrd")
        private boolean errCommSensorDisplayBrd;

        @SerializedName("DoorOpen")
        private boolean doorOpen;

        @SerializedName("ErrRFID")
        private boolean errRFID;

        @SerializedName("SignalStrength")
        private String signalStrength = "";

        @SerializedName("logE")
        private int logE;

        @SerializedName("logW")
        private int logW;

        @SerializedName("InterfaceVer")
        private int interfaceVer;

        @SerializedName("VmNo_NIU")
        private String vmNoNIU = "";

        @SerializedName("TVOCBrand")
        private String tvocBrand = "";

        private Capabilities capabilities = new Capabilities();

        private Tasks tasks = new Tasks();

        @SerializedName("$version")
        private int version;

        private String deviceId = "";

        @SerializedName("CO2")
        private int co2;

        @SerializedName("TVOC")
        private int tvoc;

        @SerializedName("Temp")
        private int temp;

        @SerializedName("Humidity")
        private int humidity;

        @SerializedName("RSSI")
        private int rssi;

        @SerializedName("PM1")
        private int pm1;

        @SerializedName("PM2_5")
        private int pm25;

        @SerializedName("PM10")
        private int pm10;

        @SerializedName("ECO2")
        private int eco2;

        // Getters for all fields
        public String getFrmVerNIU() {
            return frmVerNIU;
        }

        public String getWorkmode() {
            return workmode;
        }

        public String getFilterRFID() {
            return filterRFID;
        }

        public int getFilterLife() {
            return filterLife;
        }

        public int getFanspeed() {
            return fanspeed;
        }

        public boolean isUiLight() {
            return uiLight;
        }

        public boolean isSafetyLock() {
            return safetyLock;
        }

        public boolean isIonizer() {
            return ionizer;
        }

        public boolean isSleep() {
            return sleep;
        }

        public boolean isScheduler() {
            return scheduler;
        }

        public int getFilterType() {
            return filterType;
        }

        public boolean isDspIcoPM25() {
            return dspIcoPM25;
        }

        public boolean isDspIcoPM1() {
            return dspIcoPM1;
        }

        public boolean isDspIcoPM10() {
            return dspIcoPM10;
        }

        public boolean isDspIcoTVOC() {
            return dspIcoTVOC;
        }

        public boolean isErrPM25() {
            return errPM25;
        }

        public boolean isErrTVOC() {
            return errTVOC;
        }

        public boolean isErrTempHumidity() {
            return errTempHumidity;
        }

        public boolean isErrFanMtr() {
            return errFanMtr;
        }

        public boolean isErrCommSensorDisplayBrd() {
            return errCommSensorDisplayBrd;
        }

        public boolean isDoorOpen() {
            return doorOpen;
        }

        public boolean isErrRFID() {
            return errRFID;
        }

        public String getSignalStrength() {
            return signalStrength;
        }

        public int getLogE() {
            return logE;
        }

        public int getLogW() {
            return logW;
        }

        public int getInterfaceVer() {
            return interfaceVer;
        }

        public String getVmNoNIU() {
            return vmNoNIU;
        }

        public String getTvocBrand() {
            return tvocBrand;
        }

        public Capabilities getCapabilities() {
            return capabilities;
        }

        public Tasks getTasks() {
            return tasks;
        }

        public int getVersion() {
            return version;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public int getCo2() {
            return co2;
        }

        public int getTvoc() {
            return tvoc;
        }

        public int getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }

        public int getRssi() {
            return rssi;
        }

        public int getPm1() {
            return pm1;
        }

        public int getPm25() {
            return pm25;
        }

        public int getPm10() {
            return pm10;
        }

        public int getEco2() {
            return eco2;
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
