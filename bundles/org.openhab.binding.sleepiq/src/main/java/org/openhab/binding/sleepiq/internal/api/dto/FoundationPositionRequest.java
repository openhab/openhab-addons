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

import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuator;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.Side;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationPositionRequest} is used to set the position of the head or foot
 * of a side of a bed..
 *
 * @author Mark Hilbush - Initial contribution
 */
public class FoundationPositionRequest {
    @SerializedName("side")
    private Side side;

    @SerializedName("position")
    private int position;

    @SerializedName("actuator")
    private FoundationActuator actuator;

    @SerializedName("speed")
    private FoundationActuatorSpeed speed;

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public FoundationPositionRequest withSide(Side side) {
        setSide(side);
        return this;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public FoundationPositionRequest withPosition(int position) {
        setPosition(position);
        return this;
    }

    public FoundationActuator getFoundationActuartor() {
        return actuator;
    }

    public void setFoundationActuator(FoundationActuator actuator) {
        this.actuator = actuator;
    }

    public FoundationPositionRequest withFoundationActuator(FoundationActuator actuator) {
        setFoundationActuator(actuator);
        return this;
    }

    public FoundationActuatorSpeed getFoundationActuartorSpeed() {
        return speed;
    }

    public void setFoundationActuatorSpeed(FoundationActuatorSpeed speed) {
        this.speed = speed;
    }

    public FoundationPositionRequest withFoundationActuatorSpeed(FoundationActuatorSpeed speed) {
        setFoundationActuatorSpeed(speed);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepNumberRequest [side=");
        builder.append(side);
        builder.append(", position=");
        builder.append(position);
        builder.append(", actuator=");
        builder.append(actuator);
        builder.append(", speed=");
        builder.append(speed);
        builder.append("]");
        return builder.toString();
    }
}
