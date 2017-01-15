/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink.handler;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.config.WinkHub2Config;
import org.openhab.binding.wink.internal.discovery.WinkDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
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

    private ServiceRegistration<DiscoveryService> discoveryServiceRegistration;

    public WinkHub2Handler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Received a command!");
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(WinkHub2Config.class);
        if (validConfiguration()) {
            WinkDeviceDiscoveryService discovery = new WinkDeviceDiscoveryService(this);

            this.discoveryServiceRegistration = this.bundleContext.registerService(DiscoveryService.class, discovery,
                    null);

            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    // connect();
                }
            }, 0, TimeUnit.SECONDS);
        }
        updateStatus(ThingStatus.ONLINE);
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

    public WinkHub2Config getHubConfig() {
        return this.config;
    }

    private WinkHub2Config config;
}
