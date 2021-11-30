/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeSceneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeSceneHandler extends FreeAtHomeSystemBaseHandler {

    private @Nullable String deviceID;
    private @Nullable String deviceChannel;
    private @Nullable String deviceOdp;

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeSceneHandler.class);

    public FreeAtHomeSceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Map<String, String> properties = getThing().getProperties();

        // Initialize the communication device channel properties
        deviceOdp = properties.get("deviceOdp");
        deviceID = properties.get("deviceId");
        deviceChannel = properties.get("channelId");

        if ((null == deviceOdp) && (null == deviceID) && (null == deviceChannel)) {
            // Device properties are found
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device properties are not found");
            return;
        } else {
            // The scene does not have online update in free@home
            logger.debug("Device - online: {}", deviceID);

            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        // Unregister device and specific channel for event based state updated

        logger.debug("Device removed {}", deviceID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (null != bridge) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler) {
                freeAtHomeBridge = (FreeAtHomeBridgeHandler) handler;
            }
        }

        if (null != freeAtHomeBridge) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.OFF);
        }

        if (command instanceof OnOffType) {
            OnOffType locCommand = (OnOffType) command;

            if (locCommand.equals(OnOffType.ON)) {
                freeAtHomeBridge.setDatapoint(deviceID, deviceChannel, deviceOdp, "1");
                updateState(channelUID, OnOffType.ON);
            }
        }

        // the scene can be triggered only therefore reset after 5 seconds
        scheduler.execute(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {

            }

            updateState(channelUID, OnOffType.OFF);
        });

        logger.debug("Handle command switch {} - at channel {} - full command {}", deviceID, channelUID.getAsString(),
                command.toFullString());
    }
}
