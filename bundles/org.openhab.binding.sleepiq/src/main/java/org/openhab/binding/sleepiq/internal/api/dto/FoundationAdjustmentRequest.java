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
package org.openhab.binding.sleepiq.internal.api.dto;

import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuator;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.Side;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationAdjustmentRequest} is used control the actuator
 * on the side of a bed.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationAdjustmentRequest {
    @SerializedName("side")
    private Side side;

    @SerializedName("actuator")
    private FoundationActuator actuator;

    @SerializedName("position")
    private int position;

    @SerializedName("speed")
    private FoundationActuatorSpeed speed;

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public FoundationAdjustmentRequest withSide(Side side) {
        setSide(side);
        return this;
    }

    public FoundationActuator getFoundationActuator() {
        return actuator;
    }

    public void setFoundationActuator(FoundationActuator actuator) {
        this.actuator = actuator;
    }

    public FoundationAdjustmentRequest withFoundationActuator(FoundationActuator actuator) {
        setFoundationActuator(actuator);
        return this;
    }

    public int getFoundationPosition() {
        return position;
    }

    public void setFoundationPosition(int position) {
        this.position = position;
    }

    public FoundationAdjustmentRequest withFoundationPosition(int position) {
        setFoundationPosition(position);
        return this;
    }

    public FoundationActuatorSpeed getFoundationActuartorSpeed() {
        return speed;
    }

    public void setFoundationActuatorSpeed(FoundationActuatorSpeed speed) {
        this.speed = speed;
    }

    public FoundationAdjustmentRequest withFoundationActuatorSpeed(FoundationActuatorSpeed speed) {
        setFoundationActuatorSpeed(speed);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepNumberRequest [side=");
        builder.append(side);
        builder.append(", actuator=");
        builder.append(actuator);
        builder.append(", position=");
        builder.append(position);
        builder.append(", speed=");
        builder.append(speed);
        builder.append("]");
        return builder.toString();
    }
}
