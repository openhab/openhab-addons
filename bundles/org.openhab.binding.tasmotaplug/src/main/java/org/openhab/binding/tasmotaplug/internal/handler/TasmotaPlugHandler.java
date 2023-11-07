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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.tasmotaplug.internal.TasmotaPlugConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
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
    private int numChannels = 1;

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
        final Integer numChannels = config.numChannels;

        if (hostName == null || hostName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-hostname");
            return;
        }

        if (refresh != null) {
            this.refreshPeriod = refresh;
        }

        if (numChannels != null) {
            this.numChannels = numChannels;
        }

        plugHost = "http://" + hostName;

        // remove the channels we are not using
        if (this.numChannels < SUPPORTED_CHANNEL_IDS.size()) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

            List<Integer> channelsToRemove = IntStream.range(this.numChannels + 1, SUPPORTED_CHANNEL_IDS.size() + 1)
                    .boxed().collect(Collectors.toList());

            channelsToRemove.forEach(channel -> {
                channels.removeIf(c -> (c.getUID().getId().equals(POWER + channel)));
            });
            updateThing(editThing().withChannels(channels).build());
        }

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
        if (channelUID.getId().contains(POWER)) {
            if (command instanceof OnOffType) {
                sendCommand(channelUID.getId(), CMD_URI + "%%20" + command);
            } else {
                updateChannelState(channelUID.getId());
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
            this.refreshJob = scheduler.scheduleWithFixedDelay(() -> {
                SUPPORTED_CHANNEL_IDS.stream().limit(numChannels).forEach(channel -> {
                    updateChannelState(channel);
                });
            }, 0, refreshPeriod, TimeUnit.SECONDS);
        }
    }

    private void updateChannelState(String channel) {
        final String plugState = sendCommand(channel, CMD_URI);
        if (plugState.contains(ON)) {
            updateState(channel, OnOffType.ON);
        } else if (plugState.contains(OFF)) {
            updateState(channel, OnOffType.OFF);
        }
    }

    private String sendCommand(String channel, String cmdUri) {
        try {
            final String url = String.format(cmdUri, channel.substring(0, 1).toUpperCase() + channel.substring(1));
            logger.trace("Sending GET request to {}{}", plugHost, url);
            ContentResponse contentResponse = httpClient.GET(plugHost + url);
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
