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
package org.openhab.binding.freeathome.internal.valuestateconverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link ShuttercontrolValueStateConverter} is a value converter for shutter movement
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class ShuttercontrolValueStateConverter implements ValueStateConverter {

    @Override
    public State convertToState(String value) {
        State ret = UpDownType.DOWN;

        switch (value) {
            default:
            case "0":
                ret = new StringType("STOP");
                break;
            case "2":
                ret = UpDownType.UP;
                break;
            case "3":
                ret = UpDownType.DOWN;
                break;
        }

        return ret;
    }

    @Override
    public String convertToValueString(State state) {
        String valueString = "0";
        String stateString = "STOP";

        if (state instanceof UpDownType) {
            stateString = ((UpDownType) state).toString();
        }

        if (((Command) state) instanceof StopMoveType) {
            stateString = "STOP";
        }

        switch (stateString) {
            default:
            case "STOP":
                valueString = "0";
                break;
            case "UP":
                valueString = "0";
                break;
            case "DOWN":
                valueString = "1";
                break;
        }

        return valueString;
    }
}
