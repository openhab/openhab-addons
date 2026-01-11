/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.entsoe.internal.handler;

import static org.openhab.binding.entsoe.internal.EntsoeBindingConstants.CRON_DAILY_AT;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.entsoe.internal.EntsoeBindingConstants;
import org.openhab.binding.entsoe.internal.client.EntsoeClient;
import org.openhab.binding.entsoe.internal.client.EntsoeDocumentParser;
import org.openhab.binding.entsoe.internal.client.EntsoeRequest;
import org.openhab.binding.entsoe.internal.client.SpotPrice;
import org.openhab.binding.entsoe.internal.config.EntsoeConfiguration;
import org.openhab.binding.entsoe.internal.exception.EntsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
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
 * @author Bernd Weymann - Cron scheduling
 */
@NonNullByDefault
public class EntsoeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EntsoeHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private final EntsoeClient client;
    private final CronScheduler cron;

    private EntsoeConfiguration config = new EntsoeConfiguration();
    private EntsoeDocumentParser parser = new EntsoeDocumentParser();
    private TreeMap<Instant, SpotPrice> priceMap = new TreeMap<>();
    private @Nullable ScheduledCompletableFuture<?> cronDaily;
    private @Nullable ScheduledFuture<?> retryJob;
    private @Nullable Duration targetDuration;

    public EntsoeHandler(final Thing thing, final HttpClient httpClient, TimeZoneProvider timeZoneProvider,
            CronScheduler cron) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.client = new EntsoeClient(httpClient);
        this.cron = cron;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshJob = this.retryJob;
        ScheduledFuture<?> cronDaily = this.cronDaily;
        if (cronDaily != null) {
            cronDaily.cancel(true);
            this.cronDaily = null;
        }
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.retryJob = null;
        }
        priceMap.clear();
        targetDuration = null;
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand(channelUID:{}, command:{})", channelUID.getAsString(), command.toFullString());
        if (command instanceof RefreshType) {
            // update channels with cached data
            updateChannels();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EntsoeConfiguration.class);
        if (!checkConfig()) {
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::refreshPrices);
        // calculate local hour for cron scheduling
        ZonedDateTime nowCET = ZonedDateTime.now(ZoneId.of("CET")).withHour(config.spotPricesAvailableCetHour);
        int cronHour = nowCET.withZoneSameInstant(timeZoneProvider.getTimeZone()).getHour();
        logger.info("Schedule cron with pattern {}", String.format(CRON_DAILY_AT, cronHour));
        cronDaily = cron.schedule(this::refreshPrices, String.format(CRON_DAILY_AT, cronHour));
    }

    private boolean checkConfig() {
        if (config.securityToken.isBlank() || config.area.isBlank()) {
            handleConfigError("Security token or area is not configured");
            return false;
        }
        if (!config.resolution.isBlank()) {
            try {
                targetDuration = Duration.parse(config.resolution);
            } catch (DateTimeParseException e) {
                handleConfigError("Resolution " + config.resolution + " is not a valid ISO-8601 duration");
                return false;
            }
        }
        return true;
    }

    private void refreshPrices() {
        if (shouldFetchNewPrices()) {
            boolean forceUpdate = priceMap.isEmpty();
            fetchNewPrices();
            if (!dayAheadCheck()) {
                retryJob = scheduler.schedule(this::refreshPrices, 1, TimeUnit.MINUTES);
                if (!forceUpdate) {
                    return;
                }
            }
            updateChannels();
        }
    }

    private boolean shouldFetchNewPrices() {
        logger.debug("Fetch new prices? Empty={} Hour{}<={}", priceMap.isEmpty(), config.spotPricesAvailableCetHour,
                Instant.now().atZone(timeZoneProvider.getTimeZone()).getHour());
        return priceMap.isEmpty()
                || config.spotPricesAvailableCetHour <= Instant.now().atZone(timeZoneProvider.getTimeZone()).getHour();
    }

    private void updateChannels() {
        sendTimeSeries(EntsoeBindingConstants.CHANNEL_SPOT_PRICE, getTimeseries());
        if (dayAheadCheck()) {
            triggerChannel(EntsoeBindingConstants.CHANNEL_TRIGGER_PRICES_RECEIVED);
        }
    }

    private void fetchNewPrices() {
        logger.debug("Fetching new prices from {} to {}", queryStartTime(), queryEndTime());
        EntsoeRequest request = new EntsoeRequest(config.securityToken, config.area, queryStartTime(), queryEndTime());
        try {
            String response = client.doGetRequest(request, config.requestTimeout);
            processResponse(response);
        } catch (EntsoeResponseException e) {
            handleResponseError(e.getMessage());
        } catch (EntsoeConfigurationException e) {
            handleConfigError(e.getMessage());
        }
    }

    private void processResponse(String response) {
        parser = new EntsoeDocumentParser(response);
        if (parser.isValid()) {
            updateStatus(ThingStatus.ONLINE);
            priceMap = parser.getPriceMap(parser.getSequences().firstKey());
            logger.debug("Fetched {} price entries", priceMap.size());
        } else {
            handleResponseError(parser.getFailureReason());
        }
    }

    private Instant calculateQueryTime(int daysOffset) {
        return LocalDate.now().plusDays(daysOffset).atStartOfDay(timeZoneProvider.getTimeZone()).toInstant();
    }

    private Instant queryStartTime() {
        return calculateQueryTime(-config.historicDays);
    }

    private Instant queryEndTime() {
        return calculateQueryTime(2);
    }

    private boolean dayAheadCheck() {
        boolean dayAheadAvailable = priceMap.higherEntry(Instant.now().plus(1, ChronoUnit.DAYS)) != null;
        logger.debug("Day-ahead check: {}", dayAheadAvailable);
        return dayAheadAvailable;
    }

    private TimeSeries getTimeseries() {
        logger.debug("Get TimeSeries for {} entries and target duration {}", priceMap.size(), targetDuration);
        TimeSeries timeSeries = new TimeSeries(EntsoeBindingConstants.TIMESERIES_POLICY);
        TreeMap<Instant, SpotPrice> deliveryMap = priceMap;
        Duration resolution = targetDuration;
        if (resolution != null) {
            try {
                deliveryMap = parser.transform(parser.getSequences().firstKey(), resolution);
            } catch (EntsoeResponseException e) {
                handleResponseError(e.getMessage());
            }
        }
        for (Map.Entry<Instant, SpotPrice> entry : deliveryMap.entrySet()) {
            timeSeries.add(entry.getKey(), entry.getValue().getState());
        }
        logger.debug("TimeSeries from {} to {}", timeSeries.getBegin(), timeSeries.getEnd());
        return timeSeries;
    }

    private void handleConfigError(@Nullable String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
    }

    private void handleResponseError(@Nullable String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }
}
