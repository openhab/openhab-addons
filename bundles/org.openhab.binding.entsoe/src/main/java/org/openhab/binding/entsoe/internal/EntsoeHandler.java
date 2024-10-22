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
package org.openhab.binding.entsoe.internal;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.entsoe.internal.client.Client;
import org.openhab.binding.entsoe.internal.client.EntsoeRequest;
import org.openhab.binding.entsoe.internal.client.SpotPrice;
import org.openhab.binding.entsoe.internal.exception.EntsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseMapException;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EntsoeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Melhus - Initial contribution
 */
@NonNullByDefault
public class EntsoeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EntsoeHandler.class);
    private final ZoneId cetZoneId = ZoneId.of("CET");
    private final Client client;

    private EntsoeConfiguration config = new EntsoeConfiguration();
    private @Nullable ScheduledFuture<?> refreshJob;
    private Map<Instant, SpotPrice> entsoeTimeSeries = new LinkedHashMap<>();
    private ZonedDateTime lastDayAheadReceived = ZonedDateTime.of(LocalDateTime.MIN, cetZoneId);
    private int historicDaysInitially = 0;

    public EntsoeHandler(final Thing thing, final HttpClient httpClient) {
        super(thing);
        this.client = new Client(httpClient);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("channelLinked(channelUID:{})", channelUID.getAsString());
        String channelID = channelUID.getId();

        if (EntsoeBindingConstants.CHANNEL_SPOT_PRICE.equals(channelID)) {
            if (entsoeTimeSeries.isEmpty()) {
                refreshPrices();
            }
            updateCurrentState(EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
        }
    }

    @Override
    public void dispose() {
        entsoeTimeSeries.clear();
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(channelUID:{}, command:{})", channelUID.getAsString(), command.toFullString());

        if (command instanceof RefreshType) {
            try {
                fetchNewPrices();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EntsoeConfiguration.class);
        lastDayAheadReceived = ZonedDateTime.of(LocalDateTime.MIN, cetZoneId);

        if (historicDaysInitially == 0) {
            historicDaysInitially = config.historicDays;
        }

        if (isLinked(EntsoeBindingConstants.CHANNEL_SPOT_PRICE)) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }

        refreshJob = scheduler.schedule(this::refreshPrices, 0, TimeUnit.SECONDS);
    }

    private ZonedDateTime currentCetTime() {
        return ZonedDateTime.now(cetZoneId);
    }

    private ZonedDateTime currentCetTimeWholeHours() {
        return currentCetTime().truncatedTo(ChronoUnit.HOURS);
    }

    private long getMillisToNextStateUpdate() {
        Instant now = Instant.now();
        Instant nextHour = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS);
        long millis = Duration.between(now, nextHour).toMillis();
        try {
            Instant nextInstant = getNextSpotPrice().getInstant();
            millis = Duration.between(now, nextInstant).toMillis();
        } catch (EntsoeResponseMapException e) {
            logger.warn("Using millis to next whole hour");
        }
        return millis + 1;
    }

    private SpotPrice getCurrentSpotPrice() throws EntsoeResponseMapException {
        Duration duration = Duration.parse(config.resolution);
        Instant now = Instant.now();

        for (Map.Entry<Instant, SpotPrice> entry : entsoeTimeSeries.entrySet()) {
            Instant entryStart = entry.getKey();
            Instant entryEnd = entryStart.plus(duration);

            if (!now.isBefore(entryStart) && now.isBefore(entryEnd)) {
                return entry.getValue();
            }
        }
        throw new EntsoeResponseMapException("Could not get current SpotPrice");
    }

    private SpotPrice getNextSpotPrice() throws EntsoeResponseMapException {
        Instant now = Instant.now();

        for (Map.Entry<Instant, SpotPrice> entry : entsoeTimeSeries.entrySet()) {
            Instant entryStart = entry.getKey();

            if (entryStart.isAfter(now)) {
                return entry.getValue();
            }
        }
        throw new EntsoeResponseMapException("Could not get next SpotPrice");
    }

    private boolean needToFetchHistoricDays() {
        return needToFetchHistoricDays(false);
    }

    private boolean needToFetchHistoricDays(boolean updateHistoricDaysInitially) {
        boolean needToFetch = false;
        if (historicDaysInitially < config.historicDays) {
            logger.debug("Need to fetch historic data. Historicdays was changed to a greater number: {}",
                    config.historicDays);
            needToFetch = true;
        }

        if (updateHistoricDaysInitially && historicDaysInitially != config.historicDays) {
            historicDaysInitially = config.historicDays;
        }

        return needToFetch;
    }

    private void refreshPrices() {
        if (!isLinked(EntsoeBindingConstants.CHANNEL_SPOT_PRICE)) {
            logger.debug("Channel {} is not linked, skipping channel update",
                    EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
            return;
        }

        if (entsoeTimeSeries.isEmpty()) {
            try {
                fetchNewPrices();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        boolean needsInitialUpdate = lastDayAheadReceived.equals(ZonedDateTime.of(LocalDateTime.MIN, cetZoneId))
                || needToFetchHistoricDays(true);
        Instant nextDayInstant = ZonedDateTime.now(cetZoneId).plusDays(1).with(LocalTime.MIDNIGHT).toInstant();
        boolean hasNextDayValue = entsoeTimeSeries.containsKey(nextDayInstant);
        boolean readyForNextDayValue = currentCetTime()
                .isAfter(currentCetTimeWholeHours().withHour(config.spotPricesAvailableCetHour));

        if (needsInitialUpdate || (!hasNextDayValue && readyForNextDayValue)) {
            try {
                fetchNewPrices();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        } else {
            updateCurrentState(EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
            schedule(true);
        }
    }

    private void fetchNewPrices() throws InterruptedException {
        logger.trace("Fetching new prices");

        Instant startUtc = ZonedDateTime.now(cetZoneId)
                .minusDays(needToFetchHistoricDays() ? config.historicDays - 1 : 0).with(LocalTime.MIDNIGHT)
                .toInstant();
        Instant endUtc = ZonedDateTime.now(cetZoneId).plusDays(2).with(LocalTime.MIDNIGHT).toInstant();

        EntsoeRequest request = new EntsoeRequest(config.securityToken, config.area, startUtc, endUtc);
        boolean success = false;

        try {
            entsoeTimeSeries = client.doGetRequest(request, config.requestTimeout, config.resolution);

            TimeSeries baseTimeSeries = new TimeSeries(EntsoeBindingConstants.TIMESERIES_POLICY);
            for (Map.Entry<Instant, SpotPrice> entry : entsoeTimeSeries.entrySet()) {
                baseTimeSeries.add(entry.getValue().getInstant(), entry.getValue().getState(Units.KILOWATT_HOUR));
            }

            updateStatus(ThingStatus.ONLINE);
            lastDayAheadReceived = currentCetTime();
            sendTimeSeries(EntsoeBindingConstants.CHANNEL_SPOT_PRICE, baseTimeSeries);
            updateCurrentState(EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
            triggerChannel(EntsoeBindingConstants.CHANNEL_TRIGGER_PRICES_RECEIVED);
            success = true;
        } catch (EntsoeResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (EntsoeConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } finally {
            schedule(success);
        }
    }

    private void schedule(boolean success) {
        if (!success) {
            logger.trace("schedule(success:{})", success);
            refreshJob = scheduler.schedule(this::refreshPrices, 5, TimeUnit.MINUTES);
        } else {
            refreshJob = scheduler.schedule(this::refreshPrices, getMillisToNextStateUpdate(), TimeUnit.MILLISECONDS);
        }
    }

    private void updateCurrentState(String channelID) {
        logger.trace("Updating current state");
        try {
            updateState(channelID, getCurrentSpotPrice().getState(Units.KILOWATT_HOUR));
        } catch (EntsoeConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }
}
