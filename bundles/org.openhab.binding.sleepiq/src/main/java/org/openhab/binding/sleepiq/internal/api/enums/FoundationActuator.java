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
package org.openhab.binding.sleepiq.internal.api.enums;

import static org.openhab.binding.sleepiq.internal.SleepIQBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link FoundationActuator} represents actuators at the head and foot of the bed side.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum FoundationActuator {
    @SerializedName("H")
    HEAD("H"),

    @SerializedName("F")
    FOOT("F");

    private final String actuator;

    FoundationActuator(final String actuator) {
        this.actuator = actuator;
    }

    public String value() {
        return actuator;
    }

    public static FoundationActuator forValue(String value) {
        for (FoundationActuator s : FoundationActuator.values()) {
            if (s.actuator.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid actuator: " + value);
    }

    public static FoundationActuator convertFromChannelId(String channelId) {
        FoundationActuator localActuator;
        switch (channelId) {
            case CHANNEL_RIGHT_POSITION_HEAD:
            case CHANNEL_LEFT_POSITION_HEAD:
                localActuator = FoundationActuator.HEAD;
                break;
            case CHANNEL_RIGHT_POSITION_FOOT:
            case CHANNEL_LEFT_POSITION_FOOT:
                localActuator = FoundationActuator.FOOT;
                break;
            default:
                throw new IllegalArgumentException("Can't convert channel to actuator: " + channelId);
        }
        return localActuator;
    }

    @Override
    public String toString() {
        return actuator;
    }
}
