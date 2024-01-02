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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Room} holds temperature data for a given room.
 *
 * @author Bernhard Kreuz - Initial contribution
 *
 */
@NonNullByDefault
public class Room extends NAObject implements NAModule {
    private @Nullable String type;
    private @Nullable OnOffType anticipating;
    private boolean openWindow;
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
        return openWindow ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public int getHeatingPowerRequest() {
        return heatingPowerRequest;
    }

    public double getMeasuredTemp() {
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
        // Note: In json api answer type for NARoom is used with words like kitchen, living...
        return ModuleType.ROOM;
    }

    public @Nullable String getLocation() {
        return type;
    }
}
