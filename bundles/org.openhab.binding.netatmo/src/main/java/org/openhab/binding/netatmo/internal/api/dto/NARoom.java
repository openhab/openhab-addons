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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;

/**
 *
 * @author Bernhard Kreuz - Initial contribution
 *
 */
@NonNullByDefault
public class NARoom extends NAModule {
    private boolean anticipating;
    private int heatingPowerRequest;
    private boolean openWindow;
    private double thermMeasuredTemperature;
    private @Nullable SetpointMode thermSetpointMode;
    private double thermSetpointTemperature;
    private long thermSetpointStartTime;
    private long thermSetpointEndTime = -1;

    /**
     * @return the anticipating
     */
    public boolean isAnticipating() {
        return anticipating;
    }

    /**
     * @param anticipating the anticipating to set
     */
    // TODO : dégager les setters ?
    public void setAnticipating(boolean anticipating) {
        this.anticipating = anticipating;
    }

    /**
     * @return the heatingPowerRequest
     */
    public int getHeatingPowerRequest() {
        return heatingPowerRequest;
    }

    /**
     * @param heatingPowerRequest the heatingPowerRequest to set
     */
    public void setHeatingPowerRequest(int heatingPowerRequest) {
        this.heatingPowerRequest = heatingPowerRequest;
    }

    /**
     * @return the openWindow
     */
    public boolean isOpenWindow() {
        return openWindow;
    }

    /**
     * @param openWindow the openWindow to set
     */
    public void setOpenWindow(boolean openWindow) {
        this.openWindow = openWindow;
    }

    /**
     * @return the thermMeasuredTemperature
     */
    public Double getThermMeasuredTemperature() {
        return thermMeasuredTemperature;
    }

    /**
     * @param thermMeasuredTemperature the thermMeasuredTemperature to set
     */
    public void setThermMeasuredTemperature(Double thermMeasuredTemperature) {
        this.thermMeasuredTemperature = thermMeasuredTemperature;
    }

    /**
     * @return the thermSetpointMode
     */
    public SetpointMode getThermSetpointMode() {
        SetpointMode mode = this.thermSetpointMode;
        return mode == null ? SetpointMode.UNKNOWN : mode;
    }

    /**
     * @param thermSetpointMode the thermSetpointMode to set
     */
    public void setThermSetpointMode(SetpointMode thermSetpointMode) {
        this.thermSetpointMode = thermSetpointMode;
    }

    /**
     * @return the thermSetpointTemperature
     */
    public double getThermSetpointTemperature() {
        return thermSetpointTemperature;
    }

    /**
     * @param thermSetpointTemperature the thermSetpointTemperature to set
     */
    public void setThermSetpointTemperature(Double thermSetpointTemperature) {
        this.thermSetpointTemperature = thermSetpointTemperature;
    }

    public long getThermSetpointStartTime() {
        return thermSetpointStartTime;
    }

    public void setThermSetpointStartTime(long thermSetpointStartTime) {
        this.thermSetpointStartTime = thermSetpointStartTime;
    }

    public long getThermSetpointEndTime() {
        return thermSetpointEndTime;
    }

    public void setThermSetpointEndTime(long thermSetpointEndTime) {
        this.thermSetpointEndTime = thermSetpointEndTime;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.NARoom;
    }
}
