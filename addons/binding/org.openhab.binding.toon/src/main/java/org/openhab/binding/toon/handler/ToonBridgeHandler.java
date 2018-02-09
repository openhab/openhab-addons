/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.toon.internal.ToonApiClient;
import org.openhab.binding.toon.internal.api.ToonState;
import org.openhab.binding.toon.internal.config.ToonBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ToonBridgeHandler} class connects bridges the Toon api and connected displays .
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(ToonBridgeHandler.class);
    private ToonBridgeConfiguration configuration;
    private ToonApiClient apiClient;

    protected ScheduledFuture<?> refreshJob;

    public ToonBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Toon API bridge handler.");

        configuration = getConfigAs(ToonBridgeConfiguration.class);
        logger.debug("refresh interval {}", configuration.refreshInterval);

        disposeApiClient();
        apiClient = new ToonApiClient(configuration);

        updateStatus();
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        disposeApiClient();
    }

    private void startAutomaticRefresh() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }

        refreshJob = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateChannels();
            }
        }, 50, configuration.refreshInterval, TimeUnit.MILLISECONDS);
    }

    public void requestRefresh() {
        if (configuration == null) {
            return;
        }
        startAutomaticRefresh();
    }

    private void updateChannels() {
        if (getThing().getThings().isEmpty()) {
            return;
        }
        logger.debug("updateChannels");
        try {
            ToonState state;
            try {
                state = apiClient.collect();
            } catch (Exception e) {
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return;
            }

            // prevent spamming the log file
            if (!ONLINE.equals(getThing().getStatus())) {
                updateStatus(ONLINE);
            }

            for (Thing handler : getThing().getThings()) {
                ThingHandler thingHandler = handler.getHandler();
                if (thingHandler instanceof AbstractToonHandler) {
                    AbstractToonHandler moduleHandler = (AbstractToonHandler) thingHandler;
                    moduleHandler.updateChannels(state);
                }
            }
        } catch (Exception e) {
            logger.debug("updateChannels acting up", e);
        }
    }

    private void disposeApiClient() {
        if (apiClient != null) {
            apiClient.logout();
        }
        apiClient = null;
    }

    private void updateStatus() {
        try {
            if (configuration == null) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "Configuration is missing or corrupted");
            } else if (StringUtils.isEmpty(configuration.username)) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "Username not configured");
            } else if (StringUtils.isEmpty(configuration.password)) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR, "Password not configured");
            } else {
                getApiClient().login();
                updateStatus(ONLINE);
            }
        } catch (Exception e) {
            updateStatus(OFFLINE, NONE, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            requestRefresh();
        } else {
            logger.warn("This Bridge can only handle the REFRESH command");
        }
    }

    public ToonApiClient getApiClient() {
        return apiClient;
    }
}
