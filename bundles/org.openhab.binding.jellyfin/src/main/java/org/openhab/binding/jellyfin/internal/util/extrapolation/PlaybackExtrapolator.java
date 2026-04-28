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
package org.openhab.binding.jellyfin.internal.util.extrapolation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.tick.TickConverter;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides per-second client-side extrapolation of Jellyfin playback position.
 *
 * <p>
 * When a session reports active playback, the Jellyfin server sends position updates only
 * periodically. {@code PlaybackExtrapolator} reduces UI lag by incrementing the local
 * position counter once per second and publishing updated percentage/seconds channel states
 * between server updates.
 *
 * <p>
 * Usage:
 * <ol>
 * <li>Create an instance with the required callbacks.</li>
 * <li>Call {@link #start(SessionInfoDto)} when a playing (non-paused) session is received.</li>
 * <li>Call {@link #stop()} when playback pauses, ends, or a new session snapshot is about to
 * be applied – the new snapshot will set the authoritative position.</li>
 * <li>Call {@link #dispose()} when the owning handler is destroyed.</li>
 * </ol>
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public final class PlaybackExtrapolator {

    private final Logger logger = LoggerFactory.getLogger(PlaybackExtrapolator.class);

    private final Predicate<String> linkedChecker;
    private final BiConsumer<String, State> stateUpdater;
    private final Runnable timestampRefresher;
    private final ScheduledExecutorService scheduler;
    private final boolean ownedScheduler;

    @Nullable
    private ScheduledFuture<?> task;

    private volatile long extrapolatedPositionTicks = -1L;
    private volatile boolean running = false;

    /**
     * Production constructor – creates a dedicated single-thread scheduler for this extrapolator.
     *
     * @param deviceId device ID used to name the worker thread
     * @param linkedChecker returns {@code true} if the given channel ID has a linked item
     * @param stateUpdater publishes a new state for the given channel ID
     * @param timestampRefresher called each tick to prevent session-timeout detection during playback
     */
    public PlaybackExtrapolator(String deviceId, Predicate<String> linkedChecker,
            BiConsumer<String, State> stateUpdater, Runnable timestampRefresher) {
        this(linkedChecker, stateUpdater, timestampRefresher,
                Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "jellyfin-client-extrap-" + deviceId)),
                true);
    }

    /**
     * Test / injection constructor – caller provides and owns the scheduler lifecycle.
     *
     * @param linkedChecker returns {@code true} if the given channel ID has a linked item
     * @param stateUpdater publishes a new state for the given channel ID
     * @param timestampRefresher called each tick to prevent session-timeout detection during playback
     * @param scheduler scheduler to use for the per-second tick task
     */
    public PlaybackExtrapolator(Predicate<String> linkedChecker, BiConsumer<String, State> stateUpdater,
            Runnable timestampRefresher, ScheduledExecutorService scheduler) {
        this(linkedChecker, stateUpdater, timestampRefresher, scheduler, false);
    }

    private PlaybackExtrapolator(Predicate<String> linkedChecker, BiConsumer<String, State> stateUpdater,
            Runnable timestampRefresher, ScheduledExecutorService scheduler, boolean ownedScheduler) {
        this.linkedChecker = linkedChecker;
        this.stateUpdater = stateUpdater;
        this.timestampRefresher = timestampRefresher;
        this.scheduler = scheduler;
        this.ownedScheduler = ownedScheduler;
    }

    /**
     * Starts per-second position extrapolation for the given session.
     *
     * <p>
     * Does nothing if the session is {@code null}, has no playing item, or playback is paused.
     * Any previously running task is stopped before starting a new one.
     *
     * @param session the current session snapshot from the Jellyfin server
     */
    public void start(@Nullable SessionInfoDto session) {
        stop();

        if (session == null) {
            return;
        }
        var playState = session.getPlayState();
        var playingItem = session.getNowPlayingItem();
        if (playState == null || playingItem == null) {
            return;
        }
        if (Boolean.TRUE.equals(playState.getIsPaused())) {
            return;
        }

        Long pos = playState.getPositionTicks();
        extrapolatedPositionTicks = pos != null ? pos : 0L;
        running = true;

        final Long runTimeTicks = playingItem.getRunTimeTicks();

        task = scheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }
            try {
                extrapolatedPositionTicks += TickConverter.TICKS_PER_SECOND;

                // Refresh timeout so the session-timeout monitor doesn't mark the client offline
                // while extrapolation is actively running.
                timestampRefresher.run();

                long pos2 = extrapolatedPositionTicks;

                if (runTimeTicks != null && runTimeTicks > 0) {
                    int percent = TickConverter.ticksToPercent(pos2, runTimeTicks);
                    if (linkedChecker.test(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL)) {
                        stateUpdater.accept(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL, new PercentType(percent));
                    }
                    long maxSecs = TickConverter.ticksToSeconds(runTimeTicks);
                    long secs = Math.min(TickConverter.ticksToSeconds(pos2), maxSecs);
                    if (secs < 0) {
                        secs = 0;
                    }
                    if (linkedChecker.test(Constants.PLAYING_ITEM_SECOND_CHANNEL)) {
                        stateUpdater.accept(Constants.PLAYING_ITEM_SECOND_CHANNEL, new DecimalType(secs));
                    }
                } else {
                    long secs = TickConverter.ticksToSeconds(pos2);
                    if (secs < 0) {
                        secs = 0;
                    }
                    if (linkedChecker.test(Constants.PLAYING_ITEM_SECOND_CHANNEL)) {
                        stateUpdater.accept(Constants.PLAYING_ITEM_SECOND_CHANNEL, new DecimalType(secs));
                    }
                }

                if (linkedChecker.test(Constants.MEDIA_CONTROL_CHANNEL)) {
                    stateUpdater.accept(Constants.MEDIA_CONTROL_CHANNEL, PlayPauseType.PLAY);
                }
            } catch (Exception e) {
                logger.debug("Extrapolation tick error: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops extrapolation and resets the internal position to {@code -1}.
     * Safe to call even if extrapolation is not currently running.
     */
    public void stop() {
        running = false;
        ScheduledFuture<?> currentTask = task;
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            task = null;
        }
        extrapolatedPositionTicks = -1L;
    }

    /**
     * Stops extrapolation and shuts down the owned scheduler (if this instance created it).
     * Must be called when the owning handler is destroyed.
     */
    public void dispose() {
        stop();
        if (ownedScheduler && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Returns the current extrapolated position in Jellyfin ticks,
     * or {@code -1} if extrapolation is not running.
     *
     * @return extrapolated position ticks, or {@code -1}
     */
    public long getExtrapolatedPositionTicks() {
        return extrapolatedPositionTicks;
    }
}
