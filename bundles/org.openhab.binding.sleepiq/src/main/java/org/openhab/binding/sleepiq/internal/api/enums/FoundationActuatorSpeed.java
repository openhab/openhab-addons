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
package org.openhab.binding.sleepiq.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoundationActuatorSpeed} represents speed with which the actuator operates.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum FoundationActuatorSpeed {
    FAST(0),
    SLOW(1);

    private final int speed;

    FoundationActuatorSpeed(final int speed) {
        this.speed = speed;
    }

    public int value() {
        return speed;
    }

    public static FoundationActuatorSpeed forValue(int value) {
        for (FoundationActuatorSpeed s : FoundationActuatorSpeed.values()) {
            if (s.speed == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid speed: " + value);
    }

    @Override
    public String toString() {
        return String.valueOf(speed);
    }
}
