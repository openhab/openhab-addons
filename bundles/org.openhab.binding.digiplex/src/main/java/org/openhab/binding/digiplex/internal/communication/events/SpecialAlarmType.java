/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.digiplex.internal.communication.events;

import java.util.Arrays;

/**
 * Type of special alarm.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public enum SpecialAlarmType {
    EMERGENCY_PANIC(0),
    MEDICAL_PANIC(1),
    FIRE_PANIC(2),
    RECENT_CLOSING(3),
    POLICE_CODE(4),
    GLOBAL_SHUTDOWN(5),
    UNKNOWN(-1);

    private int indicator;

    SpecialAlarmType(int indicator) {
        this.indicator = indicator;
    }

    public static SpecialAlarmType fromMessage(int indicator) {
        return Arrays.stream(values()).filter(type -> type.indicator == indicator).findFirst().orElse(UNKNOWN);
    }
}
