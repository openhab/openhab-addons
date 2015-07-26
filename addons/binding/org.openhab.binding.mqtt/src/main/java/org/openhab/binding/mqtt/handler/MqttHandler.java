/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqtt.handler;

import static org.openhab.binding.mqtt.MqttBindingConstants.CHANNEL_1;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MqttHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcus - Initial contribution
 */
public class MqttHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    public MqttHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MQTT handler.");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_1)) {
            // TODO: handle command
        }
    }

    /**
     * Handles a command for a given channel.
     *
     * @param channelUID
     *            unique identifier of the channel on which the update was performed
     * @param newState
     *            new state
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        if (channelUID.getId().equals(CHANNEL_1)) {
            // TODO: handle command
        }

    }
}
