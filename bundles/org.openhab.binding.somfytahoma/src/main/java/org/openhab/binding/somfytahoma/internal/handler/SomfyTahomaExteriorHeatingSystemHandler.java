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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.HEATING_LEVEL;
import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.SWITCH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;

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
                if (newState != null && newState instanceof PercentType) {
                    int value = ((PercentType) newState).intValue();
                    PercentType inverted = new PercentType(100 - value);
                    updateState(chLevel.getUID(), inverted);
                    Channel chSwitch = thing.getChannel(SWITCH);
                    if (chSwitch != null) {
                        updateState(chSwitch.getUID(), value == 100 ? OnOffType.OFF : OnOffType.ON);
                    }
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (command instanceof RefreshType) {
            return;
        } else if (SomfyTahomaBindingConstants.SWITCH.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendCommand(command.toString().toLowerCase());
        } else if (HEATING_LEVEL.equals(channelUID.getId())) {
            int inverted = 100 - toInteger(command);
            sendCommand("setLevel", "[" + inverted + "]");
        }
    }
}
