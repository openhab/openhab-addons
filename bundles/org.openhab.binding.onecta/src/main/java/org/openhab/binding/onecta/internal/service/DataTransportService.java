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
package org.openhab.binding.onecta.internal.service;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.Enums;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.*;

import com.google.gson.JsonObject;

/**
 *
 * This class is responsible for storing and interpreting the per-unit data obtained from the OnectaConnectionClient.
 * 
 * @author Alexander Drent - Initial contribution
 *
 */
public class DataTransportService {

    private String unitId;
    private Enums.ManagementPoint managementPointType;
    private Unit unit;
    private JsonObject rawData;
    private final OnectaConnectionClient onectaConnectionClient = OnectaConfiguration.getOnectaConnectionClient();

    public DataTransportService(String unitId, Enums.ManagementPoint managementPointType) {
        this.unitId = unitId;
        this.managementPointType = managementPointType;
        this.refreshUnit();
    }

    public void refreshUnit() {
        this.unit = onectaConnectionClient.getUnit(unitId);
        this.rawData = onectaConnectionClient.getRawData(unitId);
    }

    public JsonObject getRawData() {
        return rawData;
    }

    public Boolean isAvailable() {
        return this.unit != null && getManagementPoint(this.managementPointType) != null;
    }

    public Enums.ManagementPoint getManagementPointType() {
        return managementPointType;
    }

    public ManagementPoint getManagementPoint(Enums.ManagementPoint managementPoint) {
        return unit.findManagementPointsByType(managementPoint.getValue());
    }

    public String getEmbeddedId() {
        return getManagementPoint(this.managementPointType).getEmbeddedId();
    }

    public Enums.OperationMode getCurrentOperationMode() {
        return Enums.OperationMode
                .fromValue(getManagementPoint(this.managementPointType).getOperationMode().getValue());
    }

    public Enums.SetpointMode getSetpointMode() {
        return Enums.SetpointMode.fromValue(getManagementPoint(this.managementPointType).getSetpointMode().getValue());
    }

    public Enums.HeatupMode getHeatupMode() {
        return Enums.HeatupMode.fromValue(getManagementPoint(this.managementPointType).getHeatupMode().getValue());
    }

    public void setCurrentOperationMode(Enums.OperationMode value) {
        onectaConnectionClient.setCurrentOperationMode(unitId, managementPointType, value);
    }

