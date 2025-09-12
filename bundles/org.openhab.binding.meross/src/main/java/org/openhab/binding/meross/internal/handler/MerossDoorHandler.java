/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.handler;

import static org.openhab.binding.meross.internal.MerossBindingConstants.CHANNEL_DOOR_STATE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossDoorConfiguration;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossDoorHandler} class is responsible for handling communication with garage doors
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class MerossDoorHandler extends MerossDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(MerossDoorHandler.class);

    public MerossDoorHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MerossDoorConfiguration.class);
        initializeDevice();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        if (channelUID.getId().startsWith(CHANNEL_DOOR_STATE)) {
            String channelId = channelUID.getId().substring(CHANNEL_DOOR_STATE.lastIndexOf("_") + 1);
            int channel = 0;
            try {
                channel = Integer.valueOf(channelId);
            } catch (NumberFormatException e) {
                // Ignore and default to channel 0, this is because only a single channel is available
            }
            MerossManager manager = this.manager;
            if (manager == null) {
                logger.debug("Handling command, manager not available");
                return;
            }

            try {
                switch (command) {
                    case UpDownType upDownType:
                        if (UpDownType.UP.equals(upDownType)) {
                            manager.sendCommand(channel, Namespace.GARAGE_DOOR_STATE, UpDownType.UP.name());
                        } else if (UpDownType.DOWN.equals(upDownType)) {
                            manager.sendCommand(channel, Namespace.GARAGE_DOOR_STATE, UpDownType.DOWN.name());
                        }
                        break;
                    case RefreshType refreshType:
                        if (ipAddress == null) {
                            logger.debug("Not connected locally, refresh not supported");
                        } else {
                            manager.refresh(Namespace.GARAGE_DOOR_STATE);
                        }
                        break;
                    default:
                        logger.debug("Unsupported command {} for channel {}", command, channelUID);
                        break;
                }
            } catch (MqttException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Cannot send command, " + e.getMessage());
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection interrupted");
            }
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }

    @Override
    public void updateState(Namespace namespace, int deviceChannel, State state) {
        if (namespace != Namespace.GARAGE_DOOR_STATE) {
            return;
        }
        String channelId = CHANNEL_DOOR_STATE + "_" + Integer.toString(deviceChannel);
        if (thing.getChannel(channelId) == null && deviceChannel == 0) {
            channelId = CHANNEL_DOOR_STATE;
        }
        updateState(channelId, state);
    }
}
