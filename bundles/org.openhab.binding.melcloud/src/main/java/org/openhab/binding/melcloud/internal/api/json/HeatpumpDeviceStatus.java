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
package org.openhab.binding.melcloud.internal.api.json;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * {@link HeatpumpDeviceStatus} is the JSON data we receive from the MELCloud API
 * when performing a request to DeviceType 1.
 * Generated with jsonschema2pojo
 *
 * @author Wietse van Buitenen - Initial contribution
 */
public class HeatpumpDeviceStatus {
    @Expose
    private Long effectiveFlags;

    @Expose(serialize = false, deserialize = true)
    private Object localIPAddress;

    @Expose
    private Double setTemperatureZone1;

    @Expose
    private Double setTemperatureZone2;

    @Expose(serialize = false, deserialize = true)
    private Double roomTemperatureZone1;

    @Expose(serialize = false, deserialize = true)
    private Double roomTemperatureZone2;

    @Expose
    private Integer operationMode;

    @Expose
    private Integer operationModeZone1;

    @Expose
    private Integer operationModeZone2;

    @Expose(serialize = false, deserialize = true)
    private List<WeatherObservation> weatherObservations = null;

    @Expose(serialize = false, deserialize = true)
    private Object errorMessage;

    @Expose(serialize = false, deserialize = true)
    private Integer errorCode;

    @Expose
    private Double setHeatFlowTemperatureZone1;

    @Expose
    private Double setHeatFlowTemperatureZone2;

    @Expose
    private Double setCoolFlowTemperatureZone1;

    @Expose
    private Double setCoolFlowTemperatureZone2;

    @Expose
    private Integer hCControlType;

    @Expose(serialize = false, deserialize = true)
    private Double tankWaterTemperature;

    @Expose
    private Double setTankWaterTemperature;

    @Expose
    private Boolean forcedHotWaterMode;

    @Expose
    private Integer unitStatus;

    @Expose
    private Double outdoorTemperature;

    @Expose
    private Boolean ecoHotWater;

    @Expose
    private Object zone1Name;

    @Expose
    private Object zone2Name;

    @Expose
    private Boolean holidayMode;

    @Expose
    private Boolean prohibitZone1;

    @Expose
    private Boolean prohibitZone2;

    @Expose
    private Boolean prohibitHotWater;

    @Expose
    private Integer temperatureIncrementOverride;

    @Expose
    private Boolean idleZone1;

    @Expose
    private Boolean idleZone2;

    @Expose
    private Integer deviceID;

    @Expose
    private Integer deviceType;

    @Expose(serialize = false, deserialize = true)
    private String lastCommunication;

    @Expose(serialize = false, deserialize = true)
    private String nextCommunication;

    @Expose
    private Boolean power;

    @Expose(serialize = false, deserialize = true)
    private Boolean hasPendingCommand;

    @Expose(serialize = false, deserialize = true)
    private Boolean offline;

    @Expose
    private Object scene;

    @Expose
    private Object sceneOwner;

    public Long getEffectiveFlags() {
        return effectiveFlags;
    }

    public void setEffectiveFlags(Long effectiveFlags) {
        this.effectiveFlags = effectiveFlags;
    }

    public Object getLocalIPAddress() {
        return localIPAddress;
    }

    public void setLocalIPAddress(Object localIPAddress) {
        this.localIPAddress = localIPAddress;
    }

    public Double getSetTemperatureZone1() {
        return setTemperatureZone1;
    }

    public void setSetTemperatureZone1(Double setTemperatureZone1) {
        this.setTemperatureZone1 = setTemperatureZone1;
    }

    public Double getSetTemperatureZone2() {
        return setTemperatureZone2;
    }

    public void setSetTemperatureZone2(Double setTemperatureZone2) {
        this.setTemperatureZone2 = setTemperatureZone2;
    }

    public Double getRoomTemperatureZone1() {
        return roomTemperatureZone1;
    }

    public void setRoomTemperatureZone1(Double roomTemperatureZone1) {
        this.roomTemperatureZone1 = roomTemperatureZone1;
    }

    public Double getRoomTemperatureZone2() {
        return roomTemperatureZone2;
    }

    public void setRoomTemperatureZone2(Double roomTemperatureZone2) {
        this.roomTemperatureZone2 = roomTemperatureZone2;
    }

    public Integer getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(Integer operationMode) {
        this.operationMode = operationMode;
    }

    public Integer getOperationModeZone1() {
        return operationModeZone1;
    }

    public void setOperationModeZone1(Integer operationModeZone1) {
        this.operationModeZone1 = operationModeZone1;
    }

    public Integer getOperationModeZone2() {
        return operationModeZone2;
    }

    public void setOperationModeZone2(Integer operationModeZone2) {
        this.operationModeZone2 = operationModeZone2;
    }

    public List<WeatherObservation> getWeatherObservations() {
        return weatherObservations;
    }

    public void setWeatherObservations(List<WeatherObservation> weatherObservations) {
        this.weatherObservations = weatherObservations;
    }

    public Object getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Object errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Double getSetHeatFlowTemperatureZone1() {
        return setHeatFlowTemperatureZone1;
    }

    public void setSetHeatFlowTemperatureZone1(Double setHeatFlowTemperatureZone1) {
        this.setHeatFlowTemperatureZone1 = setHeatFlowTemperatureZone1;
    }

