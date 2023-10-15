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
package org.openhab.binding.buienradar.internal;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.buienradar.internal.buienradarapi.BuienradarPredictionAPI;
import org.openhab.binding.buienradar.internal.buienradarapi.Prediction;
import org.openhab.binding.buienradar.internal.buienradarapi.PredictionAPI;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BuienradarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Edwin de Jong - Initial contribution
 */
@NonNullByDefault
public class BuienradarHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BuienradarHandler.class);

    private final PredictionAPI client = new BuienradarPredictionAPI();

    private @NonNullByDefault({}) ScheduledFuture<?> listenableFuture;

    /**
     * Prevents race-condition access to listenableFuture.
     */
    private final Lock listenableFutureLock = new ReentrantLock();

    private @NonNullByDefault({}) PointType location;

    private @NonNullByDefault({}) BuienradarConfiguration config;

    public BuienradarHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(BuienradarConfiguration.class);

        boolean configValid = true;
        if (config.location == null || config.location.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-location");
            configValid = false;
        }

        try {
            location = new PointType(config.location);
        } catch (IllegalArgumentException e) {
            location = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-parsing-location");
            configValid = false;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);
        }
        try {
            listenableFutureLock.lock();
            if (listenableFuture == null || listenableFuture.isCancelled()) {
                listenableFuture = scheduler.scheduleWithFixedDelay(() -> refresh(), 0L, config.refreshIntervalMinutes,
                        MINUTES);
            }
        } finally {
            listenableFutureLock.unlock();
        }
    }

    private void refresh() {
        refresh(config.retries, ZonedDateTime.now().plusMinutes(config.refreshIntervalMinutes),
                config.exponentialBackoffRetryBaseInSeconds);
    }

    private void refresh(int tries, ZonedDateTime nextRefresh, int retryInSeconds) {
        if (nextRefresh.isBefore(ZonedDateTime.now())) {
            // The next refresh is already running, stop retries.
            return;
        }
        if (tries <= 0) {
            // We are out of tries, stop retrying.
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }
        try {
            final Optional<List<Prediction>> predictionsOpt = client.getPredictions(location);
            if (predictionsOpt.isEmpty()) {
                // Did not get a result, retry the retrieval.
                // Buienradar is not a very stable source and returns nothing quite regular
                if (tries <= 2) {
                    logger.warn(
                            "Did not get a result from buienradar. Retrying. {} tries remaining, waiting {} seconds.",
                            tries, retryInSeconds);
                } else {
                    logger.debug(
                            "Did not get a result from buienradar. Retrying. {} tries remaining, waiting {} seconds.",
                            tries, retryInSeconds);
                }
                scheduler.schedule(() -> refresh(tries - 1, nextRefresh, retryInSeconds * 2), retryInSeconds,
                        TimeUnit.SECONDS);
                return;
            }
            final List<Prediction> predictions = predictionsOpt.get();
            if (!predictions.isEmpty()) {
                final ZonedDateTime actual = predictions.get(0).getActualDateTime();
                updateState(BuienradarBindingConstants.ACTUAL_DATETIME, new DateTimeType(actual));
            }
            for (final Prediction prediction : predictions) {
                final BigDecimal intensity = prediction.getIntensity();

                final long minutesFromNow = prediction.getActualDateTime().until(prediction.getDateTimeOfPrediction(),
                        ChronoUnit.MINUTES);
                logger.debug("Forecast for {} at {} made at {} is {}", minutesFromNow,
                        prediction.getDateTimeOfPrediction(), prediction.getActualDateTime(), intensity);
                if (minutesFromNow >= 0 && minutesFromNow <= 115) {
                    final String label = String.format(Locale.ENGLISH, "forecast_%d", minutesFromNow);

                    updateState(label, new QuantityType<>(intensity, Units.MILLIMETRE_PER_HOUR));
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.warn("Cannot retrieve predictions", e);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Could not reach buienradar: %s", e.getMessage()));
        }
    }

    @Override
    public void dispose() {
        try {
            listenableFutureLock.lock();
            if (listenableFuture != null && !listenableFuture.isCancelled()) {
                listenableFuture.cancel(true);
                listenableFuture = null;
            }
        } finally {
            listenableFutureLock.unlock();
        }
    }
}
