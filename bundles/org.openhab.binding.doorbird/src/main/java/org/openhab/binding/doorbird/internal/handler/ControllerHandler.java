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
package org.openhab.binding.doorbird.internal.handler;

import static org.openhab.binding.doorbird.internal.DoorbirdBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.api.DoorbirdAPI;
import org.openhab.binding.doorbird.internal.api.DoorbirdInfo;
import org.openhab.binding.doorbird.internal.config.ControllerConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ControllerHandler} is responsible for handling commands
 * to the A1081 Controller.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ControllerHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);

    private @Nullable String controllerId;

    private DoorbirdAPI api = new DoorbirdAPI();

    public ControllerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ControllerConfiguration config = getConfigAs(ControllerConfiguration.class);
        String host = config.doorbirdHost;
        if (host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Doorbird host not provided");
            return;
        }
        String user = config.userId;
        if (user == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User ID not provided");
            return;
        }
        String password = config.userPassword;
        if (password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User password not provided");
            return;
        }
        api.setAuthorization(host, user, password);

        // Get the Id of the controller for use in the open door API
        controllerId = getControllerId(config.controllerId);
        if (controllerId != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Doorbird not configured with a Controller");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Got command {} for channel {} of thing {}", command, channelUID, getThing().getUID());

        switch (channelUID.getId()) {
            case CHANNEL_OPENDOOR1:
                handleOpenDoor(command, "1");
                break;
            case CHANNEL_OPENDOOR2:
                handleOpenDoor(command, "2");
                break;
            case CHANNEL_OPENDOOR3:
                handleOpenDoor(command, "3");
                break;
        }
    }

    private void handleOpenDoor(Command command, String doorNumber) {
        String id = controllerId;
        if (id == null) {
            logger.debug("Unable to handle open door command because controller ID is not set");
            return;
        }
        if (command.equals(OnOffType.ON)) {
            api.openDoorController(id, doorNumber);
        }
    }

    private @Nullable String getControllerId(@Nullable String configId) {
        DoorbirdInfo info = api.getDoorbirdInfo();
        return info == null ? null : info.getControllerId(configId);
    }
}