    public Double getSetHeatFlowTemperatureZone2() {
        return setHeatFlowTemperatureZone2;
    }

    public void setSetHeatFlowTemperatureZone2(Double setHeatFlowTemperatureZone2) {
        this.setHeatFlowTemperatureZone2 = setHeatFlowTemperatureZone2;
    }

    public Double getSetCoolFlowTemperatureZone1() {
        return setCoolFlowTemperatureZone1;
    }

    public void setSetCoolFlowTemperatureZone1(Double setCoolFlowTemperatureZone1) {
        this.setCoolFlowTemperatureZone1 = setCoolFlowTemperatureZone1;
    }

    public Double getSetCoolFlowTemperatureZone2() {
        return setCoolFlowTemperatureZone2;
    }

    public void setSetCoolFlowTemperatureZone2(Double setCoolFlowTemperatureZone2) {
        this.setCoolFlowTemperatureZone2 = setCoolFlowTemperatureZone2;
    }

    public Integer getHCControlType() {
        return hCControlType;
    }

    public void setHCControlType(Integer hCControlType) {
        this.hCControlType = hCControlType;
    }

    public Double getTankWaterTemperature() {
        return tankWaterTemperature;
    }

    public void setTankWaterTemperature(Double tankWaterTemperature) {
        this.tankWaterTemperature = tankWaterTemperature;
    }

    public Double getSetTankWaterTemperature() {
        return setTankWaterTemperature;
    }

    public void setSetTankWaterTemperature(Double setTankWaterTemperature) {
        this.setTankWaterTemperature = setTankWaterTemperature;
    }

    public Boolean getForcedHotWaterMode() {
        return forcedHotWaterMode;
    }

    public void setForcedHotWaterMode(Boolean forcedHotWaterMode) {
        this.forcedHotWaterMode = forcedHotWaterMode;
    }

    public Integer getUnitStatus() {
        return unitStatus;
    }

    public void setUnitStatus(Integer unitStatus) {
        this.unitStatus = unitStatus;
    }

    public Double getOutdoorTemperature() {
        return outdoorTemperature;
    }

    public void setOutdoorTemperature(Double outdoorTemperature) {
        this.outdoorTemperature = outdoorTemperature;
    }

    public Boolean getEcoHotWater() {
        return ecoHotWater;
    }

    public void setEcoHotWater(Boolean ecoHotWater) {
        this.ecoHotWater = ecoHotWater;
    }

    public Object getZone1Name() {
        return zone1Name;
    }

    public void setZone1Name(Object zone1Name) {
        this.zone1Name = zone1Name;
    }

    public Object getZone2Name() {
        return zone2Name;
    }

    public void setZone2Name(Object zone2Name) {
        this.zone2Name = zone2Name;
    }

    public Boolean getHolidayMode() {
        return holidayMode;
    }

    public void setHolidayMode(Boolean holidayMode) {
        this.holidayMode = holidayMode;
    }

    public Boolean getProhibitZone1() {
        return prohibitZone1;
    }

    public void setProhibitZone1(Boolean prohibitZone1) {
        this.prohibitZone1 = prohibitZone1;
    }

    public Boolean getProhibitZone2() {
        return prohibitZone2;
    }

    public void setProhibitZone2(Boolean prohibitZone2) {
        this.prohibitZone2 = prohibitZone2;
    }

    public Boolean getProhibitHotWater() {
        return prohibitHotWater;
    }

    public void setProhibitHotWater(Boolean prohibitHotWater) {
        this.prohibitHotWater = prohibitHotWater;
    }

    public Integer getTemperatureIncrementOverride() {
        return temperatureIncrementOverride;
    }

    public void setTemperatureIncrementOverride(Integer temperatureIncrementOverride) {
        this.temperatureIncrementOverride = temperatureIncrementOverride;
    }

    public Boolean getIdleZone1() {
        return idleZone1;
    }

    public void setIdleZone1(Boolean idleZone1) {
        this.idleZone1 = idleZone1;
    }

    public Boolean getIdleZone2() {
        return idleZone2;
    }

    public void setIdleZone2(Boolean idleZone2) {
        this.idleZone2 = idleZone2;
    }

    public Integer getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(Integer deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getLastCommunication() {
        return lastCommunication;
    }

    public void setLastCommunication(String lastCommunication) {
        this.lastCommunication = lastCommunication;
    }

    public String getNextCommunication() {
        return nextCommunication;
    }

    public void setNextCommunication(String nextCommunication) {
        this.nextCommunication = nextCommunication;
    }

    public Boolean getPower() {
        return power;
    }

    public void setPower(Boolean power) {
        this.power = power;
    }

    public Boolean getHasPendingCommand() {
        return hasPendingCommand;
    }

    public void setHasPendingCommand(Boolean hasPendingCommand) {
        this.hasPendingCommand = hasPendingCommand;
    }

    public Boolean getOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public Object getScene() {
        return scene;
    }

    public void setScene(Object scene) {
        this.scene = scene;
    }

    public Object getSceneOwner() {
        return sceneOwner;
    }

    public void setSceneOwner(Object sceneOwner) {
        this.sceneOwner = sceneOwner;
    }
}
