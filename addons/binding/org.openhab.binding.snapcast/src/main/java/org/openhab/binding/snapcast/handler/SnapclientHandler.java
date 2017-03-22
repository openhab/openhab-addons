/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.handler;

import static org.openhab.binding.snapcast.SnapcastBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.snapcast.SnapcastBindingConstants;
import org.openhab.binding.snapcast.internal.protocol.SnapcastClientController;
import org.openhab.binding.snapcast.internal.protocol.SnapcastController;
import org.openhab.binding.snapcast.internal.protocol.SnapcastUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SnapclientHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Steffen Folman Sørensen - Initial contribution
 */
public class SnapclientHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SnapclientHandler.class);
    private SnapcastController snapcastController;
    private String mac;
    private String host;
    private SnapcastClientController clientContoller;

    public SnapclientHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

        try {
            if (channelUID.getId().equals(CHANNEL_MUTE)) {
                if (OnOffType.class.isAssignableFrom(command.getClass())) {
                    OnOffType mute = (OnOffType) command;
                    clientContoller.mute(mute.equals(OnOffType.ON));
                }
            }
            if (channelUID.getId().equals(CHANNEL_VOLUME)) {
                if (command instanceof DecimalType) {
                    clientContoller.volume(((DecimalType) command).intValue());
                } else {
                    logger.error("Invalid type: {}", command);
                }
            }
            if (channelUID.getId().equals(CHANNEL_STREAM)) {
                if (StringType.class.isAssignableFrom(command.getClass())) {
                    StringType string = (StringType) command;
                    clientContoller.stream(string.toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        host = (String) thing.getConfiguration().get(SnapcastBindingConstants.CONFIG_HOST_NAME);
        mac = (String) thing.getConfiguration().get(SnapcastBindingConstants.CONFIG_MAC_ADDRESS);

        final UpdateHandler updateHandler = new UpdateHandler();
        clientContoller = ((SnapserverHandler) getBridge().getHandler()).getClient(mac);
        clientContoller.addUpdateListener(updateHandler);
        if (clientContoller.connected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
        updateHandler.updateClient(clientContoller);
    }

    private class UpdateHandler implements SnapcastUpdateListener {

        @Override
        public void updateClient(SnapcastClientController clientController) {
            updateState(SnapcastBindingConstants.CHANNEL_VOLUME, new DecimalType(clientController.volume()));
            updateState(SnapcastBindingConstants.CHANNEL_NAME, new StringType(clientController.name()));
            updateState(SnapcastBindingConstants.CHANNEL_STREAM, new StringType(clientController.stream()));
            if (clientController.isMuted()) {
                updateState(SnapcastBindingConstants.CHANNEL_MUTE, OnOffType.ON);
            } else {
                updateState(SnapcastBindingConstants.CHANNEL_MUTE, OnOffType.OFF);
            }

            if (clientContoller.connected()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

        }

    }

}
