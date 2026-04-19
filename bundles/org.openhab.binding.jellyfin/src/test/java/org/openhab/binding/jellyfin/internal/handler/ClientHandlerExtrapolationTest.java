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
package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.PlayerStateInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.gen.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.extrapolation.PlaybackExtrapolator;

/**
 * Unit tests for {@link PlaybackExtrapolator}.
 *
 * <p>
 * Tests exercise the per-second tick increment logic that was formerly embedded in
 * {@code ClientHandler}. Using the extracted class directly removes the need for reflection
 * and anonymous-subclass workarounds.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class ClientHandlerExtrapolationTest {

    private @NonNullByDefault({}) ScheduledExecutorService exec;
    private @NonNullByDefault({}) PlaybackExtrapolator extrapolator;

    private final AtomicLong refreshCount = new AtomicLong();
    private final AtomicReference<String> lastChannelUpdated = new AtomicReference<>("");
    private final AtomicBoolean percentageChannelUpdated = new AtomicBoolean();

    @BeforeEach
    void setUp() {
        exec = Executors.newSingleThreadScheduledExecutor();
        percentageChannelUpdated.set(false);
        extrapolator = new PlaybackExtrapolator(
                // linked-channel checker: all channels linked
                channelId -> true,
                // state updater: record which channel was last updated and track percentage
                (channelId, state) -> {
                    lastChannelUpdated.set(channelId);
                    if (Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL.equals(channelId)) {
                        percentageChannelUpdated.set(true);
                    }
                },
                // timestamp refresher
                refreshCount::incrementAndGet, exec);
    }

    @AfterEach
    void tearDown() {
        extrapolator.dispose();
        exec.shutdownNow();
    }

    @Test
    void testExtrapolationIncrementsPositionTicks() throws InterruptedException {
        SessionInfoDto session = buildPlayingSession(0L, 100_000_000L);

        extrapolator.start(session);

        // Wait slightly more than one tick interval
        Thread.sleep(1_200);

        long pos = extrapolator.getExtrapolatedPositionTicks();
        // After one tick: position must have advanced by exactly 10_000_000 ticks
        assertEquals(10_000_000L, pos, "Position should have advanced by one tick (10_000_000) after ~1 s");
    }

    @Test
    void testExtrapolationDoesNotStartWhenPaused() throws InterruptedException {
        SessionInfoDto session = buildSession(0L, 100_000_000L, true);

        extrapolator.start(session);
        Thread.sleep(1_200);

        assertEquals(-1L, extrapolator.getExtrapolatedPositionTicks(),
                "Extrapolation must not start when session is paused");
    }

    @Test
    void testExtrapolationDoesNotStartWithoutPlayingItem() throws InterruptedException {
        SessionInfoDto session = new SessionInfoDto();
        session.setId("sess-no-item");
        PlayerStateInfo playState = new PlayerStateInfo();
        playState.setIsPaused(false);
        playState.setPositionTicks(0L);
        session.setPlayState(playState);
        // NowPlayingItem deliberately not set

        extrapolator.start(session);
        Thread.sleep(1_200);

        assertEquals(-1L, extrapolator.getExtrapolatedPositionTicks(),
                "Extrapolation must not start without a NowPlayingItem");
    }

    @Test
    void testStopResetsPosition() throws InterruptedException {
        SessionInfoDto session = buildPlayingSession(0L, 100_000_000L);

        extrapolator.start(session);
        Thread.sleep(1_200);

        assertTrue(extrapolator.getExtrapolatedPositionTicks() > 0);

        extrapolator.stop();

        assertEquals(-1L, extrapolator.getExtrapolatedPositionTicks(), "stop() must reset position to -1");
    }

    @Test
    void testTimestampRefresherIsCalledEachTick() throws InterruptedException {
        SessionInfoDto session = buildPlayingSession(0L, 100_000_000L);

        extrapolator.start(session);
        Thread.sleep(2_200);

        assertTrue(refreshCount.get() >= 2, "Timestamp refresher should have been called at least twice after ~2 s");
    }

    @Test
    void testPercentageChannelIsUpdated() throws InterruptedException {
        SessionInfoDto session = buildPlayingSession(0L, 100_000_000L);

        extrapolator.start(session);
        Thread.sleep(1_200);

        assertTrue(percentageChannelUpdated.get(), "Percentage channel should be updated when runtime is known");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static SessionInfoDto buildPlayingSession(long positionTicks, long runTimeTicks) {
        return buildSession(positionTicks, runTimeTicks, false);
    }

    private static SessionInfoDto buildSession(long positionTicks, long runTimeTicks, boolean paused) {
        SessionInfoDto session = new SessionInfoDto();
        session.setId("sess-test");

        PlayerStateInfo playState = new PlayerStateInfo();
        playState.setIsPaused(paused);
        playState.setPositionTicks(positionTicks);
        session.setPlayState(playState);

        BaseItemDto item = new BaseItemDto();
        item.setRunTimeTicks(runTimeTicks);
        session.setNowPlayingItem(item);

        return session;
    }
}
