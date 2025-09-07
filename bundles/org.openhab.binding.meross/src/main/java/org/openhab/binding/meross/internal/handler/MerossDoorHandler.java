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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossDoorConfiguration;
import org.openhab.binding.meross.internal.exception.MerossMqttConnackException;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    public MerossDoorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MerossDoorConfiguration.class);
        initializeDevice();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        MerossBridgeHandler merossBridgeHandler = this.merossBridgeHandler;
        if (merossBridgeHandler == null) {
            return;
        }
        var merossHttpConnector = merossBridgeHandler.getMerossHttpConnector();
        MerossManager merossManager = null;
        if (merossHttpConnector != null) {
            merossManager = MerossManager.newMerossManager(merossHttpConnector);
        }
        if (channelUID.getId().startsWith(CHANNEL_DOOR_STATE)) {
            String channelId = channelUID.getId().substring(CHANNEL_DOOR_STATE.length());
            int channel = 0;
            try {
                channel = Integer.valueOf(channelId);
            } catch (NumberFormatException e) {
                // Ignore and default to channel 0, this is because only a single channel available
            }
            if (command instanceof UpDownType) {
                try {
                    if (UpDownType.UP.equals(command)) {
                        if (merossManager != null) {
                            merossManager.sendCommand(config.name, channel,
                                    MerossEnum.Namespace.GARAGE_DOOR_STATE.name(), UpDownType.UP.name());
                        }
                    } else if (UpDownType.DOWN.equals(command)) {
                        if (merossManager != null) {
                            merossManager.sendCommand(config.name, channel,
                                    MerossEnum.Namespace.GARAGE_DOOR_STATE.name(), UpDownType.DOWN.name());
                        }
                    }
                } catch (IOException | MerossMqttConnackException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Cannot send command" + e.getMessage());
                }
            } else if (command instanceof RefreshType) {
                logger.debug("Refresh command not supported");
            } else {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
            }
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }
}
