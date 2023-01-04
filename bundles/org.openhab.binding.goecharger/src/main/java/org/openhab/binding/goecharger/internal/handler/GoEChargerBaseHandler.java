/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.goecharger.internal.handler;

import static org.openhab.binding.goecharger.internal.GoEChargerBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.goecharger.internal.GoEChargerConfiguration;
import org.openhab.binding.goecharger.internal.api.GoEStatusResponseBaseDTO;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GoEChargerBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Samuel Brucksch - Initial contribution
 * @author Reinhard Plaim - Adapt to use API version 2
 */
@NonNullByDefault
public abstract class GoEChargerBaseHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GoEChargerBaseHandler.class);

    protected @Nullable GoEChargerConfiguration config;

    protected List<String> allChannels = new ArrayList<>();

    protected final Gson gson = new Gson();

    private @Nullable ScheduledFuture<?> refreshJob;

    protected final HttpClient httpClient;

    public GoEChargerBaseHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    protected State getValue(String channelId, GoEStatusResponseBaseDTO goeResponseBase) {
        switch (channelId) {
            case MAX_CURRENT:
                if (goeResponseBase.maxCurrent == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponseBase.maxCurrent, Units.AMPERE);
            case CABLE_ENCODING:
                if (goeResponseBase.cableEncoding == null) {
                    return UnDefType.UNDEF;
                }
                return new QuantityType<>(goeResponseBase.cableEncoding, Units.AMPERE);
            case FIRMWARE:
                if (goeResponseBase.firmware == null) {
                    return UnDefType.UNDEF;
                }
                return new StringType(goeResponseBase.firmware);
        }
        return UnDefType.UNDEF;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(GoEChargerConfiguration.class);
        allChannels = getThing().getChannels().stream().map(channel -> channel.getUID().getId())
                .collect(Collectors.toList());

        logger.debug("Number of channels found: {}", allChannels.size());

        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();
        logger.debug("Finished initializing!");
    }

    @Nullable
    protected GoEStatusResponseBaseDTO getGoEData()
            throws InterruptedException, TimeoutException, ExecutionException, JsonSyntaxException {
        return null;
    }

    protected void updateChannelsAndStatus(@Nullable GoEStatusResponseBaseDTO goeResponse, @Nullable String message) {
    }

    private void refresh() {
        synchronized (this) {
            // Request new GoE data
            try {
                GoEStatusResponseBaseDTO goeResponse = getGoEData();
                updateChannelsAndStatus(goeResponse, null);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                updateChannelsAndStatus(null, ie.getMessage());
            } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
                updateChannelsAndStatus(null, e.getMessage());
            }
        }
    }

    private void startAutomaticRefresh() {
        synchronized (this) {
            if (refreshJob == null || refreshJob.isCancelled()) {
                GoEChargerConfiguration config = getConfigAs(GoEChargerConfiguration.class);
                int delay = config.refreshInterval.intValue();
                logger.debug("Running refresh job with delay {} s", delay);
                refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, delay, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Go-eCharger handler.");

        final ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }
}
