/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.transform.basicprofiles.internal.config.DebounceTimeStateProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debounces a {@link State} by time.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DebounceTimeStateProfile implements StateProfile {
    private final Logger logger = LoggerFactory.getLogger(DebounceTimeStateProfile.class);

    private final ProfileCallback callback;
    private final DebounceTimeStateProfileConfig config;
    private final ScheduledExecutorService scheduler;

    private @Nullable ScheduledFuture<?> toHandlerJob;
    private @Nullable ScheduledFuture<?> toItemJob;

    public DebounceTimeStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.config = context.getConfiguration().as(DebounceTimeStateProfileConfig.class);
        logger.debug("Configuring profile with parameters: {}", config);

        if (config.toHandlerDelay < 0) {
            throw new IllegalArgumentException(String
                    .format("toHandlerDelay has to be a non-negative integer but was '%d'.", config.toHandlerDelay));
        }

        if (config.toItemDelay < 0) {
            throw new IllegalArgumentException(
                    String.format("toItemDelay has to be a non-negative integer but was '%d'.", config.toItemDelay));
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return DEBOUNCE_TIME_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op
    }

    @Override
    public void onCommandFromItem(Command command) {
        logger.debug("Received command '{}' from item", command);
        if (config.toHandlerDelay == 0) {
            callback.handleCommand(command);
            return;
        }
        ScheduledFuture<?> localToHandlerJob = toHandlerJob;
        if (config.mode == DebounceTimeStateProfileConfig.DebounceMode.LAST) {
            if (localToHandlerJob != null) {
                // if we have an old job, cancel it
                localToHandlerJob.cancel(true);
            }
            logger.trace("Scheduling command '{}'", command);
            scheduleToHandler(() -> {
                logger.debug("Sending command '{}' to handler", command);
                callback.handleCommand(command);
            });
        } else {
            if (localToHandlerJob == null) {
                // send the value only if we don't have a job
                callback.handleCommand(command);
                scheduleToHandler(null);
            } else {
                logger.trace("Discarding command to handler '{}'", command);
            }
        }
    }

    private void scheduleToHandler(@Nullable Runnable function) {
        toHandlerJob = scheduler.schedule(() -> {
            if (function != null) {
                function.run();
            }
            toHandlerJob = null;
        }, config.toHandlerDelay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        logger.debug("Received command '{}' from handler", command);
        if (config.toItemDelay == 0) {
            callback.sendCommand(command);
            return;
        }

        ScheduledFuture<?> localToItemJob = toItemJob;
        if (config.mode == DebounceTimeStateProfileConfig.DebounceMode.LAST) {
            if (localToItemJob != null) {
                // if we have an old job, cancel it
                localToItemJob.cancel(true);
            }
            logger.trace("Scheduling command '{}' to item", command);
            scheduleToItem(() -> {
                logger.debug("Sending command '{}' to item", command);
                callback.sendCommand(command);
            });
        } else {
            if (localToItemJob == null) {
                // only schedule a new job if we have none
                callback.sendCommand(command);
                scheduleToItem(null);
            } else {
                logger.trace("Discarding command to item '{}'", command);
            }
        }
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        logger.debug("Received state update from Handler");
        if (config.toItemDelay == 0) {
            callback.sendUpdate(state);
            return;
        }
        ScheduledFuture<?> localToItemJob = toItemJob;
        if (config.mode == DebounceTimeStateProfileConfig.DebounceMode.LAST) {
            if (localToItemJob != null) {
                // if we have an old job, cancel it
                localToItemJob.cancel(true);
            }
            logger.trace("Scheduling state update '{}' to item", state);
            scheduleToItem(() -> {
                logger.debug("Sending state update '{}' to item", state);
                callback.sendUpdate(state);
            });
        } else {
            if (toItemJob == null) {
                // only schedule a new job if we have none
                callback.sendUpdate(state);
                scheduleToItem(null);
            } else {
                logger.trace("Discarding state update to item '{}'", state);
            }
        }
    }

    private void scheduleToItem(@Nullable Runnable function) {
        toItemJob = scheduler.schedule(() -> {
            if (function != null) {
                function.run();
            }
            toItemJob = null;
        }, config.toItemDelay, TimeUnit.MILLISECONDS);
    }
}
