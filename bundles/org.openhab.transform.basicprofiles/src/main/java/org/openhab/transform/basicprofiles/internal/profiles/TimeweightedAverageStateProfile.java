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

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.TIME_WEIGHTED_AVERAGE_UID;

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
 * Build time-weighted average {@link State} values.
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
    private String itemName;
    private @Nullable ScheduledFuture<?> twaJob;
    private @Nullable State latestState;
    private @Nullable Unit<?> stateUnit;

    public TimeweightedAverageStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.config = context.getConfiguration().as(TimeweightedAverageProfileConfig.class);
        itemName = callback.getItemChannelLink().getItemName();
        try {
            scheduleDuration = DurationUtils.parse(config.duration);
        } catch (IllegalArgumentException e) {
            scheduleDuration = DEFAULT_TIMEOUT;
            logger.warn("Invalid duration configuration {} for item {}. Fallback to {}", config.duration, itemName,
                    DEFAULT_TIMEOUT);
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return TIME_WEIGHTED_AVERAGE_UID;
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        logger.trace("Received {} {}", itemName, state);

        // first call initialization and threshold check
        // synchronize access to timeframe and latestState to avoid parallel execution of delivery by twaJob
        synchronized (timeframe) {
            if (latestState == null) {
                init(state);
            }

            // if state change is above delta threshold, deliver immediately collected values plus latest reported state
            if (deltaExceeded(state)) {
                deliver();
                callback.sendUpdate(state);
            }

            // start new time frame
            startJob();
            timeframe.put(Instant.now(), state);
            latestState = state;
            streamingInTimeframe = true;
        }
    }

    private void init(State first) {
        if (first instanceof QuantityType<?> qtState) {
            stateUnit = qtState.getUnit();
            logger.debug("Profile initialized for {} with QuantityType {}", itemName, stateUnit);
        } else if (first instanceof DecimalType) {
            // nothing to do, stateUnit stays empty
            logger.debug("Profile initialized for {} with DecimalType", itemName);
        } else {
            logger.error("Time-weighted average profile not applicable for {} with class {}", itemName,
                    first.getClass());
        }
    }

    private boolean deltaExceeded(State state) {
        if (config.delta > 0) {
            State localLatestState = latestState;
            if (localLatestState != null) {
                double delta = Math.abs(state2Double(state) - state2Double(localLatestState));
                if (delta >= config.delta) {
                    logger.debug("{} rapid change from {} to {}", itemName, latestState, state);
                    return true;
                }
            }
        }
        return false;
    }

    private void deliver() {
        TreeMap<Instant, State> delivery = prepareDelivery();
        if (delivery.size() <= 1) {
            logger.debug("Cannot calculate time-weighted average for item {} with {} elements", itemName,
                    delivery.size());
        } else {
            callback.sendUpdate(getState(average(delivery)));
        }
    }

    private TreeMap<Instant, State> prepareDelivery() {
        TreeMap<Instant, State> delivery;
        // synchronize access to timeframe and latestState to prepare delivery without parallel execution of
        // onStateUpdateFromHandler
        synchronized (timeframe) {
            resetJob();
            delivery = new TreeMap<>(timeframe);
            // add termination element
            Instant now = Instant.now();
            delivery.put(now, DecimalType.ZERO);
            // clear time frame and put latest reported state as start point of the next calculation
            timeframe.clear();
            State localState = latestState;
            if (localState != null) {
                if (streamingInTimeframe) {
                    // state updates retrieved in time frame, start new job
                    timeframe.put(now, localState);
                    streamingInTimeframe = false;
                    startJob();
                } else {
                    // no new states received during this time frame
                    logger.debug("No new states for {} received, wait for next state update", itemName);
                }
            }
        }
        return delivery;
    }

    private void startJob() {
        if (twaJob == null) {
            logger.trace("Start next time frame for {} with delay {} ms", itemName, scheduleDuration.toMillis());
            twaJob = scheduler.schedule(this::deliver, scheduleDuration.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private void resetJob() {
        ScheduledFuture<?> localTwaJob = twaJob;
        if (localTwaJob != null) {
            localTwaJob.cancel(false);
            twaJob = null;
        }
    }

    private double state2Double(State state) {
        DecimalType as = state.as(DecimalType.class);
        if (as == null) {
            // may happen if state delivery contains NULL or UNDEF states
            logger.warn("Cannot convert state {} of item {} to DecimalType for average calculation", state, itemName);
            return 0;
        }
        return as.doubleValue();
    }

    public double average(TreeMap<Instant, State> values) {
        Instant previousTimestamp = null;
        State previousValue = DecimalType.ZERO;
        double totalWeightedValue = 0;
        long totalDurationMs = 0;

        for (Map.Entry<Instant, State> entry : values.entrySet()) {
            if (previousTimestamp != null) {
                long durationMs = Duration.between(previousTimestamp, entry.getKey()).toMillis();
                totalWeightedValue += state2Double(previousValue) * durationMs;
                totalDurationMs += durationMs;
            }
            previousTimestamp = entry.getKey();
            previousValue = entry.getValue();
        }
        double average = (totalDurationMs > 0) ? totalWeightedValue / totalDurationMs : 0;
        logger.debug("Average {} is {} for {} updates", itemName, average, values.size());
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
