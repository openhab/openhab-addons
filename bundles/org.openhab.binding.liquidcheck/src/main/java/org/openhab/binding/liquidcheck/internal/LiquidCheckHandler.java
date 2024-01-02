/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.liquidcheck.internal;

import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.CONTENT_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.FILL_INDICATOR_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.LEVEL_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.MEASURE_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PUMP_TOTAL_RUNS_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.PUMP_TOTAL_RUNTIME_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.RAW_CONTENT_CHANNEL;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.RAW_LEVEL_CHANNEL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.liquidcheck.internal.httpclient.LiquidCheckHttpClient;
import org.openhab.binding.liquidcheck.internal.json.CommData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link LiquidCheckHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LiquidCheckHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LiquidCheckHandler.class);
    private final HttpClient httpClient;

    private Map<String, String> oldProps = new HashMap<>();

    private LiquidCheckConfiguration config = new LiquidCheckConfiguration();
    private @Nullable LiquidCheckHttpClient client;

    private @Nullable ScheduledFuture<?> polling;

    public LiquidCheckHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    @SuppressWarnings("null")
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(MEASURE_CHANNEL)) {
            if (command instanceof OnOffType) {
                try {
                    LiquidCheckHttpClient client = this.client;
                    if (client != null && client.isConnected()) {
                        String response = client.measureCommand();
                        CommData commandResponse = new Gson().fromJson(response, CommData.class);
                        if (commandResponse != null && !"".equals(commandResponse.header.name)) {
                            if (!"success".equals(commandResponse.context.status)) {
                                logger.warn("Starting the measurement was not successful!");
                            }
                        } else {
                            logger.debug("The object commandResponse is null!");
                        }
                    }
                } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
                    logger.warn("This went wrong in handleCommand: {}", e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                updateState(channelUID, OnOffType.OFF);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(LiquidCheckConfiguration.class);
        oldProps = thing.getProperties();

        updateStatus(ThingStatus.UNKNOWN);
        var client = new LiquidCheckHttpClient(config, httpClient);
        this.client = client;
        PollingForData pollingRunnable = new PollingForData(client);
        polling = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> polling = this.polling;
        if (null != polling) {
            polling.cancel(true);
            this.polling = null;
        }
    }

    private class PollingForData implements Runnable {

        private final LiquidCheckHttpClient client;

        public PollingForData(LiquidCheckHttpClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                String jsonString = client.pollData();
                CommData response = new Gson().fromJson(jsonString, CommData.class);
                if (response != null && !"".equals(response.header.messageId)) {
                    Map<String, String> properties = response.createPropertyMap();
                    if (!oldProps.equals(properties)) {
                        oldProps = properties;
                        updateProperties(properties);
                    }
                    updateState(CONTENT_CHANNEL, new QuantityType<>(response.payload.measure.content, Units.LITRE));
                    updateState(LEVEL_CHANNEL, new QuantityType<>(response.payload.measure.level, SIUnits.METRE));
                    updateState(RAW_CONTENT_CHANNEL,
                            new QuantityType<>(response.payload.measure.raw.content, Units.LITRE));

                    updateState(RAW_LEVEL_CHANNEL,
                            new QuantityType<>(response.payload.measure.raw.level, SIUnits.METRE));

                    updateState(PUMP_TOTAL_RUNS_CHANNEL, new DecimalType(response.payload.system.pump.totalRuns));
                    updateState(PUMP_TOTAL_RUNTIME_CHANNEL,
                            new QuantityType<>(response.payload.system.pump.totalRuntime, Units.SECOND));
                    if (config.maxContent > 1) {
                        double fillIndicator = response.payload.measure.content / config.maxContent * 100;
                        updateState(FILL_INDICATOR_CHANNEL, new QuantityType<>(fillIndicator, Units.PERCENT));
                    }
                    if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    logger.debug("Json is null");
                }
            } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
