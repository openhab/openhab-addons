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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.entsoe.internal.client.Client;
import org.openhab.binding.entsoe.internal.client.EntsoeTimeSerie;
import org.openhab.binding.entsoe.internal.client.Request;
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

    private EntsoeConfiguration config;

    private @Nullable ScheduledFuture<?> refreshJob;

    private List<EntsoeTimeSerie> entsoeTimeSeries;

    // Use region-based ZoneId instead of CET to handle transition to CEST automatically
    private final ZoneId cetZoneId = ZoneId.of("Europe/Paris");
    private final ZoneId utcZoneId = ZoneId.of("UTC");

    private ZonedDateTime lastDayAheadReceived = ZonedDateTime.of(LocalDateTime.MIN, cetZoneId);

    private int historicDaysInitially = 0;

    public EntsoeHandler(Thing thing) {
        super(thing);
        config = new EntsoeConfiguration();
        entsoeTimeSeries = new ArrayList<>();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("channelLinked(channelUID:{})", channelUID.getAsString());
        String channelID = channelUID.getId();

        if (EntsoeBindingConstants.CHANNEL_SPOT_PRICE.equals(channelID)) {
            if (entsoeTimeSeries.isEmpty()) {
                refreshPrices();
            }
            updateCurrentHourState(EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
        }
    }

    @Override
    public void dispose() {
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
            fetchNewPrices();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EntsoeConfiguration.class);

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

    private ZonedDateTime currentUtcTime() {
        return ZonedDateTime.now(utcZoneId);
    }

    private ZonedDateTime currentUtcTimeWholeHours() {
        return currentUtcTime().truncatedTo(ChronoUnit.HOURS);
    }

    private EntsoeTimeSerie getCurrentHourEntsoeTimeSerie() throws EntsoeResponseMapException {
        if (entsoeTimeSeries.isEmpty()) {
            throw new EntsoeResponseMapException("responseMap is empty");
        }
        var currentUtcTime = currentUtcTime();
        Integer currentHour = currentUtcTime.getHour();
        Integer currentDayInYear = currentUtcTime.getDayOfYear();
        var result = entsoeTimeSeries.stream()
                .filter(x -> x.getTime().getHour() == currentHour && x.getTime().getDayOfYear() == currentDayInYear)
                .findFirst();
        if (result.isEmpty() || result == null || result.get() == null) {
            throw new EntsoeResponseMapException(
                    String.format("Could not find a value for hour {} and day in year {} in responseMap", currentHour,
                            currentDayInYear));
        } else {
            return result.get();
        }
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

        config = getConfigAs(EntsoeConfiguration.class);

        if (entsoeTimeSeries.isEmpty()) {
            fetchNewPrices();
            return;
        }

        boolean needsInitialUpdate = lastDayAheadReceived.equals(ZonedDateTime.of(LocalDateTime.MIN, cetZoneId))
                || needToFetchHistoricDays(true);

        boolean hasNextDayValue = entsoeTimeSeries.stream()
                .anyMatch(x -> x.getTime().getDayOfYear() == currentUtcTime().plusDays(1).getDayOfYear());

        boolean readyForNextDayValue = currentCetTime()
                .isAfter(currentCetTime().withHour(config.spotPricesAvailableCetHour));

        if (needsInitialUpdate || (!hasNextDayValue && readyForNextDayValue)) {
            fetchNewPrices();
        } else {
            updateCurrentHourState(EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
            schedule(true);
        }
    }

    private void fetchNewPrices() {
        logger.trace("Fetching new prices");
        ZonedDateTime startUtc = currentUtcTimeWholeHours()
                .minusDays(needToFetchHistoricDays() ? config.historicDays : 1).withHour(22);
        ZonedDateTime endUtc = currentUtcTimeWholeHours().plusDays(1).withHour(22);

        Request request = new Request(config.securityToken, config.area, startUtc, endUtc);
        Client client = new Client();
        boolean success = false;

        try {
            entsoeTimeSeries = client.doGetRequest(request, 30000);

            TimeSeries baseTimeSeries = new TimeSeries(EntsoeBindingConstants.TIMESERIES_POLICY);
            for (EntsoeTimeSerie entsoeTimeSerie : entsoeTimeSeries) {
                baseTimeSeries.add(entsoeTimeSerie.getTime().toInstant(),
                        entsoeTimeSerie.getState(Units.KILOWATT_HOUR));
            }

            updateStatus(ThingStatus.ONLINE);
            lastDayAheadReceived = currentCetTime();
            sendTimeSeries(EntsoeBindingConstants.CHANNEL_SPOT_PRICE, baseTimeSeries);
            updateCurrentHourState(EntsoeBindingConstants.CHANNEL_SPOT_PRICE);
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
            Instant now = Instant.now();
            long millisUntilNextClockHour = Duration
                    .between(now, now.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS)).toMillis() + 1;
            refreshJob = scheduler.schedule(this::refreshPrices, millisUntilNextClockHour, TimeUnit.MILLISECONDS);
        }
    }

    private void updateCurrentHourState(String channelID) {
        logger.trace("Updating current hour");
        try {
            updateState(channelID, getCurrentHourEntsoeTimeSerie().getState(Units.KILOWATT_HOUR));
        } catch (EntsoeConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }
}
