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

import java.time.ZonedDateTime;

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
public class NARoom extends NAThing {
    private boolean anticipating;
    private int heatingPowerRequest;
    private boolean openWindow;
    private double thermMeasuredTemperature;
    private SetpointMode thermSetpointMode = SetpointMode.UNKNOWN;
    private double thermSetpointTemperature;
    private @Nullable ZonedDateTime thermSetpointStartTime;
    private @Nullable ZonedDateTime thermSetpointEndTime;

    /**
     * @return the anticipating
     */
    public boolean isAnticipating() {
        return anticipating;
    }

    /**
     * @return the heatingPowerRequest
     */
    public int getHeatingPowerRequest() {
        return heatingPowerRequest;
    }

    /**
     * @return the openWindow
     */
    public boolean isOpenWindow() {
        return openWindow;
    }

    /**
     * @return the thermMeasuredTemperature
     */
    public Double getThermMeasuredTemperature() {
        return thermMeasuredTemperature;
    }

    /**
     * @return the thermSetpointMode
     */
    public SetpointMode getThermSetpointMode() {
        return thermSetpointMode;
    }

    /**
     * @return the thermSetpointTemperature
     */
    public double getThermSetpointTemperature() {
        return thermSetpointTemperature;
    }

    public @Nullable ZonedDateTime getThermSetpointStartTime() {
        return thermSetpointStartTime;
    }

    public @Nullable ZonedDateTime getThermSetpointEndTime() {
        return thermSetpointEndTime;
    }

    @Override
    public ModuleType getType() {
        // In json api answer type for NARoom is used with words like kitchen, living...
        // Maybe NARoom should not inherit from NAThing
        return ModuleType.NARoom;
    }
}
