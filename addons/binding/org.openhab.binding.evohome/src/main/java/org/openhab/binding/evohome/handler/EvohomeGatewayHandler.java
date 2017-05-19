/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvohomeGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jasper van Zuijlen - Initial contribution
 */
public class EvohomeGatewayHandler extends BaseBridgeHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvohomeGatewayHandler.class);
    private EvohomeGatewayConfiguration configuration = null;
    private EvohomeApiClient apiClient = null;

    protected ScheduledFuture<?> refreshTask;

    public EvohomeGatewayHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Evohome Gateway handler.");

        configuration = getConfigAs(EvohomeGatewayConfiguration.class);
        logger.debug("refresh interval {}", configuration.refreshInterval);

        if (checkConfig()) {
            disposeApiClient();
            apiClient = new EvohomeApiClient(configuration);
            apiClient.login();
            startRefreshTask();

            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        disposeRefreshTask();
        disposeApiClient();
    }

    private void disposeApiClient() {
        if (apiClient != null) {
            apiClient.logout();
        }
        apiClient = null;
    }

    private void disposeRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
    }

    private boolean checkConfig() {
        try {
            if (configuration == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configuration is missing or corrupted");
            } else if (StringUtils.isEmpty(configuration.username)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Username not configured");
            } else if (StringUtils.isEmpty(configuration.password)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Password not configured");
            } else if (StringUtils.isEmpty(configuration.applicationId)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Application Id not configured");
            } else {
                return true;
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }

        return false;
    }

    private void startRefreshTask() {
        disposeRefreshTask();

        refreshTask = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 50, configuration.refreshInterval, TimeUnit.MILLISECONDS);
    }

    private void update() {
        apiClient.getData();
        // TODO trigger update requests on the client here and delegate them to the Things
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        //
        // if (channelUID.getId().equals(CHANNEL_1)) {
        // int i = 5;
        //
        // i++;
        // // TODO: handle command
        //
        // // Note: if communication with thing fails for some reason,
        // // indicate that by setting the status with detail information
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // // "Could not control device at IP address x.x.x.x");
        // }
    }
}