    public Enums.FanSpeed getCurrentFanspeed() {
        String fanMode = Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))
                .map(ManagementPoint::getFanControl).map(FanControl::getValue).map(FanControlValue::getOperationModes)
                .map(om -> om.getFanOperationMode(getCurrentOperationMode())).map(FanOnlyClass::getFanSpeed)
                .map(AutoFanSpeed::getCurrentMode).map(FanCurrentMode::getValue).orElse("notavailable");

        fanMode = (fanMode == null) ? "notavailable" : fanMode;

        if (Enums.FanSpeedMode.FIXED.getValue().equals(fanMode)) {
            Integer fanSpeed = Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))
                    .map(ManagementPoint::getFanControl).map(FanControl::getValue)
                    .map(FanControlValue::getOperationModes)
                    .map(om -> om.getFanOperationMode(getCurrentOperationMode())).map(FanOnlyClass::getFanSpeed)
                    .map(AutoFanSpeed::getModes).map(ActionTypesModes::getFixed).map(FanSpeedFixed::getValue)
                    .orElse(null);

            // getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL).getFanControl().getValue()
            // .getOperationModes().getFanOperationMode(getCurrentOperationMode()).getFanSpeed().getModes()
            // .getFixed().getValue();
            return Enums.FanSpeed.fromValue(String.format("%s_%s", fanMode, fanSpeed.toString()));
        }
        return Enums.FanSpeed.fromValue(fanMode);
    }

    public void setFanSpeed(Enums.FanSpeed value) {
        onectaConnectionClient.setFanSpeed(unitId, getEmbeddedId(), getCurrentOperationMode(), value);
    }

    public Enums.FanMovementHor getCurrentFanDirectionHor() {
        String fanMode = Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))
                .map(ManagementPoint::getFanControl).map(FanControl::getValue).map(FanControlValue::getOperationModes)
                .map(om -> om.getFanOperationMode(getCurrentOperationMode())).map(FanOnlyClass::getFanDirection)
                .map(FanDirection::getHorizontal).map(FanMovement::getCurrentMode).map(FanCurrentMode::getValue)
                .orElse(null);
        return Enums.FanMovementHor.fromValue(fanMode);
    }

    public Enums.FanMovementVer getCurrentFanDirectionVer() {
        String fanMode = Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))
                .map(ManagementPoint::getFanControl).map(FanControl::getValue).map(FanControlValue::getOperationModes)
                .map(om -> om.getFanOperationMode(getCurrentOperationMode())).map(FanOnlyClass::getFanDirection)
                .map(FanDirection::getVertical).map(FanMovement::getCurrentMode).map(FanCurrentMode::getValue)
                .orElse(null);
        return Enums.FanMovementVer.fromValue(fanMode);
    }

    public Enums.FanMovement getCurrentFanDirection() {
        String setting = String.format("%s_%s", getCurrentFanDirectionHor().toString(),
                getCurrentFanDirectionVer().toString());
        return switch (setting) {
            case "STOPPED_STOPPED", "NOTAVAILABLE_STOPPED" -> Enums.FanMovement.STOPPED;
            case "SWING_STOPPED" -> Enums.FanMovement.HORIZONTAL;
            case "STOPPED_SWING", "NOTAVAILABLE_SWING" -> Enums.FanMovement.VERTICAL;
            case "SWING_SWING" -> Enums.FanMovement.VERTICAL_AND_HORIZONTAL;
            case "STOPPED_WINDNICE", "NOTAVAILABLE_WINDNICE" -> Enums.FanMovement.VERTICAL_EXTRA;
            case "SWING_WINDNICE" -> Enums.FanMovement.VERTICAL_AND_HORIZONTAL_EXTRA;
            default -> throw new IllegalArgumentException("Invalid fan direction: ");
        };
    }

    public void setCurrentFanDirection(Enums.FanMovement value) {
        onectaConnectionClient.setCurrentFanDirection(unitId, getEmbeddedId(), getCurrentOperationMode(), value);
    }

    public void setCurrentFanDirectionHor(Enums.FanMovementHor value) {
        onectaConnectionClient.setCurrentFanDirectionHor(unitId, getEmbeddedId(), getCurrentOperationMode(), value);
    }

    public void setCurrentFanDirectionVer(Enums.FanMovementVer value) {
        onectaConnectionClient.setCurrentFanDirectionVer(unitId, getEmbeddedId(), getCurrentOperationMode(), value);
    }

    public String getPowerOnOff() {
        return Optional.ofNullable(getManagementPoint(managementPointType)) // .
                .map(ManagementPoint::getOnOffMode) // .
                .map(GatwaySubValueString::getValue) // .
                .orElse(null);
    }

    public String getPowerfulModeOnOff() {
        if (getManagementPoint(managementPointType) != null
                && getManagementPoint(managementPointType).getPowerfulMode() != null) {
            return getManagementPoint(this.managementPointType).getPowerfulMode().getValue();
        } else {
            return null;
        }
    }

    public void setPowerOnOff(Enums.OnOff value) {
        onectaConnectionClient.setPowerOnOff(unitId, managementPointType, value);
    }

    public void setPowerfulModeOnOff(Enums.OnOff value) {
        onectaConnectionClient.setPowerfulModeOnOff(unitId, managementPointType, value);
    }

    public String getEconoMode() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL)) //
                .map(ManagementPoint::getEconoMode) //
                .map(GatwaySubValueString::getValue) //
                .orElse(null);
    }

    public void setEconoMode(Enums.OnOff value) {
        onectaConnectionClient.setEconoMode(unitId, managementPointType, value);
    }

    public String getUnitName() {
        return Optional.ofNullable(getManagementPoint(managementPointType)) //
                .map(ManagementPoint::getNameValue) //
                .orElse(null);
    }

    public Number getCurrentTemperatureSet() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)) //
                .map(ManagementPoint::getTemperatureControl) //
                .map(TemperatureControl::getValue) //
                .map(TemperatureControlValue::getOperationModes) //
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints) //
                .map(Setpoints::getRoomTemperature) //
                .map(IconID::getValue) //
                .orElse(null);
    }

    public void setCurrentTemperatureSet(float value) {
        if (value <= getCurrentTemperatureSetMax().floatValue())
            onectaConnectionClient.setCurrentTemperatureRoomSet(unitId, getEmbeddedId(), getCurrentOperationMode(),
                    value);
    }

    public Number getCurrentTemperatureSetMin() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)) //
                .map(ManagementPoint::getTemperatureControl) //
                .map(TemperatureControl::getValue) //
                .map(TemperatureControlValue::getOperationModes) //
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints) //
                .map(Setpoints::getRoomTemperature) //
                .map(IconID::getMinValue) //
                .orElse(null);
    }

    public Number getCurrentTemperatureSetMax() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)) //
                .map(ManagementPoint::getTemperatureControl) //
                .map(TemperatureControl::getValue) //
                .map(TemperatureControlValue::getOperationModes) //
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints) //
                .map(Setpoints::getRoomTemperature) //
                .map(IconID::getMaxValue) //
                .orElse(null);
    }

    public Number getCurrentTemperatureSetStep() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)) //
                .map(ManagementPoint::getTemperatureControl) //
                .map(TemperatureControl::getValue) //
                .map(TemperatureControlValue::getOperationModes) //
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints) //
                .map(Setpoints::getRoomTemperature) //
                .map(IconID::getStepValue) //
                .orElse(null);
    }

    public Number getCurrentTankTemperatureSet() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)) //
                .map(ManagementPoint::getTemperatureControl) //
                .map(TemperatureControl::getValue) //
                .map(TemperatureControlValue::getOperationModes) //
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints) //
                .map(Setpoints::getdomesticHotWaterTemperature)//
                .map(IconID::getValue) //
                .orElse(null);
    }

    public void setCurrentTankTemperatureSet(float value) {
        if (value <= getCurrentTankTemperatureSetMax().floatValue())
            onectaConnectionClient.setCurrentTemperatureHotWaterSet(unitId, getEmbeddedId(), getCurrentOperationMode(),
                    value);
    }

    public Number getCurrentTankTemperatureSetMin() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getTemperatureControl)//
                .map(TemperatureControl::getValue)//
                .map(TemperatureControlValue::getOperationModes)//
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints)//
                .map(Setpoints::getdomesticHotWaterTemperature)//
                .map(IconID::getMinValue) //
                .orElse(null);
    }

    public Number getCurrentTankTemperatureSetMax() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getTemperatureControl)//
                .map(TemperatureControl::getValue)//
                .map(TemperatureControlValue::getOperationModes)//
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints)//
                .map(Setpoints::getdomesticHotWaterTemperature)//
                .map(IconID::getMaxValue) //
                .orElse(null);
    }

    public Number getCurrentTankTemperatureSetStep() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getTemperatureControl)//
                .map(TemperatureControl::getValue)//
                .map(TemperatureControlValue::getOperationModes)//
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints)//
                .map(Setpoints::getdomesticHotWaterTemperature)//
                .map(IconID::getStepValue) //
                .orElse(null);
    }

    public Number getSetpointLeavingWaterTemperature() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getTemperatureControl)//
                .map(TemperatureControl::getValue)//
                .map(TemperatureControlValue::getOperationModes)//
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints)//
                .map(Setpoints::getLeavingWaterTemperature)//
                .map(IconID::getValue) //
                .orElse(null);
    }

    public void setSetpointLeavingWaterTemperature(float value) {
        onectaConnectionClient.setSetpointLeavingWaterTemperature(unitId, getEmbeddedId(), getCurrentOperationMode(),
                value);
    }

    public Number getSetpointLeavingWaterOffset() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getTemperatureControl)//
                .map(TemperatureControl::getValue)//
                .map(TemperatureControlValue::getOperationModes)//
                .map(modes -> modes.getOperationMode(getCurrentOperationMode())) //
                .map(OpertationMode::getSetpoints)//
                .map(Setpoints::getLeavingWaterOffset)//
                .map(IconID::getValue) //
                .orElse(null);
    }

    public void setSetpointLeavingWaterOffset(float value) {
        onectaConnectionClient.setSetpointLeavingWaterOffset(unitId, getEmbeddedId(), getCurrentOperationMode(), value);
    }

    public Number getIndoorTemperature() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getSensoryData)//
                .map(SensoryData::getValue)//
                .map(SensoryDataValue::getRoomTemperature)//
                .map(IconID::getValue).orElse(null);
    }

    public Number getLeavingWaterTemperature() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getSensoryData)//
                .map(SensoryData::getValue)//
                .map(SensoryDataValue::getLeavingWaterTemperature)//
                .map(IconID::getValue).orElse(null);
    }

    public Number getTankTemperature() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getSensoryData)//
                .map(SensoryData::getValue)//
                .map(SensoryDataValue::getTankTemperature)//
                .map(IconID::getValue).orElse(null);
    }

    public Number getIndoorHumidity() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getSensoryData)//
                .map(SensoryData::getValue)//
                .map(SensoryDataValue::getRoomHumidity)//
                .map(IconID::getValue).orElse(null);
    }

    public ZonedDateTime getTimeStamp() {
        return Optional.ofNullable(ZonedDateTime.parse(unit.getTimestamp()))//
                .orElse(null);
    }

    public Number getOutdoorTemperature() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getSensoryData)//
                .map(SensoryData::getValue)//
                .map(SensoryDataValue::getOutdoorTemperature)//
                .map(IconID::getValue)//
                .orElse(null);
    }

    public Number getTargetTemperatur() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getTargetTemperature)//
                .map(IconID::getValue)//
                .orElse(null);
    }

    public void setTargetTemperatur(float value) {
        onectaConnectionClient.setTargetTemperatur(unitId, getEmbeddedId(), value);
    }

    public Number getTargetTemperaturMax() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getTargetTemperature)//
                .map(IconID::getMaxValue)//
                .orElse(null);
    }

    public Number getTargetTemperaturMin() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getTargetTemperature)//
                .map(IconID::getMinValue)//
                .orElse(null);
    }

    public Number getTargetTemperaturStep() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getTargetTemperature)//
                .map(IconID::getStepValue)//
                .orElse(null);
    }

    public String getStreamerMode() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getStreamerMode)//
                .map(GatwaySubValueString::getValue)//
                .orElse(null);
    }

    public void setStreamerMode(Enums.OnOff value) {
        onectaConnectionClient.setStreamerMode(unitId, getEmbeddedId(), value);
    }

    public String getHolidayMode() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getHolidayMode)//
                .map(HolidayMode::getValue)//
                .orElse(null);
    }

    public Boolean getIsHolidayModeActive() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getisHolidayModeActive)//
                .map(GatwaySubValueBoolean::getValue)//
                .orElse(null);
    }

    public Boolean getIsPowerfulModeActive() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getisHolidayModeActive)//
                .map(GatwaySubValueBoolean::getValue)//
                .orElse(null);
    }

    public void setHolidayMode(Enums.OnOff value) {
        onectaConnectionClient.setHolidayMode(unitId, getEmbeddedId(), value);
    }

    public Enums.DemandControl getDemandControl() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getDemandControl)//
                .map(DemandControl::getValue)//
                .map(DemandControlValue::getCurrentMode).map(GatwaySubValueString::getValue)
                .map(Enums.DemandControl::fromValue)//
                .orElse(null);
    }

    public void setDemandControl(Enums.DemandControl value) {
        onectaConnectionClient.setDemandControl(unitId, getEmbeddedId(), value);
    }

    public Integer getDemandControlFixedValue() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getDemandControl)//
                .map(DemandControl::getValue)//
                .map(DemandControlValue::getModes)//
                .map(DemandControlModes::getFixedValues)//
                .map(DemandControlModesFixed::getValue).orElse(null);
    }

    public Integer getDemandControlFixedStepValue() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getDemandControl)//
                .map(DemandControl::getValue)//
                .map(DemandControlValue::getModes)//
                .map(DemandControlModes::getFixedValues)//
                .map(DemandControlModesFixed::getStepValue)//
                .orElse(null);
    }

    public Integer getDemandControlFixedMinValue() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getDemandControl)//
                .map(DemandControl::getValue)//
                .map(DemandControlValue::getModes)//
                .map(DemandControlModes::getFixedValues)//
                .map(DemandControlModesFixed::getMinValue).orElse(null);
    }

    public Integer getDemandControlFixedMaxValue() {
        return Optional.ofNullable(getManagementPoint(Enums.ManagementPoint.CLIMATECONTROL))//
                .map(ManagementPoint::getDemandControl)//
                .map(DemandControl::getValue)//
                .map(DemandControlValue::getModes)//
                .map(DemandControlModes::getFixedValues)//
                .map(DemandControlModesFixed::getMaxValue).orElse(null);
    }

    public void setDemandControlFixedValue(Integer value) {
        onectaConnectionClient.setDemandControlFixedValue(unitId, getEmbeddedId(), value);
    }

    public Float[] getConsumptionCoolingDay() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getConsumptionData)//
                .map(ConsumptionData::getValue)//
                .map(ConsumptionDataValue::getElectrical)//
                .map(Electrical::getCooling)//
                .map(Ing::getDay)//
                .orElse(null);
    }

    public Float[] getConsumptionCoolingWeek() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getConsumptionData)//
                .map(ConsumptionData::getValue)//
                .map(ConsumptionDataValue::getElectrical)//
                .map(Electrical::getCooling)//
                .map(Ing::getWeek)//
                .orElse(null);
    }

    public Float[] getConsumptionCoolingMonth() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getConsumptionData)//
                .map(ConsumptionData::getValue)//
                .map(ConsumptionDataValue::getElectrical)//
                .map(Electrical::getCooling)//
                .map(Ing::getMonth)//
                .orElse(null);
    }

    public Float[] getConsumptionHeatingDay() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getConsumptionData)//
                .map(ConsumptionData::getValue)//
                .map(ConsumptionDataValue::getElectrical)//
                .map(Electrical::getHeating)//
                .map(Ing::getDay)//
                .orElse(null);
    }

    public Float[] getConsumptionHeatingWeek() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getConsumptionData)//
                .map(ConsumptionData::getValue)//
                .map(ConsumptionDataValue::getElectrical)//
                .map(Electrical::getHeating)//
                .map(Ing::getWeek)//
                .orElse(null);
    }

    public Float[] getConsumptionHeatingMonth() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))//
                .map(ManagementPoint::getConsumptionData)//
                .map(ConsumptionData::getValue)//
                .map(ConsumptionDataValue::getElectrical)//
                .map(Electrical::getHeating)//
                .map(Ing::getMonth)//
                .orElse(null);
    }

    public Boolean getDaylightSavingTimeEnabled() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getDaylightSavingTimeEnabled).map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public String getFirmwareVerion() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getFirmwareVersion).map(GatwaySubValueString::getValue).orElse(null);
    }

    public Boolean getIsFirmwareUpdateSupported() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getIsFirmwareUpdateSupported).map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public Boolean getIsInErrorState() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getIsInErrorState)
                .map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public String getErrorCode() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getErrorCode)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public Boolean getIsInEmergencyState() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getIsInEmergencyState).map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public Boolean getIsInInstallerState() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getIsInInstallerState).map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public Boolean getIsInWarningState() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getIsInWarningState).map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public Boolean getIsLedEnabled() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getIsLedEnabled)
                .map(GatwaySubValueBoolean::getValue).orElse(null);
    }

    public String getRegionCode() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getRegionCode)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getSerialNumber() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getSerialNumber)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getSsid() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getSsid)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getTimeZone() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getTimeZone)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getWifiConectionSSid() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getWifiConnectionSSID).map(GatwaySubValueString::getValue).orElse(null);
    }

    public Integer getWifiConectionStrength() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getWifiConnectionStrength).map(GatwaySubValueInteger::getValue).orElse(null);
    }

    public String getModelInfo() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getModelInfo)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getIpAddress() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getIpAddress)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getMacAddress() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getMacAddress)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getSoftwareVersion() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType))
                .map(ManagementPoint::getSoftwareVersion).map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getEepromVerion() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getEepromVersion)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public String getDryKeepSetting() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getDryKeepSetting)
                .map(GatwaySubValueString::getValue).orElse(null);
    }

    public Number getFanMotorRotationSpeed() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getSensoryData)
                .map(SensoryData::getValue).map(SensoryDataValue::getFanMotorRotationSpeed).map(IconID::getValue)
                .orElse(null);
    }

    public Number getDeltaD() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getSensoryData)
                .map(SensoryData::getValue).map(SensoryDataValue::getDeltaD).map(IconID::getValue).orElse(null);
    }

    public Number getHeatExchangerTemperature() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getSensoryData)
                .map(SensoryData::getValue).map(SensoryDataValue::getHeatExchangerTemperature).map(IconID::getValue)
                .orElse(null);
    }

    public Number getSuctionTemperature() {
        return Optional.ofNullable(getManagementPoint(this.managementPointType)).map(ManagementPoint::getSensoryData)
                .map(SensoryData::getValue).map(SensoryDataValue::getSuctionTemperature).map(IconID::getValue)
                .orElse(null);
    }
}
