/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.electroluxair.internal.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ElectroluxPureA9DTO} class defines the DTO for the Electrolux Pure A9.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxPureA9DTO {
    private String applianceId = "";
    private ApplianceInfo applianceInfo = new ApplianceInfo();
    private ApplianceData applianceData = new ApplianceData();
    private Properties properties = new Properties();
    private String status = "";
    private String connectionState = "";

    public String getApplianceId() {
        return applianceId;
    }

    public ApplianceInfo getApplianceInfo() {
        return applianceInfo;
    }

    public void setApplianceInfo(ApplianceInfo applianceInfo) {
        this.applianceInfo = applianceInfo;
    }

    public ApplianceData getApplianceData() {
        return applianceData;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getStatus() {
        return status;
    }

    public String getConnectionState() {
        return connectionState;
    }

    public class ApplianceInfo {
        private String manufacturingDateCode = "";
        private String serialNumber = "";
        private String pnc = "";
        private String brand = "";
        private String market = "";
        private String productArea = "";
        private String deviceType = "";
        private String project = "";
        private String model = "";
        private String variant = "";
        private String colour = "";

        public String getManufacturingDateCode() {
            return manufacturingDateCode;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public String getPnc() {
            return pnc;
        }

        public String getBrand() {
            return brand;
        }

        public String getMarket() {
            return market;
        }

        public String getProductArea() {
            return productArea;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getProject() {
            return project;
        }

        public String getModel() {
            return model;
        }

        public String getVariant() {
            return variant;
        }

        public String getColour() {
            return colour;
        }
    }

    class ApplianceData {
        private String applianceName = "";
        private String created = "";
        private String modelName = "";

        public String getApplianceName() {
            return applianceName;
        }

        public String getCreated() {
            return created;
        }

        public String getModelName() {
            return modelName;
        }
    }

    public class Properties {
        private Desired desired = new Desired();
        private Reported reported = new Reported();
        private Object metadata = new Object();

        public Desired getDesired() {
            return desired;
        }

        public Reported getReported() {
            return reported;
        }

        public Object getMetadata() {
            return metadata;
        }
    }

    class Desired {
        @SerializedName("TimeZoneStandardName")
        private String timeZoneStandardName = "";
        @SerializedName("FrmVer_NIU")
        private String frmVerNIU = "";
        @SerializedName("LocationReq")
        private boolean locationReq;
        private Map<String, Object> metadata = new HashMap<>();
        private int version;

        public String getTimeZoneStandardName() {
            return timeZoneStandardName;
        }

        public String getFrmVerNIU() {
            return frmVerNIU;
        }

        public boolean isLocationReq() {
            return locationReq;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public int getVersion() {
            return version;
        }
    }

    public class Reported {
        @SerializedName("FrmVer_NIU")
        private String frmVerNIU = "";
        @SerializedName("Workmode")
        private String workmode = "";
        @SerializedName("FilterRFID")
        private String filterRFID = "";
        @SerializedName("FilterLife")
        private int filterLife = 0;
        @SerializedName("Fanspeed")
        private int fanSpeed = 0;
        @SerializedName("UILight")
        private boolean uiLight = false;
        @SerializedName("SafetyLock")
        private boolean safetyLock = false;
        @SerializedName("Ionizer")
        private boolean ionizer = false;
        @SerializedName("Sleep")
        private boolean sleep = false;
        @SerializedName("Scheduler")
        private boolean scheduler = false;
        @SerializedName("FilterType")
        private int filterType = 0;
        @SerializedName("DspIcoPM2_5")
        private boolean dspIcoPM25 = false;
        @SerializedName("DspIcoPM1")
        private boolean dspIcoPM1 = false;
        @SerializedName("DspIcoPM10")
        private boolean dspIcoPM10 = false;
        @SerializedName("DspIcoTVOC")
        private boolean dspIcoTVOC = false;
        @SerializedName("ErrPM2_5")
        private boolean errPM25 = false;
        @SerializedName("ErrTVOC")
        private boolean errTVOC = false;
        @SerializedName("ErrTempHumidity")
        private boolean errTempHumidity = false;
        @SerializedName("ErrFanMtr")
        private boolean errFanMtr = false;
        @SerializedName("ErrCommSensorDisplayBrd")
        private boolean errCommSensorDisplayBrd = false;
        @SerializedName("DoorOpen")
        private boolean doorOpen = false;
        @SerializedName("ErrRFID")
        private boolean errRFID = false;
        @SerializedName("SignalStrength")
        private String signalStrength = "";
        private Map<String, Object> metadata = new HashMap<>();
        private int version = 0;
        private String deviceId = "";
        @SerializedName("CO2")
        private int co2 = 0;
        @SerializedName("TVOC")
        private int tvoc = 0;
        @SerializedName("Temp")
        private int temp = 0;
        @SerializedName("Humidity")
        private int humidity = 0;
        @SerializedName("RSSI")
        private int rssi = 0;
        @SerializedName("PM1")
        private int pm1 = 0;
        @SerializedName("PM2_5")
        private int pm25 = 0;
        @SerializedName("PM10")
        private int pm10 = 0;

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
            return fanSpeed;
        }

        public boolean isUILight() {
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

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public int getVersion() {
            return version;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public int getCO2() {
            return co2;
        }

        public int getTVOC() {
            return tvoc;
        }

        public int getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }

        public int getRSSI() {
            return rssi;
        }

        public int getPM1() {
            return pm1;
        }

        public int getPM25() {
            return pm25;
        }

        public int getPM10() {
            return pm10;
        }
    }
}
