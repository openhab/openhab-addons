/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wso2iots.handler;

import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wso2iots.internal.config.BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeHandler} is responsible for handling commands, which are
 * sent to the channels.
 *
 * @author Ramesha Karunasena - Initial contribution
 */
public class BridgeHandler extends BaseBridgeHandler {

    private ScheduledFuture<?> refreshJob;

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);

    public BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {

        logger.debug("Initializing bridge handler.");

        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        logger.debug("config hostname = {}", config.hostname);
        logger.debug("config port = {}", config.port);

        boolean validConfig = true;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.hostname) == null) {
            errorMsg = "Parameter 'hostname' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.port == null) {
            errorMsg = "Parameter 'port' must be configured";
            validConfig = false;
        }
        if (config.refresh == null) {
            errorMsg = "Parameter 'refresh' must be configured";
            validConfig = false;
        }

        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the bridge handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels - nothing to do
    }

    protected BridgeConfiguration getBridgeConfiguration() {

        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        return config;

    }

}
