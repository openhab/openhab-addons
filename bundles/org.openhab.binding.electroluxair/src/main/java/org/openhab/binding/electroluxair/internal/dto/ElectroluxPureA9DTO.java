/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ElectroluxPureA9DTO} class defines the DTO for the Electrolux Pure A9.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxPureA9DTO {
    public String pncId = "";
    public ApplianceData applianceData = new ApplianceData();
    public AppliancesInfo applicancesInfo = new AppliancesInfo();

    public Twin twin = new Twin();
    public String telemetry = "";

    public String getPncId() {
        return pncId;
    }

    public ApplianceData getApplianceData() {
        return applianceData;
    }

    public AppliancesInfo getApplicancesInfo() {
        return applicancesInfo;
    }

    public void setApplicancesInfo(AppliancesInfo applicancesInfo) {
        this.applicancesInfo = applicancesInfo;
    }

    public Twin getTwin() {
        return twin;
    }

    public String getTelemetry() {
        return telemetry;
    }

    public class $metadata {

        public String $lastUpdated = "";
        public int $lastUpdatedVersion;
        @SerializedName("TimeZoneStandardName")
        public TimeZoneStandardName timeZoneStandardName = new TimeZoneStandardName();
        @SerializedName("FrmVer_NIU")
        public FrmVerNIU frmVerNIU = new FrmVerNIU();
    }

    public class $metadata_ {

        public String $lastUpdated = "";
        @SerializedName("FrmVer_NIU")
        public FrmVerNIU_ frmVerNIU = new FrmVerNIU_();
        @SerializedName("Workmode")
        public Workmode workmode = new Workmode();
        @SerializedName("FilterRFID")
        public FilterRFID filterRFID = new FilterRFID();
        @SerializedName("FilterLife")
        public FilterLife filterLife = new FilterLife();
        @SerializedName("Fanspeed")
        public Fanspeed fanspeed = new Fanspeed();
        @SerializedName("UILight")
        public UILight uILight = new UILight();
        @SerializedName("SafetyLock")
        public SafetyLock safetyLock = new SafetyLock();
        @SerializedName("Ionizer")
        public Ionizer ionizer = new Ionizer();
        @SerializedName("Sleep")
        public Sleep sleep = new Sleep();
        @SerializedName("Scheduler")
        public Scheduler scheduler = new Scheduler();
        @SerializedName("FilterType")
        public FilterType filterType = new FilterType();
        @SerializedName("DspIcoPM2_5")
        public DspIcoPM25 dspIcoPM25 = new DspIcoPM25();
        @SerializedName("DspIcoPM1")
        public DspIcoPM1 dspIcoPM1 = new DspIcoPM1();
        @SerializedName("DspIcoPM10")
        public DspIcoPM10 dspIcoPM10 = new DspIcoPM10();
        @SerializedName("DspIcoTVOC")
        public DspIcoTVOC dspIcoTVOC = new DspIcoTVOC();
        @SerializedName("ErrPM2_5")
        public ErrPM25 errPM25 = new ErrPM25();
        @SerializedName("ErrTVOC")
        public ErrTVOC errTVOC = new ErrTVOC();
        @SerializedName("ErrTempHumidity")
        public ErrTempHumidity errTempHumidity = new ErrTempHumidity();
        @SerializedName("ErrFanMtr")
        public ErrFanMtr errFanMtr = new ErrFanMtr();
        @SerializedName("ErrCommSensorDisplayBrd")
        public ErrCommSensorDisplayBrd errCommSensorDisplayBrd = new ErrCommSensorDisplayBrd();
        @SerializedName("DoorOpen")
        public DoorOpen doorOpen = new DoorOpen();
        @SerializedName("ErrRFID")
        public ErrRFID errRFID = new ErrRFID();
        @SerializedName("SignalStrength")
        public SignalStrength signalStrength = new SignalStrength();
        @SerializedName("PM1")
        public PM1 pM1 = new PM1();
        @SerializedName("PM2_5")
        public PM25 pM25 = new PM25();
        @SerializedName("PM10")
        public PM10 pM10 = new PM10();
        @SerializedName("TVOC")
        public TVOC tVOC = new TVOC();
        @SerializedName("CO2")
        public CO2 cO2 = new CO2();
        @SerializedName("Temp")
        public Temp temp = new Temp();
        @SerializedName("Humidity")
        public Humidity humidity = new Humidity();
        @SerializedName("EnvLightLvl")
        public EnvLightLvl envLightLvl = new EnvLightLvl();
        @SerializedName("RSSI")
        public RSSI rSSI = new RSSI();
    }

    public class ApplianceData {

        public String applianceName = "";
        public String created = "";
        public String modelName = "";
        public String pncId = "";
    }

    public class AppliancesInfo {
        public String brand = "";
        public String colour = "";
        public String device = "";
        public String model = "";
        public String serialNumber = "";
    }

    public class CO2 {
        public String $lastUpdated = "";
    }

    public class Desired {

        @SerializedName("TimeZoneStandardName")
        public String timeZoneStandardName = "";
        @SerializedName("FrmVer_NIU")
        public String frmVerNIU = "";
        public $metadata $metadata = new $metadata();
        public int $version;
    }

    public class DoorOpen {
        public String $lastUpdated = "";
    }

    public class DspIcoPM1 {
        public String $lastUpdated = "";
    }

    public class DspIcoPM10 {

        public String $lastUpdated = "";
    }

    public class DspIcoPM25 {
        public String $lastUpdated = "";
    }

    public class DspIcoTVOC {
        public String $lastUpdated = "";
    }

    public class EnvLightLvl {
        public String $lastUpdated = "";
    }

    public class ErrCommSensorDisplayBrd {
        public String $lastUpdated = "";
    }

    public class ErrFanMtr {

        public String $lastUpdated = "";
    }

    public class ErrPM25 {
        public String $lastUpdated = "";
    }

    public class ErrRFID {
        public String $lastUpdated = "";
    }

    public class ErrTVOC {
        public String $lastUpdated = "";
    }

    public class ErrTempHumidity {
        public String $lastUpdated = "";
    }

    public class Fanspeed {
        public String $lastUpdated = "";
    }

    public class FilterLife {
        public String $lastUpdated = "";
    }

    public class FilterRFID {
        public String $lastUpdated = "";
    }

    public class FilterType {
        public String $lastUpdated = "";
    }

    public class FrmVerNIU {
        public String $lastUpdated = "";
        public int $lastUpdatedVersion;
    }

    public class FrmVerNIU_ {
        public String $lastUpdated = "";
    }

    public class Humidity {
        public String $lastUpdated = "";
    }

    public class Ionizer {
        public String $lastUpdated = "";
    }

    public class PM1 {
        public String $lastUpdated = "";
    }

    public class PM10 {
        public String $lastUpdated = "";
    }

    public class PM25 {
        public String $lastUpdated = "";
    }

    public class Properties {
        public Desired desired = new Desired();
        public Reported reported = new Reported();

        public Reported getReported() {
            return reported;
        }
    }

    public class RSSI {
        public String $lastUpdated = "";
    }

    public class Reported {

        @SerializedName("FrmVer_NIU")
        public String frmVerNIU = "";
        @SerializedName("Workmode")
        public String workmode = "";
        @SerializedName("FilterRFID")
        public String filterRFID = "";
        @SerializedName("FilterLife")
        public int filterLife;
        @SerializedName("Fanspeed")
        public int fanspeed;
        @SerializedName("UILight")
        public boolean uILight;
        @SerializedName("SafetyLock")
        public boolean safetyLock;
        @SerializedName("Ionizer")
        public boolean ionizer;
        @SerializedName("Sleep")
        public boolean sleep;
        @SerializedName("Scheduler")
        public boolean scheduler;
        @SerializedName("FilterType")
        public int filterType;
        @SerializedName("DspIcoPM2_5")
        public boolean dspIcoPM25;
        @SerializedName("DspIcoPM1")
        public boolean dspIcoPM1;
        @SerializedName("DspIcoPM10")
        public boolean dspIcoPM10;
        @SerializedName("DspIcoTVOC")
        public boolean dspIcoTVOC;
        @SerializedName("ErrPM2_5")
        public boolean errPM25;
        @SerializedName("ErrTVOC")
        public boolean errTVOC;
        @SerializedName("ErrTempHumidity")
        public boolean errTempHumidity;
        @SerializedName("ErrFanMtr")
        public boolean errFanMtr;
        @SerializedName("ErrCommSensorDisplayBrd")
        public boolean errCommSensorDisplayBrd;
        @SerializedName("DoorOpen")
        public boolean doorOpen;
        @SerializedName("ErrRFID")
        public boolean errRFID;
        @SerializedName("SignalStrength")
        public String signalStrength = "";
        @SerializedName("$metadata")
        public $metadata_ $metadata = new $metadata_();
        public int $version;
        public String deviceId = "";
        @SerializedName("PM1")
        public int pM1;
        @SerializedName("PM2_5")
        public int pM25;
        @SerializedName("PM10")
        public int pM10;
        @SerializedName("TVOC")
        public int tVOC;
        @SerializedName("CO2")
        public int cO2;
        @SerializedName("Temp")
        public int temp;
        @SerializedName("Humidity")
        public int humidity;
        @SerializedName("EnvLightLvl")
        public int envLightLvl;
        @SerializedName("RSSI")
        public int rSSI;

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

        public boolean isuILight() {
            return uILight;
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

        public int get$version() {
            return $version;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public int getpM1() {
            return pM1;
        }

        public int getpM25() {
            return pM25;
        }

        public int getpM10() {
            return pM10;
        }

        public int gettVOC() {
            return tVOC;
        }

        public int getcO2() {
            return cO2;
        }

        public int getTemp() {
            return temp;
        }

        public int getHumidity() {
            return humidity;
        }

        public int getEnvLightLvl() {
            return envLightLvl;
        }

        public int getrSSI() {
            return rSSI;
        }
    }

    public class SafetyLock {
        public String $lastUpdated = "";
    }

    public class Scheduler {
        public String $lastUpdated = "";
    }

    public class SignalStrength {
        public String $lastUpdated = "";
    }

    public class Sleep {
        public String $lastUpdated = "";
    }

    public class TVOC {
        public String $lastUpdated = "";
    }

    public class Temp {
        public String $lastUpdated = "";
    }

    public class TimeZoneStandardName {
        public String $lastUpdated = "";
        public int $lastUpdatedVersion;
    }

    public class Twin {
        public String deviceId = "";
        public Properties properties = new Properties();
        public String status = "";
        public String connectionState = "";

        public String getDeviceId() {
            return deviceId;
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
    }

    public class UILight {
        public String $lastUpdated = "";
    }

    public class Workmode {
        public String $lastUpdated = "";
    }
}
