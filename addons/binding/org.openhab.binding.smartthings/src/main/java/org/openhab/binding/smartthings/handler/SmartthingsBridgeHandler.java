/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smartthings.config.SmartthingsBridgeConfig;
import org.openhab.binding.smartthings.internal.SmartthingsHttpClient;
//import org.eclipse.equinox.event.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartthingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
public class SmartthingsBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(SmartthingsBridgeHandler.class);

    private SmartthingsBridgeConfig config;

    private SmartthingsHttpClient httpClient;

    public SmartthingsBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things" which include Cm11aSwitchHandler and Cm11aLampHandler
    }

    @Override
    public void initialize() {
        // Get config data and validate
        config = getThing().getConfiguration().as(SmartthingsBridgeConfig.class);
        if (!validateConfig(this.config)) {
            return;
        }

        // Initialize the Smartthings http client
        try {
            httpClient = new SmartthingsHttpClient(config.smartthingsIp, config.smartthingsPort);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to start Smartthings Communications services because: " + e.getMessage());
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Smartthings Handler disposed.");

        httpClient.stopHttpClient();

        super.dispose();
    }

    private boolean validateConfig(SmartthingsBridgeConfig config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "smartthings configuration is missing");
            return false;
        }

        String ip = config.smartthingsIp;
        if (ip == null || ip.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings IP address is not specified");
            return false;
        }

        int port = config.smartthingsPort;
        if (port <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings Port is not specified");
            return false;
        }

        return true;
    }

    /**
     * Convert the Smartthings data into an OpenHAB State
     *
     * @param statusMap
     * @return
     */
    public SmartthingsHttpClient getSmartthingsHttpClient() {
        return httpClient;
    }

}
