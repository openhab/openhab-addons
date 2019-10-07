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
package org.openhab.binding.nikobus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link NikobusSwitchModuleHandler} is responsible for communication between Nikobus switch module and binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusSwitchModuleHandler extends NikobusModuleHandler {
    public NikobusSwitchModuleHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected int valueFromCommand(Command command) {
        if (command == OnOffType.ON) {
            return 0xff;
        }

        if (command == OnOffType.OFF) {
            return 0x00;
        }

        throw new IllegalArgumentException("Command '" + command + "' not supported");
    }

    @Override
    protected State stateFromValue(int value) {
        return value != 0 ? OnOffType.ON : OnOffType.OFF;
    }
}
