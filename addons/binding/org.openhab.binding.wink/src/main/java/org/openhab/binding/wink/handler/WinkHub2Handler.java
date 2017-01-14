/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.config.WinkHub2Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: The {@link WinkHub2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class WinkHub2Handler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(WinkHub2Handler.class);

    public WinkHub2Handler(Bridge bridge) {
        super(bridge);
        logger.info("Here's a new light bulb handler!");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Received a command!");
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(WinkHub2Config.class);
        if (validConfiguration()) {

        }
    }

    private boolean validConfiguration() {
        if (this.config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hub configuration missing");
            return false;
        } else if (StringUtils.isEmpty(this.config.access_token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "access_token not specified");
            return false;
        } else if (StringUtils.isEmpty(this.config.refresh_token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "refresh_token not specified");
            return false;
        }
        return true;
    }

    private WinkHub2Config config;
}
