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
package org.openhab.binding.sonos.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonos.internal.handler.ZonePlayerHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.library.types.PercentType;

/**
 * Tests volume reporting by the Sonos audio sink.
 *
 * @author Leo Siepel - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class SonosAudioSinkTest {

    private @Mock @NonNullByDefault({}) ZonePlayerHandler handler;
    private @Mock @NonNullByDefault({}) AudioHTTPServer audioHTTPServer;

    private @NonNullByDefault({}) SonosAudioSink sink;

    @BeforeEach
    public void createSink() {
        sink = new SonosAudioSink(handler, audioHTTPServer, null);
    }

    @Test
    public void unknownVolumeIsNotReportedAsZero() {
        assertThrows(IOException.class, () -> sink.getVolume());
        verify(handler).getRefreshedVolume();
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "20", "100" })
    public void knownVolumeIsReported(String volume) throws IOException {
        when(handler.getVolume()).thenReturn(volume);

        assertEquals(new PercentType(volume), sink.getVolume());
        verify(handler, never()).getRefreshedVolume();
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "20", "100" })
    public void unknownVolumeIsRetrievedFromSpeaker(String volume) throws IOException {
        when(handler.getRefreshedVolume()).thenReturn(volume);

        assertEquals(new PercentType(volume), sink.getVolume());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "invalid", "-1", "101" })
    public void invalidRetrievedVolumeCannotBeRestored(String volume) {
        when(handler.getRefreshedVolume()).thenReturn(volume);

        assertThrows(IOException.class, () -> sink.getVolume());
    }

    @Test
    public void volumeCanBeRetrievedAfterAQueryFailure() throws IOException {
        when(handler.getRefreshedVolume()).thenReturn(null, "20");

        assertThrows(IOException.class, () -> sink.getVolume());
        assertEquals(new PercentType(20), sink.getVolume());
    }

    @Test
    public void missingVolumeEventsDoNotReuseAnEarlierQueryResult() throws IOException {
        when(handler.getRefreshedVolume()).thenReturn("20", "30");

        assertEquals(new PercentType(20), sink.getVolume());
        assertEquals(new PercentType(30), sink.getVolume());
    }

    @Test
    public void fixedOutputDoesNotQueryVolume() {
        when(handler.isOutputLevelFixed()).thenReturn(true);

        assertThrows(UnsupportedOperationException.class, () -> sink.getVolume());
        verify(handler, never()).getVolume();
        verify(handler, never()).getRefreshedVolume();
    }

    @Test
    public void fixedOutputDoesNotSetVolume() {
        when(handler.isOutputLevelFixed()).thenReturn(true);
        PercentType volume = new PercentType(20);

        assertThrows(UnsupportedOperationException.class, () -> sink.setVolume(volume));
        verify(handler, never()).setVolume(volume);
    }

    @Test
    public void volumeCanBeRetrievedAfterFixedOutputIsDisabled() throws IOException {
        when(handler.isOutputLevelFixed()).thenReturn(true, false);
        when(handler.getRefreshedVolume()).thenReturn("20");

        assertThrows(UnsupportedOperationException.class, () -> sink.getVolume());
        assertEquals(new PercentType(20), sink.getVolume());
    }

    @Test
    public void variableOutputSetsVolume() {
        PercentType volume = new PercentType(20);

        sink.setVolume(volume);

        verify(handler).setVolume(volume);
    }

    @Test
    public void volumeCanBeReadAfterItBecomesKnown() throws IOException {
        when(handler.getVolume()).thenReturn(null, "20");

        assertThrows(IOException.class, () -> sink.getVolume());
        assertEquals(new PercentType(20), sink.getVolume());
    }
}
