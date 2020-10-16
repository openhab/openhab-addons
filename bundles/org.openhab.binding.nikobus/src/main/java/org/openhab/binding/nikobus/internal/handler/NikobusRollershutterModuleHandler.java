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
package org.openhab.binding.nikobus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link NikobusRollershutterModuleHandler} is responsible for communication between Nikobus
 * rollershutter-controller and binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusRollershutterModuleHandler extends NikobusModuleHandler {
    public NikobusRollershutterModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected int valueFromCommand(Command command) {
        if (command == UpDownType.DOWN || command == StopMoveType.MOVE) {
            return 0x02;
        }
        if (command == UpDownType.UP) {
            return 0x01;
        }
        if (command == StopMoveType.STOP) {
            return 0x00;
        }

        throw new IllegalArgumentException("Command '" + command + "' not supported");
    }

    @Override
    protected State stateFromValue(int value) {
        if (value == 0x00) {
            return OnOffType.OFF;
        }
        if (value == 0x01) {
            return UpDownType.UP;
        }
        if (value == 0x02) {
            return UpDownType.DOWN;
        }
        throw new IllegalArgumentException("Unexpected value " + value + " received");
    }
}
