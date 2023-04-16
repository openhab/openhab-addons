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
package org.openhab.binding.sleepiq.internal.api.dto;

import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;
import org.openhab.binding.sleepiq.internal.api.enums.Side;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationPresetRequest} is used to set a preset for a bed side.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationPresetRequest {
    @SerializedName("side")
    private Side side;

    @SerializedName("preset")
    private FoundationPreset preset;

    @SerializedName("speed")
    private FoundationActuatorSpeed speed;

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public FoundationPresetRequest withSide(Side side) {
        setSide(side);
        return this;
    }

    public FoundationPreset getFoundationPreset() {
        return preset;
    }

    public void setFoundationPreset(FoundationPreset preset) {
        this.preset = preset;
    }

    public FoundationPresetRequest withFoundationPreset(FoundationPreset preset) {
        setFoundationPreset(preset);
        return this;
    }

    public FoundationActuatorSpeed getFoundationActuartorSpeed() {
        return speed;
    }

    public void setFoundationActuatorSpeed(FoundationActuatorSpeed speed) {
        this.speed = speed;
    }

    public FoundationPresetRequest withFoundationActuatorSpeed(FoundationActuatorSpeed speed) {
        setFoundationActuatorSpeed(speed);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepNumberRequest [side=");
        builder.append(side);
        builder.append(", preset=");
        builder.append(preset);
        builder.append(", speed=");
        builder.append(speed);
        builder.append("]");
        return builder.toString();
    }
}
