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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.LIGHT_INTENSITY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SomfyTahomaDimmerLightHandler} is responsible for handling commands,
 * which are sent to one of the channels of the dimmer light thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaDimmerLightHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaDimmerLightHandler(Thing thing) {
        super(thing);
        stateNames.put(LIGHT_INTENSITY, "core:LightIntensityState");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else if (LIGHT_INTENSITY.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                sendCommand(command.toString().toLowerCase());
            } else {
                sendCommand("setIntensity", "[" + toInteger(command) + "]");
            }
        }
    }
}
