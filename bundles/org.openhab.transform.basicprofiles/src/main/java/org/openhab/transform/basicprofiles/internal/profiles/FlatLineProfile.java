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
import org.openhab.core.types.UnDefType;
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
public class FlatLineProfile implements StateProfile {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofHours(1);

    private final Logger logger = LoggerFactory.getLogger(FlatLineProfile.class);

    private final ProfileCallback callback;
    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final Boolean inverted;
    private final Runnable onTimeout;

    private @Nullable ScheduledFuture<?> timeoutTask;

    public FlatLineProfile(ProfileCallback callback, ProfileContext context) {
        long milliSeconds = 0;
        FlatLineProfileConfig config = context.getConfiguration().as(FlatLineProfileConfig.class);
        try {
            QuantityType<?> timeout = QuantityType.valueOf(config.timeout);
            if (!Units.SECOND.getDimension().equals(timeout.getDimension())) {
                throw new IllegalArgumentException();
            }
            milliSeconds = timeout.toUnit(MetricPrefix.MILLI(Units.SECOND)) instanceof QuantityType<?> mS
                    ? mS.longValue()
                    : 0;
            if (milliSeconds <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Profile configuration timeout \"{}\" is invalid", config.timeout);
        }

        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.timeout = Duration.ofMillis(milliSeconds > 0 ? milliSeconds : DEFAULT_TIMEOUT.toMillis());
        this.inverted = Boolean.valueOf(config.inverted);
        this.onTimeout = () -> {
            this.callback.sendUpdate(OnOffType.from(!inverted));
        };

        createTimeoutTask();

        this.callback.sendUpdate(UnDefType.UNDEF);
        logger.debug("Created profile with timeout:{}, inverted:{}", timeout, inverted);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return FLAT_LINE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do nothing
    }

    @Override
    public void onCommandFromItem(Command command) {
        // do nothing
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand(OnOffType.from(inverted));
        createTimeoutTask();
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate(OnOffType.from(inverted));
        createTimeoutTask();
    }

    private synchronized void createTimeoutTask() {
        ScheduledFuture<?> priorTask = timeoutTask;
        if (priorTask != null) {
            priorTask.cancel(false);
        }
        timeoutTask = scheduler.schedule(onTimeout, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
