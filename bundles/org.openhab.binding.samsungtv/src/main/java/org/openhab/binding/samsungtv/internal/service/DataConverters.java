/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.service;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;

/**
 * The {@link DataConverters} provides utils for converting openHAB commands to
 * Samsung TV specific values.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class DataConverters {

    /**
     * Convert openHAB command to int.
     *
     * @param command
     * @param min
     * @param max
     * @param currentValue
     * @return
     */
    public static int convertCommandToIntValue(Command command, int min, int max, int currentValue) {
        if (command instanceof IncreaseDecreaseType || command instanceof DecimalType) {
            int value;
            if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
                value = Math.min(max, currentValue + 1);
            } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
                value = Math.max(min, currentValue - 1);
            } else if (command instanceof DecimalType) {
                value = ((DecimalType) command).intValue();
            } else {
                throw new NumberFormatException("Command '" + command + "' not supported");
            }

            return value;

        } else {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }
    }

    /**
     * Convert openHAB command to boolean.
     *
     * @param command
     * @return
     */
    public static boolean convertCommandToBooleanValue(Command command) {
        if (command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType) {
            boolean newValue;

            if (command.equals(OnOffType.ON) || command.equals(UpDownType.UP) || command.equals(OpenClosedType.OPEN)) {
                newValue = true;
            } else if (command.equals(OnOffType.OFF) || command.equals(UpDownType.DOWN)
                    || command.equals(OpenClosedType.CLOSED)) {
                newValue = false;
            } else {
                throw new NumberFormatException("Command '" + command + "' not supported");
            }

            return newValue;

        } else {
            throw new NumberFormatException("Command '" + command + "' not supported for channel");
        }
    }
}
