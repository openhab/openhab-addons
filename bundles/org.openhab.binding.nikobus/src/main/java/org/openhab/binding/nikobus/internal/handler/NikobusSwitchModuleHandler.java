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
package org.openhab.binding.nikobus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

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
    protected int valueFromCommand(String channelId, Command command) {
        if (command == OnOffType.ON) {
            return 0xff;
        }

        if (command == OnOffType.OFF) {
            return 0x00;
        }

        throw new IllegalArgumentException("Command '" + command + "' not supported");
    }

    @Override
    protected State stateFromValue(String channelId, int value) {
        return OnOffType.from(value != 0);
    }
}
