/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link NARoom} holds temperature data for a given room.
 *
 * @author Bernhard Kreuz - Initial contribution
 *
 */
@NonNullByDefault
public class NARoom extends NAThing implements NetatmoModule {
    private @Nullable OnOffType anticipating;
    private @Nullable OnOffType openWindow;
    private @Nullable ZonedDateTime thermSetpointStartTime;
    private @Nullable ZonedDateTime thermSetpointEndTime;
    private SetpointMode thermSetpointMode = SetpointMode.UNKNOWN;
    private int heatingPowerRequest;
    private double thermMeasuredTemperature;
    private double thermSetpointTemperature;

    public State isAnticipating() {
        OnOffType status = anticipating;
        return status != null ? status : UnDefType.NULL;
    }

    public State hasOpenedWindows() {
        OnOffType status = openWindow;
        return status != null ? status : UnDefType.NULL;
    }

    public int getHeatingPowerRequest() {
        return heatingPowerRequest;
    }

    public Double getMeasuredTemp() {
        return thermMeasuredTemperature;
    }

    public SetpointMode getSetpointMode() {
        return thermSetpointMode;
    }

    public double getSetpointTemp() {
        return thermSetpointTemperature;
    }

    public @Nullable ZonedDateTime getSetpointBegin() {
        return thermSetpointStartTime;
    }

    public @Nullable ZonedDateTime getSetpointEnd() {
        return thermSetpointEndTime;
    }

    @Override
    public ModuleType getType() {
        // In json api answer type for NARoom is used with words like kitchen, living...
        // TODO : consider if Maybe NARoom should not inherit from NAThing
        return ModuleType.NARoom;
    }
}
