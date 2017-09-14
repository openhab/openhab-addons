/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fronius.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for Fronius devices.
 *
 * @author Gerrit Beine - Initial contribution
 */
public class FroniusSymoBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FroniusSymoBridgeHandler.class);
    private final Set<FroniusBaseThingHandler> services = new HashSet<>();

    private ScheduledFuture<?> refreshJob;
    private String hostname;
    private Long refreshInterval;

    public FroniusSymoBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
    }

    @Override
    public void initialize() {
        logger.info("Initializing Fronius Bridge");

        if (getConfig().get("hostname") == null) {
            logger.error("Fronius Bridge requires a hostname");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        hostname = getConfig().get("hostname").toString();
        logger.debug("Fronius Bridge hostname is {}", hostname);

        refreshInterval = Long.parseLong(getConfig().get("refresh_interval").toString());
        logger.debug("Fronius Bridge refresh interval is {} seconds", refreshInterval);

        startAutomaticRefresh();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    public void registerService(final FroniusBaseThingHandler service) {
        this.services.add(service);
    }

    private void startAutomaticRefresh() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        };
        logger.debug("Initializing data refresh job for Fronius Bridge");

        refreshJob = scheduler.scheduleAtFixedRate(runnable, refreshInterval, refreshInterval, TimeUnit.SECONDS);
    }

    private void refresh() {
        logger.debug("Running data refresh job for Fronius Bridge");
        final HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            for (FroniusBaseThingHandler service : services) {
                service.refresh(httpClient);
            }
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Error during HTTP request: {}", e);
        }
        logger.debug("Finished data refresh job for Fronius Bridge");
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("Fronius Bridge child handler initialized: {}", childThing.getThingTypeUID());
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Fronius Bridge child handler disposed: {}", childThing.getThingTypeUID());
    }

}
