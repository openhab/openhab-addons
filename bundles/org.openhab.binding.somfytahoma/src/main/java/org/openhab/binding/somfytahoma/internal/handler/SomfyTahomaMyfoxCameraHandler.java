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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * The {@link SomfyTahomaMyfoxCameraHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Myfox camera.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaMyfoxCameraHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaMyfoxCameraHandler(Thing thing) {
        super(thing);
        stateNames.put(CLOUD_STATUS, CLOUD_DEVICE_STATUS_STATE);
        stateNames.put(SHUTTER, MYFOX_SHUTTER_STATUS_STATE);
    }

    @Override
    public void updateThingChannels(SomfyTahomaState state) {
        if (MYFOX_SHUTTER_STATUS_STATE.equals(state.getName())) {
            Channel ch = thing.getChannel(SHUTTER);
            if (ch != null) {
                // we need to covert opened/closed values to ON/OFF
                boolean open = "opened".equals(state.getValue());
                updateState(ch.getUID(), OnOffType.from(open));
            }
        } else if (CLOUD_DEVICE_STATUS_STATE.equals(state.getName())) {
            Channel ch = thing.getChannel(CLOUD_STATUS);
            if (ch != null) {
                State newState = parseTahomaState(ch.getAcceptedItemType(), state);
                if (newState != null) {
                    updateState(ch.getUID(), newState);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (!SHUTTER.equals(channelUID.getId())) {
            return;
        }

        if (command instanceof RefreshType) {
            return;
        } else {
            if (command instanceof OnOffType) {
                sendCommand(command.equals(OnOffType.ON) ? "open" : "close");
            }
        }
    }
}
