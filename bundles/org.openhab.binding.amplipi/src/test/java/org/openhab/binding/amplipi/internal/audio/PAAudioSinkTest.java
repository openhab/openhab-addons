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
package org.openhab.binding.amplipi.internal.audio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amplipi.internal.AmpliPiBindingConstants;
import org.openhab.binding.amplipi.internal.AmpliPiHandler;
import org.openhab.binding.amplipi.internal.AmpliPiZoneHandler;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;

/**
 * Tests for the {@link PAAudioSink} target zone handling, i.e. that announcements on the bridge play on all zones
 * while a zone sink is restricted to its own zone.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class PAAudioSinkTest {

    private static final String AUDIO_URL = "http://example.com/announcement.mp3";

    @Test
    public void testBridgeSinkAnnouncesOnAllZones() throws Exception {
        AmpliPiHandler bridgeHandler = createBridgeHandler();
        PAAudioSink sink = new PAAudioSink();
        sink.setThingHandler(bridgeHandler);

        sink.processSynchronously(urlAudioStream());

        verify(bridgeHandler).playPA(AUDIO_URL, null, null);
    }

    @Test
    public void testZoneSinkAnnouncesOnOwnZoneOnly() throws Exception {
        AmpliPiHandler bridgeHandler = createBridgeHandler();
        PAAudioSink sink = new PAAudioSink();
        sink.setThingHandler(createZoneHandler(bridgeHandler, 1));

        sink.processSynchronously(urlAudioStream());

        verify(bridgeHandler).playPA(AUDIO_URL, null, List.of(1));
    }

    @Test
    public void testZoneSinkRejectsDisabledZone() throws Exception {
        AmpliPiHandler bridgeHandler = createBridgeHandler();
        when(bridgeHandler.isZoneDisabled(1)).thenReturn(true);
        URLAudioStream stream = urlAudioStream();
        PAAudioSink sink = new PAAudioSink();
        sink.setThingHandler(createZoneHandler(bridgeHandler, 1));

        sink.processSynchronously(stream);

        verify(bridgeHandler, never()).playPA(eq(AUDIO_URL), isNull(), isNull());
        verify(stream).close();
    }

    @Test
    public void testConcurrentAnnouncementsAreSerialized() throws Exception {
        AmpliPiHandler bridgeHandler = createBridgeHandler();
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicInteger entered = new AtomicInteger();
        doAnswer(invocation -> {
            if (entered.incrementAndGet() == 1) {
                firstEntered.countDown();
                // Block the first announcement until released so the second one must wait for the lock
                if (!releaseFirst.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("First announcement was not released in time");
                }
            } else {
                secondEntered.countDown();
            }
            return null;
        }).when(bridgeHandler).playPA(eq(AUDIO_URL), isNull(), isNull());

        PAAudioSink sink1 = new PAAudioSink();
        sink1.setThingHandler(bridgeHandler);
        PAAudioSink sink2 = new PAAudioSink();
        sink2.setThingHandler(bridgeHandler);
        CountDownLatch secondStarted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> first = executor.submit(() -> {
            try {
                sink1.processSynchronously(urlAudioStream());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            assertTrue(firstEntered.await(10, TimeUnit.SECONDS), "First announcement did not enter");
            Future<?> second = executor.submit(() -> {
                secondStarted.countDown();
                try {
                    sink2.processSynchronously(urlAudioStream());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            assertTrue(secondStarted.await(10, TimeUnit.SECONDS), "Second announcement did not start");
            // The second thread is now running, so a missing lock would let it enter straight away.
            // A bounded wait proves it is blocked on the lock instead.
            assertFalse(secondEntered.await(500, TimeUnit.MILLISECONDS),
                    "Second announcement entered while the first one was still running");
            releaseFirst.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
        verify(bridgeHandler, times(2)).playPA(AUDIO_URL, null, null);
    }

    private AmpliPiHandler createBridgeHandler() {
        AmpliPiHandler bridgeHandler = mock(AmpliPiHandler.class);
        when(bridgeHandler.getAnnouncementLock()).thenReturn(new Object());
        return bridgeHandler;
    }

    private AmpliPiZoneHandler createZoneHandler(AmpliPiHandler bridgeHandler, int zoneId) {
        AmpliPiZoneHandler zoneHandler = mock(AmpliPiZoneHandler.class);
        Thing thing = mock(Thing.class);
        Configuration configuration = new Configuration();
        configuration.put(AmpliPiBindingConstants.CFG_PARAM_ID, zoneId);
        when(zoneHandler.getBridgeHandler()).thenReturn(bridgeHandler);
        when(zoneHandler.getThing()).thenReturn(thing);
        when(thing.getConfiguration()).thenReturn(configuration);
        return zoneHandler;
    }

    private URLAudioStream urlAudioStream() {
        URLAudioStream urlAudioStream = mock(URLAudioStream.class);
        when(urlAudioStream.getURL()).thenReturn(AUDIO_URL);
        return urlAudioStream;
    }
}
