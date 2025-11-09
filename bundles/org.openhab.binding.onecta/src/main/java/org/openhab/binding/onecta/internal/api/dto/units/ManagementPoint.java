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
package org.openhab.binding.onecta.internal.api.dto.units;

/**
 * @author Alexander Drent - Initial contribution
 */
public class ManagementPoint {
    private String embeddedId;
    private String managementPointType;
    private String managementPointSubType;
    private String managementPointCategory;
    private GatwaySubValueBoolean daylightSavingTimeEnabled;
    private GatwaySubValueString errorCode;
    private GatwaySubValueString firmwareVersion;
    private GatwaySubValueBoolean isFirmwareUpdateSupported;
    private GatwaySubValueBoolean isInErrorState;
    private GatwaySubValueBoolean ledEnabled;
    private GatwaySubValueString ipAddress;
    private GatwaySubValueString macAddress;
    private GatwaySubValueString modelInfo;
    private GatwaySubValueString regionCode;
    private GatwaySubValueString serialNumber;
    private GatwaySubValueString eepromVersion;
    private GatwaySubValueString ssid;
    private GatwaySubValueString timeZone;
    private GatwaySubValueString wifiConnectionSSID;
    private GatwaySubValueInteger wifiConnectionStrength;
    private ConsumptionData consumptionData;
    private DemandControl demandControl;
    private GatwaySubValueString econoMode;
    private FanControl fanControl;
    private HolidayMode holidayMode;
    private GatwaySubValueBoolean isHolidayModeActive;
    private GatwaySubValueBoolean isInEmergencyState;
    private GatwaySubValueString heatupMode;
    private GatwaySubValueString setpointMode;
    private GatwaySubValueBoolean isInWarningState;
    private GatwaySubValueBoolean isInInstallerState;
    private Name name;
    private GatwaySubValueString onOffMode;
    private GatwaySubValueString operationMode;
    private IconID targetTemperature;
    private GatwaySubValueString outdoorSilentMode;
    private GatwaySubValueString powerfulMode;
    private GatwaySubValueBoolean isPowerfulModeActive;
    private SensoryData sensoryData;
    private GatwaySubValueString streamerMode;
    private TemperatureControl temperatureControl;
    private GatwaySubValueString softwareVersion;
    private GatwaySubValueString dryKeepSetting;

    public String getEmbeddedId() {
        return embeddedId;
    }

    public String getManagementPointType() {
        return managementPointType;
    }

    public String getManagementPointSubType() {
        return managementPointSubType;
    }

    public String getManagementPointCategory() {
        return managementPointCategory;
    }

    public GatwaySubValueBoolean getDaylightSavingTimeEnabled() {
        return daylightSavingTimeEnabled;
    }

    public GatwaySubValueString getErrorCode() {
        return errorCode;
    }

    public GatwaySubValueString getFirmwareVersion() {
        return firmwareVersion;
    }

    public GatwaySubValueBoolean getIsFirmwareUpdateSupported() {
        return isFirmwareUpdateSupported;
    }

    public GatwaySubValueBoolean getIsInErrorState() {
        return isInErrorState;
    }

    public GatwaySubValueBoolean getisInWarningState() {
        return isInWarningState;
    }

    public GatwaySubValueBoolean getIsInInstallerState() {
        return isInInstallerState;
    }

    public GatwaySubValueBoolean getIsInWarningState() {
        return isInWarningState;
    }

    public GatwaySubValueBoolean getIsLedEnabled() {
        return ledEnabled;
    }

    public GatwaySubValueString getIpAddress() {
        return ipAddress;
    }

    public GatwaySubValueString getMacAddress() {
        return macAddress;
    }

    public GatwaySubValueString getModelInfo() {
        return modelInfo;
    }

    public GatwaySubValueString getRegionCode() {
        return regionCode;
    }

    public GatwaySubValueString getSerialNumber() {
        return serialNumber;
    }

    public GatwaySubValueString getSsid() {
        return ssid;
    }

    public GatwaySubValueString getTimeZone() {
        return timeZone;
    }

    public GatwaySubValueString getWifiConnectionSSID() {
        return wifiConnectionSSID;
    }

    public GatwaySubValueInteger getWifiConnectionStrength() {
        return wifiConnectionStrength;
    }

    public ConsumptionData getConsumptionData() {
        return consumptionData;
    }

    public DemandControl getDemandControl() {
        return demandControl;
    }

    public HolidayMode getHolidayMode() {
        return holidayMode;
    }

    public GatwaySubValueBoolean getisHolidayModeActive() {
        return isHolidayModeActive;
    }

    public GatwaySubValueBoolean getIsInEmergencyState() {
        return isInEmergencyState;
    }

    public Name getName() {
        return name;
    }

    public String getNameValue() {
        return name != null ? name.getValue() : "";
    }

    public GatwaySubValueString getOnOffMode() {
        return onOffMode;
    }

    public GatwaySubValueString getOperationMode() {
        return operationMode;
    }

    public GatwaySubValueString getHeatupMode() {
        return heatupMode;
    }

    public GatwaySubValueString getSetpointMode() {
        return setpointMode;
    }

    public GatwaySubValueString getOutdoorSilentMode() {
        return outdoorSilentMode;
    }

    public GatwaySubValueString getPowerfulMode() {
        return powerfulMode;
    }

    public GatwaySubValueBoolean getIsPowerfulModeActive() {
        return isPowerfulModeActive;
    }

    public SensoryData getSensoryData() {
        return sensoryData;
    }

    public GatwaySubValueString getStreamerMode() {
        return streamerMode;
    }

    public TemperatureControl getTemperatureControl() {
        return temperatureControl;
    }

    public FanControl getFanControl() {
        return fanControl;
    }

    public GatwaySubValueString getEconoMode() {
        return econoMode;
    }

    public GatwaySubValueString getEepromVersion() {
        return eepromVersion;
    }

    public GatwaySubValueString getSoftwareVersion() {
        return softwareVersion;
    }

    public GatwaySubValueString getDryKeepSetting() {
        return dryKeepSetting;
    }
}
