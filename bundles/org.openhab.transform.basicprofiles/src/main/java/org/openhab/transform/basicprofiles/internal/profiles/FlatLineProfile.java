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

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.FLAT_LINE_UID;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.transform.basicprofiles.internal.config.FlatLineProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Changes the state of a {@link Switch} Item depending on whether data has been sent by the
 * binding during the configured timeout window.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class FlatLineProfile implements StateProfile, AutoCloseable {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(1);

    private final Logger logger = LoggerFactory.getLogger(FlatLineProfile.class);

    private final ProfileCallback callback;
    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final boolean inverted;
    private final Runnable onTimeout;
    private boolean closed;

    private @Nullable ScheduledFuture<?> timeoutTask;

    public FlatLineProfile(ProfileCallback callback, ProfileContext context) {
        FlatLineProfileConfig config = context.getConfiguration().as(FlatLineProfileConfig.class);
        long mSec = 0;
        try {
            QuantityType<?> timeQty = QuantityType.valueOf(config.timeout);
            if (!Units.SECOND.getDimension().equals(timeQty.getDimension())) {
                throw new IllegalArgumentException();
            }
            mSec = timeQty.toUnit(MetricPrefix.MILLI(Units.SECOND)) instanceof QuantityType<?> mSecQty
                    ? mSecQty.longValue()
                    : 0;
            if (mSec <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Profile configuration timeout value \"{}\" is invalid", config.timeout);
        }

        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.timeout = mSec > 0 ? Duration.ofMillis(mSec) : DEFAULT_TIMEOUT;
        this.inverted = config.inverted != null ? config.inverted : false;

        this.onTimeout = () -> {
            State itemState = OnOffType.from(!inverted);
            logger.debug("timeout:{} => itemState:{}", timeout, itemState);
            this.callback.sendUpdate(itemState);
        };

        rescheduleTimeoutTask();
        logger.debug("Created(timeout:{}, inverted:{})", timeout, inverted);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return FLAT_LINE_UID;
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

    private void cancelTimeoutTask() {
        ScheduledFuture<?> priorTask = timeoutTask;
        if (priorTask != null) {
            priorTask.cancel(false);
        }
    }

    private synchronized void rescheduleTimeoutTask() {
        cancelTimeoutTask();
        if (!closed) {
            long mSec = timeout.toMillis();
            timeoutTask = scheduler.scheduleWithFixedDelay(onTimeout, mSec, mSec, TimeUnit.MILLISECONDS);
        }
    }
}
