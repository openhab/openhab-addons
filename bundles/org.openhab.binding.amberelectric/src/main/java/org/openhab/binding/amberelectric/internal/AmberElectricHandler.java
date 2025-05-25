/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amberelectric.internal;

import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amberelectric.internal.api.CurrentPrices;
import org.openhab.binding.amberelectric.internal.api.Sites;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * The {@link AmberElectricHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class AmberElectricHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AmberElectricHandler.class);

    private final TimeZoneProvider timeZoneProvider;

    private long refreshInterval;
    private String apiKey = "";
    private String nmi = "";
    private String siteID = "";

    private @NonNullByDefault({}) AmberElectricConfiguration config;
    private @NonNullByDefault({}) AmberElectricWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public AmberElectricHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This binding is read only");
    }

    @Override
    public void initialize() {
        config = getConfigAs(AmberElectricConfiguration.class);
        if (config.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-api-key");
            return;
        }

        webTargets = new AmberElectricWebTargets();
        updateStatus(ThingStatus.UNKNOWN);
        refreshInterval = config.refresh;
        nmi = config.nmi;
        apiKey = config.apiKey;

        schedulePoll();
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        logger.debug("Scheduling poll every {} s", refreshInterval);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshInterval, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to AmberAPI", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to AmberAPI", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void updatePriceChannel(String channel, double price) {
        final String electricityUnit = " AUD/kWh";
        Unit<?> unit = CurrencyUnits.getInstance().getUnit("AUD");
        if (unit == null) {
            logger.trace("Currency AUD is unknown, falling back to DecimalType");
            updateState(channel, new DecimalType(price / 100));
        } else {
            updateState(channel, new QuantityType<>(price / 100 + " " + electricityUnit));
        }
    }

    private void updatePriceTimeSeries(TimeSeries timeSeries, Instant instant, double price) {
        final String electricityUnit = " AUD/kWh";
        Unit<?> unit = CurrencyUnits.getInstance().getUnit("AUD");
        State state = (unit == null) ? new DecimalType(price / 100)
                : new QuantityType<>(price / 100 + " " + electricityUnit);
        timeSeries.add(instant, state);
    }

    private void pollStatus() throws IOException {
        try {
            if (siteID.isEmpty()) {
                Sites sites = webTargets.getSites(apiKey, nmi);
                // add error handling
                siteID = sites.siteid;
                Configuration configuration = editConfiguration();
                configuration.put("nmi", sites.nmi);
                updateConfiguration(configuration);
                logger.debug("Detected amber siteid is {}, for nmi {}", sites.siteid, sites.nmi);
            }
            updateStatus(ThingStatus.ONLINE);

            String response = webTargets.getCurrentPrices(siteID, apiKey);
            Gson gson = new Gson();
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
            CurrentPrices currentPrices;
            TimeSeries elecTimeSeries = new TimeSeries(REPLACE);

            for (int i = 0; i < jsonArray.size(); i++) {
                currentPrices = gson.fromJson(jsonArray.get(i), CurrentPrices.class);
                Instant instantStart = Instant.parse(currentPrices.startTime);
                if ("CurrentInterval".equals(currentPrices.type) && "general".equals(currentPrices.channelType)) {
                    updateState(AmberElectricBindingConstants.CHANNEL_ELECTRICITY_STATUS,
                            new StringType(currentPrices.descriptor));
                    updateState(AmberElectricBindingConstants.CHANNEL_NEM_TIME, new StringType(currentPrices.nemTime));
                    updateState(AmberElectricBindingConstants.CHANNEL_RENEWABLES,
                            new DecimalType(currentPrices.renewables));
                    updateState(AmberElectricBindingConstants.CHANNEL_SPIKE,
                            OnOffType.from(!"none".equals(currentPrices.spikeStatus)));
                    updatePriceChannel(AmberElectricBindingConstants.CHANNEL_ELECTRICITY_PRICE, currentPrices.perKwh);
                    updatePriceTimeSeries(elecTimeSeries, instantStart, currentPrices.perKwh);
                }
                if ("ForecastInterval".equals(currentPrices.type) && "general".equals(currentPrices.channelType)) {
                    updatePriceTimeSeries(elecTimeSeries, instantStart, currentPrices.perKwh);
                }
                if ("CurrentInterval".equals(currentPrices.type) && "feedIn".equals(currentPrices.channelType)) {
                    updateState(AmberElectricBindingConstants.CHANNEL_FEED_IN_STATUS,
                            new StringType(currentPrices.descriptor));
                    updatePriceChannel(AmberElectricBindingConstants.CHANNEL_FEED_IN_PRICE, currentPrices.perKwh);
                }
                if ("CurrentInterval".equals(currentPrices.type)
                        && "controlledLoad".equals(currentPrices.channelType)) {
                    updateState(AmberElectricBindingConstants.CHANNEL_CONTROLLED_LOAD_STATUS,
                            new StringType(currentPrices.descriptor));
                    updatePriceChannel(AmberElectricBindingConstants.CHANNEL_CONTROLLED_LOAD_STATUS,
                            currentPrices.perKwh);
                }
            }
            sendTimeSeries(AmberElectricBindingConstants.CHANNEL_ELECTRICITY_PRICE, elecTimeSeries);
        } catch (AmberElectricCommunicationException e) {
            logger.debug("Unexpected error connecting to Amber Electric API", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
