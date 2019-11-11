/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.upb.internal.message;

import java.util.Map.Entry;

/**
 * An enum of possible commands.
 *
 * @author cvanorman - Initial contribution
 */
public enum Command {
    ACTIVATE,
    DEACTIVATE,
    GOTO,
    START_FADE,
    STOP_FADE,
    BLINK,
    REPORT_STATE,
    STORE_STATE,
    DEVICE_STATE,
    NONE;

    /**
     * Gets the protocol byte code for this Command.
     *
     * @return
     */
    public byte toByte() {
        for (Entry<Integer, Command> e : UPBMessage.commandMap.entrySet()) {
            if (e.getValue() == this) {
                return e.getKey().byteValue();
            }
        }

        return 0;
    }

    /**
     * Converts a byte value into a Command.
     *
     * @param value
     *                  the byte value.
     * @return the Command that is represented by the given byte value.
     */
    public static Command valueOf(byte value) {
        return UPBMessage.commandMap.get(value);
    }
}
