/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tasmotaplug.internal.handler;

import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.tasmotaplug.internal.TasmotaPlugBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.tasmotaplug.internal.TasmotaPlugConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TasmotaPlugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class TasmotaPlugHandler extends BaseThingHandler {
    private static final int DEFAULT_REFRESH_PERIOD_SEC = 30;

    private final Logger logger = LoggerFactory.getLogger(TasmotaPlugHandler.class);
    private final HttpClient httpClient;

    private @Nullable ScheduledFuture<?> refreshJob;

    private String plugHost = BLANK;
    private int refreshPeriod = DEFAULT_REFRESH_PERIOD_SEC;

    public TasmotaPlugHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TasmotaPlug handler.");
        TasmotaPlugConfiguration config = getConfigAs(TasmotaPlugConfiguration.class);

        final String hostName = config.hostName;
        final Integer refresh = config.refresh;

        if (hostName == null || BLANK.equals(hostName)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-hostname");
            return;
        }

        if (refresh != null) {
            this.refreshPeriod = refresh;
        }

        plugHost = "http://" + hostName;
        updateStatus(ThingStatus.UNKNOWN);

        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the TasmotaPlug handler.");

        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (POWER.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                sendCommand(SET_POWER + command);
            } else {
                updateChannelState();
            }
        } else {
            logger.warn("Unsupported command: {}", command.toString());
        }
    }

    /**
     * Start the job to periodically update the state of the plug
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            refreshJob = null;
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::updateChannelState, 0, refreshPeriod,
                    TimeUnit.SECONDS);
        }
    }

    private void updateChannelState() {
        String plugState = sendCommand(GET_POWER);
        if (plugState.contains(ON)) {
            updateState(POWER, OnOffType.ON);
        } else if (plugState.contains(OFF)) {
            updateState(POWER, OnOffType.OFF);
        }
    }

    private String sendCommand(String cmdUri) {
        try {
            logger.trace("Sending GET request to {}{}", plugHost, cmdUri);
            ContentResponse contentResponse = httpClient.GET(plugHost + cmdUri);
            logger.trace("Response: {}", contentResponse.getContentAsString());

            if (contentResponse.getStatus() != OK_200) {
                throw new IllegalStateException("Tasmota http response code was: " + contentResponse.getStatus());
            }

            updateStatus(ThingStatus.ONLINE);
            return contentResponse.getContentAsString();
        } catch (IllegalStateException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Error executing Tasmota GET request: '{}{}', {}", plugHost, cmdUri, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return BLANK;
    }
}
