/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal.model;

import org.openhab.binding.alarm.internal.AlarmException;

/**
 * Enumeration of all alarm zone types.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum AlarmZoneType {
    DISABLED,
    ACTIVE,
    INTERN_ACTIVE,
    EXIT_ENTRY,
    IMMEDIATELY,
    SABOTAGE,
    ALWAYS,
    ALWAYS_IMMEDIATELY;

    /**
     * Parses a string with an alarm zone type.
     */
    public static AlarmZoneType parse(String strType) throws AlarmException {
        if (strType != null) {
            if (DISABLED.toString().equalsIgnoreCase(strType)) {
                return DISABLED;
            } else if (ACTIVE.toString().equalsIgnoreCase(strType)) {
                return ACTIVE;
            } else if (INTERN_ACTIVE.toString().equalsIgnoreCase(strType)) {
                return INTERN_ACTIVE;
            } else if (EXIT_ENTRY.toString().equalsIgnoreCase(strType)) {
                return EXIT_ENTRY;
            } else if (IMMEDIATELY.toString().equalsIgnoreCase(strType)) {
                return IMMEDIATELY;
            } else if (SABOTAGE.toString().equalsIgnoreCase(strType)) {
                return SABOTAGE;
            } else if (ALWAYS.toString().equalsIgnoreCase(strType)) {
                return ALWAYS;
            } else if (ALWAYS_IMMEDIATELY.toString().equalsIgnoreCase(strType)) {
                return ALWAYS_IMMEDIATELY;
            }
        }
        throw new AlarmException("AlarmZoneType " + strType + " not available");
    }
}
