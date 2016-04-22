/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonytv.handler;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sonytv.SonyTVBindingConstants;
import org.openhab.binding.sonytv.config.BraviaConfiguration;
import org.openhab.binding.sonytv.internal.ScalarApiClient;
import org.openhab.binding.sonytv.internal.ScalarApiClient.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The {@link SonyTVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mikołaj Siedlarek - Initial contribution
 */
public class SonyTVHandler extends BaseThingHandler implements DiscoveryListener {

    private static final int DEFAULT_REFRESH_INTERVAL = 5;

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                updateState();
            } catch (final Exception exception) {
                logger.error("Exception during poll.", exception);
            }
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DiscoveryServiceRegistry discoveryServiceRegistry;

    private ScalarApiClient api;
    private ScheduledFuture<?> pollingJob;

    public SonyTVHandler(final Thing thing, final DiscoveryServiceRegistry discoveryServiceRegistry) {
        super(thing);
        this.discoveryServiceRegistry = Preconditions.checkNotNull(discoveryServiceRegistry);
    }

    @Override
    public void initialize() {
        final BraviaConfiguration configuration = getConfig().as(BraviaConfiguration.class);

        if (configuration.udn == null) {
            logger.warn("Invalid configuration: missing UDN.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration: missing UDN.");
            return;
        }
        if (configuration.apiUrl == null) {
            logger.warn("Invalid configuration: missing API URL.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration: missing API URL.");
            return;
        }
        if (configuration.preSharedKey == null) {
            logger.warn("Invalid configuration: missing pre-shared key.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid configuration: missing pre-shared key.");
            return;
        }

        discoveryServiceRegistry.addDiscoveryListener(this);
        connect();
        super.initialize();
    }

    @Override
    public void dispose() {
        discoveryServiceRegistry.removeDiscoveryListener(this);
        disconnect();
        super.dispose();
    }

    @Override
    public void thingDiscovered(final DiscoveryService source, final DiscoveryResult result) {
        if (result.getThingUID().equals(this.getThing().getUID())) {
            if (getThing().getConfiguration().get(BraviaConfiguration.UDN)
                    .equals(result.getProperties().get(BraviaConfiguration.UDN))) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    connect();
                }
            }
        }
    }

    @Override
    public void thingRemoved(final DiscoveryService source, final ThingUID thingUID) {
        if (thingUID.equals(this.getThing().getUID())) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.warn("Ignoring command - not connected.");
            return;
        }
        try {
            switch (channelUID.getId()) {
                case SonyTVBindingConstants.CHANNEL_POWER:
                    api.setPower((OnOffType) command);
                    break;
                case SonyTVBindingConstants.CHANNEL_INPUT:
                    api.setActiveInput((StringType) command);
                    break;
            }
        } catch (final Error error) {
            logger.error("Error while executing command.", error);
        }
    }

    private void updateState() throws Error {
        try {
            final OnOffType power = api.getPower();
            updateState(SonyTVBindingConstants.CHANNEL_POWER, power);
            updateStatus(ThingStatus.ONLINE);
            if (power.equals(OnOffType.ON)) {
                updateState(SonyTVBindingConstants.CHANNEL_INPUT, api.getActiveInput());
            }
        } catch (final ScalarApiClient.Error error) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.getMessage());
        }
    }

    private synchronized void connect() {
        final BraviaConfiguration config = getConfig().as(BraviaConfiguration.class);
        try {
            api = new ScalarApiClient(config.apiUrl, config.preSharedKey);
        } catch (final Error error) {
            logger.error("Communication error.", error);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication error.");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0,
                    (config.refresh == null ? DEFAULT_REFRESH_INTERVAL : config.refresh), TimeUnit.SECONDS);
        }
    }

    private synchronized void disconnect() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

}