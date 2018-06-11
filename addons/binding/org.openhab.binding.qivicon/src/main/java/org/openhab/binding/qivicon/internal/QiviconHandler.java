/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import static org.openhab.binding.qivicon.internal.QiviconBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QiviconHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Claudius Ellsel - Initial contribution
 */
@NonNullByDefault
public class QiviconHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(QiviconHandler.class);

    // Reading the network address and authorization key from the properties map.
    private String networkAddress = getThing().getConfiguration().get(PARAMETER_NETWORK_ADDRESS).toString();
    private String authKey = getThing().getConfiguration().get(PARAMETER_AUTHORIZATION_KEY).toString();

    @Nullable
    private QiviconConfiguration config;

    public QiviconHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_TEMPERATURE)) {
            // TODO: handle command
            logger.debug("Handling Command {}", command.toString());
            apiHelper(networkAddress, authKey, channelUID, command);

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    public void apiHelper(String networkAddress, String authKey, ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        config = getConfigAs(QiviconConfiguration.class);

        logger.debug("Initializing, network address: {}", networkAddress);

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    public String getNetworkAddress() {
        return networkAddress;
    }

    public String getAuthKey() {
        return authKey;
    }
}
