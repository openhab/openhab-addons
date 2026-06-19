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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.DEBOUNCE_STATE_UID;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.transform.basicprofiles.internal.config.DebounceStateProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delays an {@link OnOffType}/{@link OpenClosedType} {@link State} per direction. Active values (ON/OPEN) are held for
 * {@code onDelay}, inactive values (OFF/CLOSED) for {@code offDelay}. A pending forward is cancelled by an opposing
 * value; repeated identical values do not reschedule.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class DebounceStateProfile implements StateProfile {
    private final Logger logger = LoggerFactory.getLogger(DebounceStateProfile.class);

    private final ProfileCallback callback;
    private final DebounceStateProfileConfig config;
    private final ScheduledExecutorService scheduler;

    private @Nullable State lastForwarded;
    private @Nullable Boolean pendingActive;
    private @Nullable ScheduledFuture<?> pendingJob;
    private long pendingGeneration;

    public DebounceStateProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.scheduler = context.getExecutorService();
        this.config = context.getConfiguration().as(DebounceStateProfileConfig.class);
        logger.debug("Configuring profile with parameters: {}", config);

        if (config.onDelay < 0) {
            throw new IllegalArgumentException(
                    String.format("onDelay has to be a non-negative integer but was '%d'.", config.onDelay));
        }
        if (config.offDelay < 0) {
            throw new IllegalArgumentException(
                    String.format("offDelay has to be a non-negative integer but was '%d'.", config.offDelay));
        }
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return DEBOUNCE_STATE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // no-op: this profile only shapes the handler -> item direction
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        // commands are not state and are forwarded immediately; only state updates are delayed
        callback.sendCommand(command);
    }

    @Override
    public synchronized void onStateUpdateFromHandler(State state) {
        Boolean active = asActive(state);
        if (active == null) {
            // non-binary values (e.g. UNDEF/NULL) are forwarded immediately and reset the delay state
            cancelPending();
            doForward(state);
            return;
        }

        // 1. a job for the same value is already pending -> let the running timer continue
        if (pendingJob != null && active.equals(pendingActive)) {
            return;
        }
        // 2. a job for the opposite value is pending -> cancel it
        if (pendingJob != null) {
            cancelPending();
        }
        // 3. already in the target state -> nothing to forward
        State last = lastForwarded;
        if (last != null && active.equals(asActive(last))) {
            return;
        }
        // 4. forward now or schedule
        int delay = active ? config.onDelay : config.offDelay;
        if (delay <= 0) {
            doForward(state);
        } else {
            pendingActive = active;
            long generation = ++pendingGeneration;
            pendingJob = scheduler.schedule(() -> {
                synchronized (DebounceStateProfile.this) {
                    if (generation != pendingGeneration) {
                        // superseded or cancelled while we waited for the lock
                        return;
                    }
                    doForward(state);
                    pendingJob = null;
                    pendingActive = null;
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void doForward(State state) {
        lastForwarded = state;
        callback.sendUpdate(state);
    }

    private void cancelPending() {
        ScheduledFuture<?> job = pendingJob;
        if (job != null) {
            job.cancel(false);
        }
        // invalidate any already-running task that is waiting for the lock
        pendingGeneration++;
        pendingJob = null;
        pendingActive = null;
    }

    private static @Nullable Boolean asActive(State state) {
        if (state instanceof OnOffType onOff) {
            return onOff == OnOffType.ON;
        }
        if (state instanceof OpenClosedType openClosed) {
            return openClosed == OpenClosedType.OPEN;
        }
        return null;
    }
}
