/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.handler;

import static org.openhab.binding.roomba.RoombaBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.roomba.config.RoombaConfiguration;
import org.openhab.binding.roomba.model.exception.RoombaCommunicationException;
import org.openhab.binding.roomba.service.RoombaService;
import org.openhab.binding.roomba.service.impl.Roomba900Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RoombaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stephen Liang - Initial contribution
 */
public class RoombaHandler extends BaseThingHandler {
    private RoombaService roombaService;

    private Logger logger = LoggerFactory.getLogger(RoombaHandler.class);

    public RoombaHandler(Thing thing) {
        super(thing);

        RoombaConfiguration config = getConfigAs(RoombaConfiguration.class);

        // Keeping this flexible, iRobot may decide to change how the APIs are done for future Roomba products.
        try {
            switch (thing.getThingTypeUID().getId()) {
                case ROOMBA_980_SKU:
                    this.roombaService = new Roomba900Service(config.ipAddress, config.password);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Cannot support this thing yet: " + thing.getThingTypeUID().getId());
            }

        } catch (Exception e) {
            logger.error("Failed to start roomba 980 service", e);
        }
    }

    /**
     * Handles the command for a given channel
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("received command for channel uid: {}, command: {}", channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_START:
                handleStart(command);
                break;
            case CHANNEL_PAUSE:
                handlePause(command);
                break;
            case CHANNEL_STOP:
                handleStop(command);
                break;
            case CHANNEL_RESUME:
                handleResume(command);
                break;
            case CHANNEL_DOCK:
                handleDock(command);
                break;
            default:
                logger.debug("Received command {} for unknown channel: {}", command, channelUID);
                break;
        }
    }

    /**
     * Initializes this handler and ensures that the Roomba is available for commands.
     */
    @Override
    public void initialize() {
        try {
            if (roombaService.isOk()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Failed to get the Roomba's current mission.");
            }
        } catch (RoombaCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Handles the start channel. This channel starts the Roomba's cleaning process
     *
     * @param command The command being handled
     */
    private void handleStart(Command command) {
        try {
            if (command instanceof OnOffType) {
                roombaService.start();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RoombaCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Handles the start channel. This channel pauses a Roomba's cleaning process
     *
     * @param command The command being handled
     */
    private void handlePause(Command command) {
        try {
            if (command instanceof OnOffType) {
                roombaService.pause();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RoombaCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Handles the start channel. This channel resumes a paused Roomba cleaning process
     *
     * @param command The command being handled
     */
    private void handleResume(Command command) {
        try {
            if (command instanceof OnOffType) {
                roombaService.resume();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RoombaCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Handles the start channel. This channel stops the Roomba's cleaning process
     *
     * @param command The command being handled
     */
    private void handleStop(Command command) {
        try {
            if (command instanceof OnOffType) {
                roombaService.stop();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RoombaCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Handles the start channel. This channel docks the Roomba to its charging dock
     *
     * @param command The command being handled
     */
    private void handleDock(Command command) {
        try {
            if (command instanceof OnOffType) {
                roombaService.dock();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RoombaCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
