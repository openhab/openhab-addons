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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model;

import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.FanSpeed;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OperationMode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;

/**
 * This class contains the current state of the controllerw
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class MadokaSettings {

    private @Nullable OnOffType onOffState;

    private @Nullable QuantityType<Temperature> setpoint;

    private @Nullable QuantityType<Temperature> indoorTemperature;
    private @Nullable QuantityType<Temperature> outdoorTemperature;

    private @Nullable FanSpeed fanspeed;

    private @Nullable OperationMode operationMode;

    private @Nullable String homekitCurrentMode;
    private @Nullable String homekitTargetMode;

    private @Nullable String communicationControllerVersion;
    private @Nullable String remoteControllerVersion;

    private @Nullable PercentType eyeBrightness;
    private @Nullable QuantityType<Time> indoorPowerHours;
    private @Nullable QuantityType<Time> indoorOperationHours;
    private @Nullable QuantityType<Time> indoorFanHours;

    private @Nullable Boolean cleanFilterIndicator;

    public @Nullable Boolean getCleanFilterIndicator() {
        return cleanFilterIndicator;
    }

    public void setCleanFilterIndicator(Boolean cleanFilterIndicator) {
        this.cleanFilterIndicator = cleanFilterIndicator;
    }

    public @Nullable OnOffType getOnOffState() {
        return onOffState;
    }

    public void setOnOffState(OnOffType onOffState) {
        this.onOffState = onOffState;
    }

    public @Nullable QuantityType<Temperature> getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(QuantityType<Temperature> setpoint) {
        this.setpoint = setpoint;
    }

    public @Nullable QuantityType<Temperature> getIndoorTemperature() {
        return indoorTemperature;
    }

    public void setIndoorTemperature(QuantityType<Temperature> indoorTemperature) {
        this.indoorTemperature = indoorTemperature;
    }

    public @Nullable QuantityType<Temperature> getOutdoorTemperature() {
        return outdoorTemperature;
    }

    public void setOutdoorTemperature(QuantityType<Temperature> outdoorTemperature) {
        this.outdoorTemperature = outdoorTemperature;
    }

    public @Nullable FanSpeed getFanspeed() {
        return fanspeed;
    }

    public void setFanspeed(FanSpeed fanspeed) {
        this.fanspeed = fanspeed;
    }

    public @Nullable OperationMode getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(OperationMode operationMode) {
        this.operationMode = operationMode;
    }

    public @Nullable String getHomekitCurrentMode() {
        return homekitCurrentMode;
    }

    public void setHomekitCurrentMode(String homekitCurrentMode) {
        this.homekitCurrentMode = homekitCurrentMode;
    }

    public @Nullable String getHomekitTargetMode() {
        return homekitTargetMode;
    }

    public void setHomekitTargetMode(String homekitTargetMode) {
        this.homekitTargetMode = homekitTargetMode;
    }

    public @Nullable String getCommunicationControllerVersion() {
        return communicationControllerVersion;
    }

    public void setCommunicationControllerVersion(String communicationControllerVersion) {
        this.communicationControllerVersion = communicationControllerVersion;
    }

    public @Nullable String getRemoteControllerVersion() {
        return remoteControllerVersion;
    }

    public void setRemoteControllerVersion(String remoteControllerVersion) {
        this.remoteControllerVersion = remoteControllerVersion;
    }

    public @Nullable PercentType getEyeBrightness() {
        return eyeBrightness;
    }

    public void setEyeBrightness(PercentType eyeBrightness) {
        this.eyeBrightness = eyeBrightness;
    }

    public @Nullable QuantityType<Time> getIndoorPowerHours() {
        return indoorPowerHours;
    }

    public void setIndoorPowerHours(QuantityType<Time> indoorPowerHours) {
        this.indoorPowerHours = indoorPowerHours;
    }

    public @Nullable QuantityType<Time> getIndoorOperationHours() {
        return indoorOperationHours;
    }

    public void setIndoorOperationHours(QuantityType<Time> indoorOperationHours) {
        this.indoorOperationHours = indoorOperationHours;
    }

    public @Nullable QuantityType<Time> getIndoorFanHours() {
        return indoorFanHours;
    }

    public void setIndoorFanHours(QuantityType<Time> indoorFanHours) {
        this.indoorFanHours = indoorFanHours;
    }
}
