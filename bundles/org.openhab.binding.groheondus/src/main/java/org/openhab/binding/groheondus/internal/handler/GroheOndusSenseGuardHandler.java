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

import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.api.ApplianceInfo;
import org.openhab.binding.groheondus.internal.api.GroheOndusApi;
import org.openhab.binding.groheondus.internal.api.GroheOndusApiException;
import org.openhab.binding.groheondus.internal.api.GuardData;
import org.openhab.binding.groheondus.internal.api.GuardSample;
import org.openhab.binding.groheondus.internal.api.WaterWithdrawal;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the GROHE Sense Guard water valve.
 *
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 * @author Arne Seime - Publish historic measurements as time series, use own API client
 */
@NonNullByDefault
public class GroheOndusSenseGuardHandler extends GroheOndusBaseHandler<GroheOndusSenseGuardHandler.Snapshot> {
    private static final int MIN_API_TIMEFRAME_DAYS = 1;
    private static final int MAX_API_TIMEFRAME_DAYS = 90;
    private static final int DEFAULT_TIMEFRAME_DAYS = 1;
    // The Sense Guard transmits every 15 minutes by default
    private static final Duration MAX_SILENCE = Duration.ofHours(6);

    private final Logger logger = LoggerFactory.getLogger(GroheOndusSenseGuardHandler.class);

    record Snapshot(GuardData data, @Nullable Boolean valveOpen, ZoneId zone) {
        @Nullable
        GuardSample latest() {
            List<GuardSample> measurements = data.measurements();
            return measurements.isEmpty() ? null : measurements.get(measurements.size() - 1);
        }
    }

    public GroheOndusSenseGuardHandler(Thing thing, int thingCounter, TimeZoneProvider timeZoneProvider) {
        super(thing, ApplianceInfo.TYPE_SENSE_GUARD, thingCounter, timeZoneProvider);
    }

    @Override
    protected int getPollingInterval(ApplianceInfo appliance) {
        if (config.pollingInterval > 0) {
            return config.pollingInterval;
        }
        Integer transmissionInterval = appliance.measurementTransmissionInterval();
        return transmissionInterval != null && transmissionInterval > 0 ? transmissionInterval
                : DEFAULT_POLLING_INTERVAL;
    }

    @Override
    protected Snapshot fetch(GroheOndusApi api, ApplianceInfo appliance) throws GroheOndusApiException {
        ZoneId zone = timeZoneProvider.getTimeZone();
        LocalDate today = LocalDate.now(zone);
        LocalDate from = today.minusDays(getTimeframeDays());
        LocalDate to = today.plusDays(1);
        logger.debug("Fetching data for {} from {} to {}", thing.getUID(), from, to);
        GuardData data = api.getGuardData(appliance.ref(), from, to);
        logger.debug("Received {} measurements and {} withdrawals for {}", data.measurements().size(),
                data.withdrawals().size(), thing.getUID());
        return new Snapshot(data, api.isValveOpen(appliance.ref()), zone);
    }

    @Override
    protected void updateChannel(ChannelUID channelUID, ApplianceInfo appliance, Snapshot snapshot) {
        GuardSample latest = snapshot.latest();
        List<GuardSample> measurements = snapshot.data().measurements();
        State newState = switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_PRESSURE -> {
                sendHistory(channelUID, measurements, GuardSample::timestamp, GuardSample::pressure, Units.BAR);
                yield toState(latest == null ? null : latest.pressure(), Units.BAR);
            }
            case CHANNEL_TEMPERATURE_GUARD -> {
                sendHistory(channelUID, measurements, GuardSample::timestamp, GuardSample::temperature,
                        SIUnits.CELSIUS);
                yield toState(latest == null ? null : latest.temperature(), SIUnits.CELSIUS);
            }
            case CHANNEL_PAUSE -> getPauseState();
            case CHANNEL_VALVE_OPEN -> {
                Boolean valveOpen = snapshot.valveOpen();
                yield valveOpen == null ? UnDefType.UNDEF : OnOffType.from(valveOpen);
            }
            case CHANNEL_WATERCONSUMPTION -> sumWaterConsumption(snapshot.data().withdrawals(), Instant.MIN);
            case CHANNEL_WATERCONSUMPTION_SINCE_MIDNIGHT -> sumWaterConsumption(snapshot.data().withdrawals(),
                    LocalDate.now(snapshot.zone()).atStartOfDay(snapshot.zone()).toInstant());
            default -> {
                logger.debug("Channel {} not supported", channelUID);
                yield UnDefType.UNDEF;
            }
        };
        updateState(channelUID, newState);
    }

    static QuantityType<?> sumWaterConsumption(List<WaterWithdrawal> withdrawals, Instant since) {
        // Sum as BigDecimal to keep the decimal values reported by the API exact
        BigDecimal litres = withdrawals.stream().filter(withdrawal -> !withdrawal.timestamp().isBefore(since))
                .map(withdrawal -> BigDecimal.valueOf(withdrawal.waterConsumption()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new QuantityType<>(litres, Units.LITRE);
    }

    @Override
    protected Duration getMaxSilence() {
        return MAX_SILENCE;
    }

    @Override
    protected @Nullable Instant getNewestSampleTime(Snapshot snapshot) {
        GuardSample latest = snapshot.latest();
        return latest == null ? null : latest.timestamp();
    }

    private int getTimeframeDays() {
        Channel waterConsumptionChannel = thing.getChannel(CHANNEL_WATERCONSUMPTION);
        if (waterConsumptionChannel == null) {
            return DEFAULT_TIMEFRAME_DAYS;
        }
        Object timeframeConfig = waterConsumptionChannel.getConfiguration().get(CHANNEL_CONFIG_TIMEFRAME);
        if (!(timeframeConfig instanceof BigDecimal timeframeValue)) {
            return DEFAULT_TIMEFRAME_DAYS;
        }
        int timeframe = timeframeValue.intValue();
        if (timeframe < MIN_API_TIMEFRAME_DAYS || timeframe > MAX_API_TIMEFRAME_DAYS) {
            logger.debug("Timeframe of the waterconsumption channel must be between {} and {} days, got {}",
                    MIN_API_TIMEFRAME_DAYS, MAX_API_TIMEFRAME_DAYS, timeframe);
            return DEFAULT_TIMEFRAME_DAYS;
        }
        return timeframe;
    }

    @Override
    protected void handleApplianceCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_VALVE_OPEN.equals(channelUID.getIdWithoutGroup())) {
            super.handleApplianceCommand(channelUID, command);
            return;
        }
        if (!(command instanceof OnOffType onOffCommand)) {
            logger.debug("Invalid command received for channel {}. Expected OnOffType, received {}.", channelUID,
                    command.getClass().getName());
            return;
        }
        runApiCommand(api -> {
            api.setValveOpen(getApplianceRef(), onOffCommand == OnOffType.ON);
            updateState(channelUID, onOffCommand);
        });
    }
}
