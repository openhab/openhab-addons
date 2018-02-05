/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication.events;

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
