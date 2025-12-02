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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.DEBOUNCE_TIME_UID;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.util.DurationUtils;
import org.openhab.transform.basicprofiles.internal.config.TimeweightedAverageProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debounces a {@link State} by time.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TimeweightedAverageStateProfile implements StateProfile {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(1);

    private final Logger logger = LoggerFactory.getLogger(TimeweightedAverageStateProfile.class);
    private final TreeMap<Instant, State> timeframe = new TreeMap<>();
    private final TimeweightedAverageProfileConfig config;
    private final ScheduledExecutorService scheduler;
    private final ProfileCallback callback;

    private boolean streamingInTimeframe = false;
    private Duration scheduleDuration;
    private @Nullable ScheduledFuture<?> twaJob;
    private @Nullable State latestState;
    private @Nullable Unit<?> stateUnit;

    public TimeweightedAverageStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.config = context.getConfiguration().as(TimeweightedAverageProfileConfig.class);

        try {
            scheduleDuration = DurationUtils.parse(config.duration);
        } catch (IllegalArgumentException e) {
            scheduleDuration = DEFAULT_TIMEOUT;
            logger.warn("Inavlid duration configuration {}", config.duration);
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return DEBOUNCE_TIME_UID;
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        logger.trace("Received state {}", state);
        if (latestState == null) {
            init(state);
        }
        synchronized (timeframe) {
            startJob();
            timeframe.put(Instant.now(), state);
            latestState = state;
            streamingInTimeframe = true;
        }
    }

    private void init(State first) {
        if (first instanceof QuantityType<?> qtState) {
            stateUnit = qtState.getUnit();
            logger.debug("Profile initialized for QunatityTypes {}", stateUnit);
        } else if (first instanceof DecimalType) {
            // nothing to do, ustateUnit stays empty
            logger.debug("Profile initialized for Numbers");
        } else {
            logger.error("Time-weighted average profile not applicable for {}", first.getClass());
        }
    }

    private void deliver() {
        TreeMap<Instant, State> delivery = new TreeMap<>();
        synchronized (timeframe) {
            delivery = (TreeMap<Instant, State>) timeframe.clone();
            // add termination element
            delivery.put(Instant.now(), DecimalType.ZERO);
            // clear timeframe and put latest reported state as start point of the next calculation
            timeframe.clear();
            resetJob();

            State localState = latestState;
            if (localState != null) {
                if (streamingInTimeframe) {
                    // state updates retrieved in time frame, start new job
                    timeframe.put(Instant.now(), localState);
                    streamingInTimeframe = false;
                    startJob();
                } else {
                    // no new states received during this time frame
                    logger.debug("no new states received, wait for next state update");
                }
            }
        }

        if (delivery.size() <= 1) {
            logger.debug("Cannot calculate time-weighted average with {} elements", delivery.size());
        } else {
            callback.sendUpdate(getState(average(delivery)));
        }
    }

    private void startJob() {
        if (twaJob == null) {
            logger.debug("Start new job with delay {} ms", scheduleDuration.toMillis());
            twaJob = scheduler.schedule(this::deliver, scheduleDuration.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void resetJob() {
        twaJob = null;
    }

    public double average(TreeMap<Instant, State> values) {
        Instant iterationTimestamp = null;
        State iterationValue = null;
        double totalWeightedValue = 0;
        long totalDurationMs = 0;

        for (Map.Entry<Instant, State> entry : values.entrySet()) {
            if (iterationTimestamp == null) {
                iterationTimestamp = entry.getKey();
                iterationValue = entry.getValue();
            } else {
                Instant end = entry.getKey();
                long durationMs = Duration.between(iterationTimestamp, end).toMillis();
                totalWeightedValue += iterationValue.as(DecimalType.class).doubleValue() * durationMs;
                totalDurationMs += durationMs;
                iterationTimestamp = end;
                iterationValue = entry.getValue();
            }
        }
        double average = (totalDurationMs > 0) ? totalWeightedValue / totalDurationMs : 0;
        logger.debug("Average {} for {} ms", average, totalDurationMs);
        return average;
    }

    private State getState(double average) {
        Unit<?> localUnit = stateUnit;
        if (localUnit == null) {
            return new DecimalType(average);
        } else {
            return new QuantityType<>(average, localUnit);
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op
    }

    @Override
    public void onCommandFromItem(Command command) {
        // no-op
    }

    @Override
    public void onCommandFromHandler(Command command) {
        // no-op
    }
}
