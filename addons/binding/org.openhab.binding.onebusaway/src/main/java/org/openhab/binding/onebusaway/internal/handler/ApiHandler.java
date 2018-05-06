/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal.handler;

import static org.openhab.binding.onebusaway.OneBusAwayBindingConstants.THING_TYPE_API;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.onebusaway.internal.config.ApiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiHandler} is responsible for storing basic configuration data for talking to a OneBusAway API server.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class ApiHandler extends BaseBridgeHandler {
    public static final ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_API;

    private ApiConfiguration config;
    private Logger logger = LoggerFactory.getLogger(ApiHandler.class);

    public ApiHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("The API bridge is a read-only and can not handle commands.");
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OneBusAway bridge...");

        config = loadAndCheckConfiguration();
        if (config == null) {
            logger.debug("Initialization of OneBusAway API bridge failed!");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    protected String getApiKey() {
        return config.getApiKey();
    }

    protected String getApiServer() {
        return config.getApiServer();
    }

    private ApiConfiguration loadAndCheckConfiguration() {
        ApiConfiguration config = getConfigAs(ApiConfiguration.class);
        if (config.getApiKey() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "apiKey is not set");
            return null;
        }
        if (config.getApiServer() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "apiServer is not set");
            return null;
        }
        return config;
    }

}
