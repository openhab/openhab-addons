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
package org.openhab.binding.sonos.internal.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ONE_THING_TYPE_UID;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonos.internal.SonosStateDescriptionOptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;

/**
 * Tests when notification playback may clear the player's queue.
 *
 * @author Leo Siepel - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ZonePlayerHandlerNotificationTest {

    private static final String UDN = "RINCON_000000000000001400";
    private static final String AV_TRANSPORT = "AVTransport";
    private static final StringType NOTIFICATION = new StringType("http://localhost:8080/audio/notification.mp3");

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @Mock @NonNullByDefault({}) UpnpIOService upnpIOService;
    private @Mock @NonNullByDefault({}) SonosStateDescriptionOptionProvider stateDescriptionProvider;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @Nullable ZonePlayerHandler handler;

    @AfterEach
    public void disposePlayer() {
        ZonePlayerHandler handler = this.handler;
        if (handler != null) {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "STOPPED", "TRANSITIONING", "PLAYING" })
    public void notificationWaitsForPlaybackBeforeClearingQueue(@Nullable String initialState) throws Exception {
        NotificationPlayer player = createPlayer(30);
        if (initialState != null) {
            player.onValueReceived("TransportState", initialState, AV_TRANSPORT);
        }

        FutureTask<@Nullable Void> notification = new FutureTask<>(() -> {
            player.playNotificationSoundURI(NOTIFICATION);
            return null;
        });
        Thread playbackThread = Thread.ofPlatform().daemon().unstarted(notification);
        playbackThread.start();
        try {
            assertTrue(player.stateReadTwice.await(10, TimeUnit.SECONDS),
                    "Notification did not check the transport state");
            player.onValueReceived("TransportState", "PLAYING", AV_TRANSPORT);
            assertTrue(player.playingObserved.await(10, TimeUnit.SECONDS),
                    "Notification did not wait for playback to start");
            assertFalse(notification.isDone());
            assertFalse(player.queueCleared);

            player.onValueReceived("TransportState", "STOPPED", AV_TRANSPORT);
            notification.get(10, TimeUnit.SECONDS);

            assertTrue(player.queueCleared);
        } finally {
            notification.cancel(true);
            playbackThread.join(TimeUnit.SECONDS.toMillis(10));
            assertFalse(playbackThread.isAlive(), "Notification did not stop after interruption");
        }
    }

    @Test
    public void notificationTimesOutWhenTransportStateRemainsUnknown() {
        NotificationPlayer player = createPlayer(1);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            long start = System.nanoTime();
            player.playNotificationSoundURI(NOTIFICATION);

            assertTrue(System.nanoTime() - start >= TimeUnit.SECONDS.toNanos(1),
                    "Unknown transport state must not bypass the notification timeout");
            assertTrue(player.queueCleared);
        });
    }

    private NotificationPlayer createPlayer(int notificationTimeout) {
        Thing thing = ThingBuilder.create(ONE_THING_TYPE_UID, "player")
                .withConfiguration(new Configuration(Map.of("udn", UDN, "notificationTimeout", notificationTimeout)))
                .build();
        NotificationPlayer player = new NotificationPlayer(thing);
        this.handler = player;
        player.setCallback(callback);
        player.initialize();
        thing.setHandler(player);
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        return player;
    }

    private class NotificationPlayer extends ZonePlayerHandler {

        private final Thread testThread = Thread.currentThread();
        private final CountDownLatch stateReadTwice = new CountDownLatch(2);
        private final CountDownLatch playingObserved = new CountDownLatch(1);
        private volatile boolean queueCleared;

        private NotificationPlayer(Thing thing) {
            super(ZonePlayerHandlerNotificationTest.this.thingRegistry, thing,
                    ZonePlayerHandlerNotificationTest.this.upnpIOService, null,
                    ZonePlayerHandlerNotificationTest.this.stateDescriptionProvider);
        }

        @Override
        protected ZonePlayerHandler getHandlerByName(String remotePlayerName) {
            return this;
        }

        @Override
        public long getQueueSize() {
            return 0;
        }

        @Override
        public void playURI(Command command) {
        }

        @Override
        public void removeAllTracksFromQueue() {
            queueCleared = true;
        }

        @Override
        public @Nullable String getTransportState() {
            String state = super.getTransportState();
            if (!Thread.currentThread().equals(testThread)) {
                stateReadTwice.countDown();
                if ("PLAYING".equals(state)) {
                    playingObserved.countDown();
                }
            }
            return state;
        }
    }
}
