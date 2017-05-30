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
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.evohome.configuration.EvohomeGatewayConfiguration;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient;
import org.openhab.binding.evohome.internal.api.EvohomeApiClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvohomeGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jasper van Zuijlen - Initial contribution
 */

public class EvohomeGatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EvohomeGatewayHandler.class);
    private EvohomeGatewayConfiguration configuration = null;
    private EvohomeApiClient apiClient = null;

    protected ScheduledFuture<?> refreshTask;

    public EvohomeGatewayHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.info("Initializing Evohome Gateway handler.");

        configuration = getConfigAs(EvohomeGatewayConfiguration.class);
        logger.debug("refresh interval {}", configuration.refreshInterval);

        if (checkConfig()) {
            disposeApiClient();
            apiClient = new EvohomeApiClientV2(configuration);

            // Initialization can take a while, so kick if off on a separate thread
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    if (apiClient.login()) {
                        startRefreshTask();
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            }, 0, TimeUnit.SECONDS);

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
        if (getThing().getThings().isEmpty()) {
            return;
        }

        try {
            try {
                apiClient.update();
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return;
            }

            // prevent spamming the log file
            if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.ONLINE);
            }

            for (Thing handler : getThing().getThings()) {
                ThingHandler thingHandler = handler.getHandler();
                if (thingHandler instanceof BaseEvohomeHandler) {
                    BaseEvohomeHandler moduleHandler = (BaseEvohomeHandler) thingHandler;
                    moduleHandler.update(apiClient);
                }
            }
        } catch (Exception e) {
            logger.debug("updateChannels acting up", e);
        }
    }

    public EvohomeApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command == RefreshType.REFRESH) {
//            update();
        }
    }
}
