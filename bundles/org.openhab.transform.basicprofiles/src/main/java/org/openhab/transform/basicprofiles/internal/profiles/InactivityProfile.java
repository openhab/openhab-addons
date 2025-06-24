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

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.INACTIVITY_UID;

import java.lang.ref.Cleaner;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.basicprofiles.internal.config.InactivityProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes the state of a {@link Switch} Item depending on whether data has been sent by the
 * binding during the configured timeout window.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class InactivityProfile implements StateProfile, AutoCloseable {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(1);
    private static final Cleaner CLEANER = Cleaner.create();

    public static final AtomicBoolean DEBUG_CLEANER_TASK_CALLED = new AtomicBoolean();

    private final Logger logger = LoggerFactory.getLogger(InactivityProfile.class);

    private static class CleanerTaskCanceller implements Runnable {
        private @Nullable ScheduledFuture<?> task;

        public void setTask(@Nullable ScheduledFuture<?> task) {
            this.task = task;
        }

        @Override
        public void run() {
            DEBUG_CLEANER_TASK_CALLED.set(true);
            if (task instanceof ScheduledFuture target) {
                target.cancel(true);
            }
        }
    }

    private final ProfileCallback callback;
    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final boolean inverted;
    private final CleanerTaskCanceller cleanerTaskCanceller;
    private boolean closed;

    private @Nullable ScheduledFuture<?> timeoutTask;

    public InactivityProfile(ProfileCallback callback, ProfileContext context) {
        InactivityProfileConfig config = context.getConfiguration().as(InactivityProfileConfig.class);

        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.inverted = config.inverted != null ? config.inverted : false;

        this.cleanerTaskCanceller = new CleanerTaskCanceller();
        CLEANER.register(this, cleanerTaskCanceller);

        Duration timeout;
        try {
            timeout = parseDuration(config.timeout);
            logger.debug("Profile created with timeout:{}, inverted:{}", timeout, inverted);
        } catch (IllegalArgumentException e) {
            timeout = DEFAULT_TIMEOUT;
            logger.warn("Profile configuration timeout value \"{}\" is invalid", config.timeout);
        }

        this.timeout = timeout;
        onStateUpdateFromHandler(UnDefType.NULL); // dummy to set initial item state
    }

    private Duration parseDuration(String timeOrDuration) throws IllegalArgumentException {
        if (timeOrDuration.isBlank()) {
            return DEFAULT_TIMEOUT;
        }
        if (QuantityType.valueOf(timeOrDuration) instanceof QuantityType<?> time
                && time.toUnit(MetricPrefix.MILLI(Units.SECOND)) instanceof QuantityType<?> msec) {
            return Duration.ofMillis(msec.longValue());
        }
        // TODO refactor when #4868 is merged: return DurationUtils.parse(timeOrDuration);
        throw new IllegalArgumentException("invalid time or duration");
    }

    private void onTimeout() {
        State itemState = OnOffType.from(!inverted);
        logger.debug("timeout:{} => itemState:{}", timeout, itemState);
        this.callback.sendUpdate(itemState);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return INACTIVITY_UID;
    }

    @Override
    public void onStateUpdateFromItem(State itemState) {
        // do nothing
    }

    @Override
    public void onCommandFromItem(Command itemCommand) {
        // do nothing
    }

    @Override
    public void onCommandFromHandler(Command handlerCommand) {
        Command itemCommand = OnOffType.from(inverted);
        logger.debug("handlerCommand:{} => itemCommand:{}", handlerCommand, itemCommand);
        callback.sendCommand(itemCommand);
        rescheduleTimeoutTask();
    }

    @Override
    public void onStateUpdateFromHandler(State handlerState) {
        State itemState = OnOffType.from(inverted);
        logger.debug("handlerState:{} => itemState:{}", handlerState, itemState);
        callback.sendUpdate(itemState);
        rescheduleTimeoutTask();
    }

    @Override
    public void close() throws Exception {
        cancelTimeoutTask();
        closed = true;
    }

    private synchronized void cancelTimeoutTask() {
        cleanerTaskCanceller.setTask(null);
        if (timeoutTask instanceof ScheduledFuture task) {
            task.cancel(false);
        }
        timeoutTask = null;
    }

    private synchronized void rescheduleTimeoutTask() {
        cancelTimeoutTask();
        if (!closed) {
            long mSec = timeout.toMillis();
            timeoutTask = scheduler.scheduleWithFixedDelay(() -> onTimeout(), mSec, mSec, TimeUnit.MILLISECONDS);
            if (timeoutTask instanceof ScheduledFuture task) {
                cleanerTaskCanceller.setTask(task);
            }
        }
    }
}
