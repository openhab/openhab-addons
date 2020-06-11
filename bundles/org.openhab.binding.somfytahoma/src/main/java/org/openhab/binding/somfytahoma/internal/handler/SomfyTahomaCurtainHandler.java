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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;

/**
 * The {@link SomfyTahomaCurtainHandler} is responsible for handling commands,
 * which are sent to one of the channels of the curtain thing.
 *
 * @author Tobias Ammann - Initial contribution
 * @author Ondrej Pecta - Code optimization
 */
@NonNullByDefault
public class SomfyTahomaCurtainHandler extends SomfyTahomaRollerShutterHandler {

    public SomfyTahomaCurtainHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected String getTahomaCommand(String command) {
        switch (command) {
            case "OFF":
            case "DOWN":
            case "CLOSE":
                return COMMAND_CLOSE;
            case "ON":
            case "UP":
            case "OPEN":
                return COMMAND_OPEN;
            case "MY":
            case "STOP":
                return COMMAND_MY;
            default:
                return COMMAND_SET_CLOSURE;
        }
    }
}
