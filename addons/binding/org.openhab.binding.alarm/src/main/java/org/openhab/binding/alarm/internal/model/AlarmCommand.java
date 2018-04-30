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
 * Enumeration of all alarm controller commands.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum AlarmCommand {
    ARM_INTERNALLY,
    ARM_EXTERNALLY,
    PASSTHROUGH,
    FORCE_ALARM,
    DISARM;

    /**
     * Parses a string with a command.
     */
    public static AlarmCommand parse(String strCommand) throws AlarmException {
        if (strCommand != null) {
            if (ARM_INTERNALLY.toString().equalsIgnoreCase(strCommand)) {
                return ARM_INTERNALLY;
            } else if (ARM_EXTERNALLY.toString().equalsIgnoreCase(strCommand)) {
                return ARM_EXTERNALLY;
            } else if (PASSTHROUGH.toString().equalsIgnoreCase(strCommand)) {
                return PASSTHROUGH;
            } else if (FORCE_ALARM.toString().equalsIgnoreCase(strCommand)) {
                return FORCE_ALARM;
            } else if (DISARM.toString().equalsIgnoreCase(strCommand)) {
                return DISARM;
            }
        }
        throw new AlarmException("Command " + strCommand + " not available");
    }
}
