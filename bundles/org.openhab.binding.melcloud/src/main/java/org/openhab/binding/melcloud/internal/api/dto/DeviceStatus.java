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
package org.openhab.binding.melcloud.internal.api.dto;

import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * The {@link DeviceProps} is responsible of JSON data For MELCloud API
 * Device Status data
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 * @author Pauli Anttila - Fine tuned expose annotations
 */
public class DeviceStatus {

    @Expose
    private Integer effectiveFlags;

    @Expose(serialize = false, deserialize = true)
    private Object localIPAddress;

    @Expose(serialize = false, deserialize = true)
    private Double roomTemperature;

    @Expose
    private Double setTemperature;

    @Expose
    private Integer setFanSpeed;

    @Expose
    private Integer operationMode;

    @Expose
    private Integer vaneHorizontal;

    @Expose
    private Integer vaneVertical;

    @Expose
    private Object name;

    @Expose(serialize = false, deserialize = true)
    private Integer numberOfFanSpeeds;

    @Expose(serialize = false, deserialize = true)
    private List<WeatherObservation> weatherObservations = null;

    @Expose(serialize = false, deserialize = true)
    private Object errorMessage;

    @Expose(serialize = false, deserialize = true)
    private Integer errorCode;

    @Expose(serialize = false, deserialize = true)
    private Double defaultHeatingSetTemperature;

    @Expose(serialize = false, deserialize = true)
    private Double defaultCoolingSetTemperature;

    @Expose(serialize = false, deserialize = true)
    private Boolean hideVaneControls;

    @Expose(serialize = false, deserialize = true)
    private Boolean hideDryModeControl;

    @Expose(serialize = false, deserialize = true)
    private Integer roomTemperatureLabel;

    @Expose(serialize = false, deserialize = true)
    private Boolean inStandbyMode;

    @Expose(serialize = false, deserialize = true)
    private Integer temperatureIncrementOverride;

    @Expose
    private Integer deviceID;

    @Expose(serialize = false, deserialize = true)
    private Integer deviceType;

    @Expose(serialize = false, deserialize = true)
    private String lastCommunication;

    @Expose(serialize = false, deserialize = true)
    private String nextCommunication;

    @Expose
    private Boolean power;

    @Expose
    private Boolean hasPendingCommand;

    @Expose(serialize = false, deserialize = true)
    private Boolean offline;

    @Expose(serialize = false, deserialize = true)
    private Object scene;

    @Expose(serialize = false, deserialize = true)
    private Object sceneOwner;

    public Integer getEffectiveFlags() {
        return effectiveFlags;
    }

    public void setEffectiveFlags(Integer effectiveFlags) {
        this.effectiveFlags = effectiveFlags;
    }

    public Object getLocalIPAddress() {
        return localIPAddress;
    }

    public void setLocalIPAddress(Object localIPAddress) {
        this.localIPAddress = localIPAddress;
    }

    public Double getRoomTemperature() {
        return roomTemperature;
    }

    public void setRoomTemperature(Double roomTemperature) {
        this.roomTemperature = roomTemperature;
    }

    public Double getSetTemperature() {
        return setTemperature;
    }

    public void setSetTemperature(Double setTemperature) {
        this.setTemperature = setTemperature;
    }

    public Integer getSetFanSpeed() {
        return setFanSpeed;
    }

    public void setSetFanSpeed(Integer setFanSpeed) {
        this.setFanSpeed = setFanSpeed;
    }

    public Integer getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(Integer operationMode) {
        this.operationMode = operationMode;
    }

    public Integer getVaneHorizontal() {
        return vaneHorizontal;
    }

    public void setVaneHorizontal(Integer vaneHorizontal) {
        this.vaneHorizontal = vaneHorizontal;
    }

    public Integer getVaneVertical() {
        return vaneVertical;
    }

    public void setVaneVertical(Integer vaneVertical) {
        this.vaneVertical = vaneVertical;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public Integer getNumberOfFanSpeeds() {
        return numberOfFanSpeeds;
    }

    public void setNumberOfFanSpeeds(Integer numberOfFanSpeeds) {
        this.numberOfFanSpeeds = numberOfFanSpeeds;
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

    public Double getDefaultHeatingSetTemperature() {
        return defaultHeatingSetTemperature;
    }

    public void setDefaultHeatingSetTemperature(Double defaultHeatingSetTemperature) {
        this.defaultHeatingSetTemperature = defaultHeatingSetTemperature;
    }

    public Double getDefaultCoolingSetTemperature() {
        return defaultCoolingSetTemperature;
    }

    public void setDefaultCoolingSetTemperature(Double defaultCoolingSetTemperature) {
        this.defaultCoolingSetTemperature = defaultCoolingSetTemperature;
    }

    public Boolean getHideVaneControls() {
        return hideVaneControls;
    }

    public void setHideVaneControls(Boolean hideVaneControls) {
        this.hideVaneControls = hideVaneControls;
    }

    public Boolean getHideDryModeControl() {
        return hideDryModeControl;
    }

    public void setHideDryModeControl(Boolean hideDryModeControl) {
        this.hideDryModeControl = hideDryModeControl;
    }

    public Integer getRoomTemperatureLabel() {
        return roomTemperatureLabel;
    }

    public void setRoomTemperatureLabel(Integer roomTemperatureLabel) {
        this.roomTemperatureLabel = roomTemperatureLabel;
    }

    public Boolean getInStandbyMode() {
        return inStandbyMode;
    }

    public void setInStandbyMode(Boolean inStandbyMode) {
        this.inStandbyMode = inStandbyMode;
    }

    public Integer getTemperatureIncrementOverride() {
        return temperatureIncrementOverride;
    }

    public void setTemperatureIncrementOverride(Integer temperatureIncrementOverride) {
        this.temperatureIncrementOverride = temperatureIncrementOverride;
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
