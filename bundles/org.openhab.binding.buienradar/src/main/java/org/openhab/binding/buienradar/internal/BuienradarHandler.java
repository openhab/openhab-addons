/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;

import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.buienradar.internal.buienradarapi.BuienradarPredictionAPI;
import org.openhab.binding.buienradar.internal.buienradarapi.Prediction;
import org.openhab.binding.buienradar.internal.buienradarapi.PredictionAPI;
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

    private @NonNullByDefault({}) BuienradarConfiguration config;

    private final PredictionAPI client = new BuienradarPredictionAPI();

    private @NonNullByDefault({}) ScheduledFuture<?> listenableFuture;

    public BuienradarHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(BuienradarConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        if (listenableFuture == null || listenableFuture.isCancelled()) {
            listenableFuture = scheduler.scheduleWithFixedDelay(this::refresh, 0L, config.refreshIntervalMinutes,
                    MINUTES);
        }
    }

    private void refresh() {
        try {
            @SuppressWarnings("null")
            final List<Prediction> predictions = client.getPredictions(config.latitude, config.longitude);
            for (final Prediction prediction : predictions) {
                final BigDecimal intensity = prediction.getIntensity();
                final ZonedDateTime now = ZonedDateTime.now();
                final ZonedDateTime lastFiveMinute = now.withMinute((now.getMinute() / 5) * 5).withSecond(0)
                        .withNano(0);
                final long minutesFromNow = lastFiveMinute.until(prediction.getDateTime(), ChronoUnit.MINUTES);
                final long minuteClass = minutesFromNow;
                logger.debug("Forecast for {} at {} is {}", minutesFromNow, prediction.getDateTime(), intensity);
                if (minuteClass >= 0 && minuteClass <= 115) {
                    final String label = String.format("forecast_%d", minuteClass);
                    updateState(label, new QuantityType<Speed>(intensity, SmartHomeUnits.MILLIMETRE_PER_HOUR));
                }
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.warn("Cannot retrieve predictions", e);
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Could not reach buienradar: %s", e.getMessage()));
        }
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        if (listenableFuture != null && !listenableFuture.isCancelled()) {
            listenableFuture.cancel(true);
            listenableFuture = null;
        }
    }
}
