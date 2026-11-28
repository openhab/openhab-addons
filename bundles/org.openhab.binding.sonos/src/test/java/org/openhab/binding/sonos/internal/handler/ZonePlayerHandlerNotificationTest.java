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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.sonos.internal.SonosBindingConstants.ONE_THING_TYPE_UID;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonos.internal.SonosStateDescriptionOptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
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

    @Test
    public void notificationWaitsForPlaybackToStopBeforeClearingQueue() {
        NotificationPlayer player = createPlayer(30);
        // Two null reads catch cleanup before playback starts. The second PLAYING read catches
        // cleanup while playback is still active.
        player.reportTransportStates(null, null, "PLAYING", "PLAYING", "STOPPED");

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> player.playNotificationSoundURI(NOTIFICATION));

        assertEquals("STOPPED", player.transportStateWhenQueueCleared);
    }

    @Test
    public void notificationTimesOutWhenTransportStateRemainsUnknown() {
        int notificationTimeoutSeconds = 1;
        NotificationPlayer player = createPlayer(notificationTimeoutSeconds);

        long elapsedNanos = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            long start = System.nanoTime();
            player.playNotificationSoundURI(NOTIFICATION);
            return System.nanoTime() - start;
        });

        assertTrue(elapsedNanos >= TimeUnit.SECONDS.toNanos(notificationTimeoutSeconds),
                "Unknown transport state must not bypass the notification timeout");
        assertTrue(player.queueCleared);
    }

    private NotificationPlayer createPlayer(int notificationTimeout) {
        Thing thing = ThingBuilder.create(ONE_THING_TYPE_UID, "player")
                .withConfiguration(new Configuration(Map.of("udn", UDN, "notificationTimeout", notificationTimeout)))
                .build();
        NotificationPlayer player = new NotificationPlayer(thing);
        this.handler = player;
        player.setCallback(callback);
        player.initialize();
        return player;
    }

    private class NotificationPlayer extends ZonePlayerHandler {

        private Iterator<@Nullable String> transportStates = Collections.emptyIterator();
        private @Nullable String transportState;
        private @Nullable String transportStateWhenQueueCleared;
        private boolean queueCleared;

        private NotificationPlayer(Thing thing) {
            super(ZonePlayerHandlerNotificationTest.this.thingRegistry, thing,
                    ZonePlayerHandlerNotificationTest.this.upnpIOService, null,
                    ZonePlayerHandlerNotificationTest.this.stateDescriptionProvider);
        }

        private void reportTransportStates(@Nullable String... states) {
            transportStates = Arrays.asList(states).iterator();
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
            transportStateWhenQueueCleared = transportState;
        }

        @Override
        public @Nullable String getTransportState() {
            if (transportStates.hasNext()) {
                transportState = transportStates.next();
            }
            return transportState;
        }
    }
}
