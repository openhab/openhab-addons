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
package org.openhab.binding.groheondus.internal.handler;

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_BATTERY;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_HUMIDITY;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_PAUSE;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.CHANNEL_TEMPERATURE;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.api.ApplianceInfo;
import org.openhab.binding.groheondus.internal.api.ApplianceStatus;
import org.openhab.binding.groheondus.internal.api.GroheOndusApi;
import org.openhab.binding.groheondus.internal.api.GroheOndusApiException;
import org.openhab.binding.groheondus.internal.api.SenseSample;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the GROHE Sense water and climate sensor.
 *
 * @author Florian Schmidt - Initial contribution
 * @author Arne Seime - Publish historic measurements as time series, use own API client
 */
@NonNullByDefault
public class GroheOndusSenseHandler extends GroheOndusBaseHandler<GroheOndusSenseHandler.Snapshot> {

    // The Sense uploads its measurements once a day at best
    private static final int DATA_WINDOW_DAYS = 2;
    private static final Duration MAX_SILENCE = Duration.ofHours(48);

    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseHandler.class);

    record Snapshot(List<SenseSample> samples, ApplianceStatus status) {
        @Nullable
        SenseSample latest() {
            return samples.isEmpty() ? null : samples.get(samples.size() - 1);
        }
    }

    public GroheOndusSenseHandler(Thing thing, int thingCounter, TimeZoneProvider timeZoneProvider) {
        super(thing, ApplianceInfo.TYPE_SENSE, thingCounter, timeZoneProvider);
    }

    @Override
    protected Snapshot fetch(GroheOndusApi api, ApplianceInfo appliance) throws GroheOndusApiException {
        LocalDate today = LocalDate.now(timeZoneProvider.getTimeZone());
        LocalDate from = today.minusDays(DATA_WINDOW_DAYS);
        LocalDate to = today.plusDays(1);
        logger.debug("Fetching data for {} from {} to {}", thing.getUID(), from, to);
        List<SenseSample> samples = api.getSenseSamples(appliance.ref(), from, to);
        logger.debug("Received {} samples for {}", samples.size(), thing.getUID());
        return new Snapshot(samples, api.getStatus(appliance.ref()));
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, ApplianceInfo appliance, Snapshot snapshot) {
        SenseSample latest = snapshot.latest();
        State newState = switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_TEMPERATURE -> {
                sendHistory(channelUID, snapshot.samples(), SenseSample::timestamp, SenseSample::temperature,
                        SIUnits.CELSIUS);
                yield toState(latest == null ? null : latest.temperature(), SIUnits.CELSIUS);
            }
            case CHANNEL_HUMIDITY -> {
                sendHistory(channelUID, snapshot.samples(), SenseSample::timestamp, SenseSample::humidity,
                        Units.PERCENT);
                yield toState(latest == null ? null : latest.humidity(), Units.PERCENT);
            }
            case CHANNEL_BATTERY -> {
                // The cloud keeps reporting the last known battery level of a device that has gone silent
                Integer batteryLevel = snapshot.status().batteryLevel();
                yield batteryLevel == null || isDeviceSilent() ? UnDefType.UNDEF : new DecimalType(batteryLevel);
            }
            case CHANNEL_PAUSE -> getPauseState();
            default -> {
                logger.debug("Channel {} not supported", channelUID);
                yield UnDefType.UNDEF;
            }
        };
        updateState(channelUID, newState);
    }

    @Override
    protected Duration getMaxSilence() {
        return MAX_SILENCE;
    }

    @Override
    protected @Nullable Boolean isConnected(Snapshot snapshot) {
        return snapshot.status().connected();
    }

    @Override
    protected @Nullable Instant getNewestSampleTime(Snapshot snapshot) {
        SenseSample latest = snapshot.latest();
        return latest == null ? null : latest.timestamp();
    }
}
