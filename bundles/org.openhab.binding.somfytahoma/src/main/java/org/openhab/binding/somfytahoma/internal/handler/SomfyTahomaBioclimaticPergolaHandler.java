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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaBioclimaticPergolaHandler} is responsible for handling commands,
 * which are sent to one of the channels of the bioclimatic pergola thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaBioclimaticPergolaHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaBioclimaticPergolaHandler(Thing thing) {
        super(thing);
        stateNames.put(SLATS, "core:SlatsOpenClosedState");
        stateNames.put(ORIENTATION, "core:SlatsOrientationState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        switch (channelUID.getId()) {
            case PERGOLA_COMMAND:
                String cmd = getTahomaCommand(command.toString().toUpperCase());
                sendCommand(cmd);
                break;
            case ORIENTATION:
                // Bioclimatic pergola can control only orientation and full closure, not partial closure
                String param = "[" + toInteger(command) + "]";
                sendCommand(COMMAND_SET_ORIENTATION, param);
                break;
            default:
                return;
        }
    }

    private String getTahomaCommand(String command) {
        switch (command) {
            case "OFF":
            case "DOWN":
            case "CLOSE":
            case "CLOSESLATS":
                return COMMAND_CLOSE_SLATS;
            case "ON":
            case "UP":
            case "OPEN":
            case "OPENSLATS":
                return COMMAND_OPEN_SLATS;
            default:
                return COMMAND_STOP;
        }
    }
}
