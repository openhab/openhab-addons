/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.HEATING_LEVEL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * The {@link SomfyTahomaExteriorHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the exterior heating system thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaExteriorHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaExteriorHeatingSystemHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void updateThingChannels(SomfyTahomaState state) {
        if ("core:LevelState".equals(state.getName())) {
            Channel chLevel = thing.getChannel(HEATING_LEVEL);
            if (chLevel != null) {
                State newState = parseTahomaState(state);
                if (newState instanceof PercentType percentState) {
                    int value = percentState.intValue();
                    PercentType inverted = new PercentType(100 - value);
                    updateState(chLevel.getUID(), inverted);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else if (HEATING_LEVEL.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                sendCommand(command.toString().toLowerCase());
            } else {
                int inverted = 100 - toInteger(command);
                sendCommand("setLevel", "[" + inverted + "]");
            }
        }
    }
}
