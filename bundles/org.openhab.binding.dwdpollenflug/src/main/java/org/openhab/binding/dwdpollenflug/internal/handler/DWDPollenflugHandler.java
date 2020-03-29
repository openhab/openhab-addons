/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dwdpollenflug.internal.handler;

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.dwdpollenflug.internal.DWDDataAccess;
import org.openhab.binding.dwdpollenflug.internal.config.DWDPollenflugConfiguration;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollen;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DWDPollenflugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugHandler.class);

    private boolean initializing;
    private @Nullable DWDPollenflugConfiguration config;

    private boolean ignoreConfigurationUpdate;

    private @Nullable ScheduledFuture<?> pollingJob;
    private boolean polling;

    private final DWDDataAccess dataAccess;

    public DWDPollenflugHandler(final Thing thing, final HttpClient client) {
        super(thing);
        this.dataAccess = new DWDDataAccess(client);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        initializing = true;
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(DWDPollenflugConfiguration.class);

        if (config.isValid()) {
            stopPolling();
            startPolling();
        } else {
            logger.warn("Configuration error");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

        initializing = false;
        logger.debug("Finished initializing!");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!ignoreConfigurationUpdate) {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    private void startPolling() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at interval {}min", config.getRefresh());
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY,
                    config.getRefresh() * SECONDS_PER_MINUTE, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
    }

    private void stopPolling() {
        if (!(pollingJob == null || pollingJob.isCancelled())) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (!(command instanceof RefreshType)) {
            return;
        }
    }

    private void poll() {
        if (initializing) {
            logger.trace("Still initializing. Ignoring refresh request.");
            return;
        }

        if (polling) {
            logger.trace("Already polling. Ignoring refresh request.");
            return;
        }

        logger.debug("Polling");
        polling = true;

        dataAccess.refresh(config.getRegionId()).handle((region, e) -> {
            if (region == null) {
                if (e == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            } else {
                updateStatus(ThingStatus.ONLINE);
                updateRegion(region);
            }

            polling = false;
            return null;
        });
    }

    private void updateRegion(final DWDRegion region) {
        updateProperties(region);
        updateChannels(region);
    }

    private void updateProperties(final DWDRegion region) {
        ignoreConfigurationUpdate = true;
        updateProperties(region.getProperties());
        ignoreConfigurationUpdate = false;
    }

    private void updateChannels(final DWDRegion region) {
        for (final Entry<String, DWDPollen> entry : region.getPollen().entrySet()) {
            updatePollen(entry.getKey(), entry.getValue());
        }
    }

    private void updatePollen(final String pollenType, final DWDPollen pollenState) {
        ChannelUID channelUID = createChannelUID(pollenType, CHANNEL_TODAY);
        logger.debug("Update {} to {}", channelUID, pollenState.getToday());
        updateState(channelUID, new StringType(pollenState.getToday()));

        channelUID = createChannelUID(pollenType, CHANNEL_TOMORROW);
        logger.debug("Update {} to {}", channelUID, pollenState.getTomorrow());
        updateState(channelUID, new StringType(pollenState.getTomorrow()));

        channelUID = createChannelUID(pollenType, CHANNEL_DAYAFTER_TO);
        logger.debug("Update {} to {}", channelUID, pollenState.getDayAfterTomorrow());
        updateState(channelUID, new StringType(pollenState.getDayAfterTomorrow()));
    }

    private ChannelUID createChannelUID(final String pollenType, final String subchannel) {
        final String mappedType = CHANNELS_POLLEN_MAP.get(pollenType);
        return thing.getChannel(mappedType + "#" + subchannel).getUID();
    }
}
