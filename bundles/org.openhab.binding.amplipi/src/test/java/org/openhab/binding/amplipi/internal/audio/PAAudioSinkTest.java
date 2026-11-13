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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
        AmpliPiHandler bridgeHandler = mock(AmpliPiHandler.class);
        PAAudioSink sink = new PAAudioSink();
        sink.setThingHandler(bridgeHandler);

        sink.processSynchronously(urlAudioStream());

        verify(bridgeHandler).playPA(AUDIO_URL, null, null);
    }

    @Test
    public void testZoneSinkAnnouncesOnOwnZoneOnly() throws Exception {
        AmpliPiHandler bridgeHandler = mock(AmpliPiHandler.class);
        AmpliPiZoneHandler zoneHandler = mock(AmpliPiZoneHandler.class);
        Thing thing = mock(Thing.class);
        Configuration configuration = new Configuration();
        configuration.put(AmpliPiBindingConstants.CFG_PARAM_ID, 1);
        when(zoneHandler.getBridgeHandler()).thenReturn(bridgeHandler);
        when(zoneHandler.getThing()).thenReturn(thing);
        when(thing.getConfiguration()).thenReturn(configuration);
        PAAudioSink sink = new PAAudioSink();
        sink.setThingHandler(zoneHandler);

        sink.processSynchronously(urlAudioStream());

        verify(bridgeHandler).playPA(AUDIO_URL, null, List.of(1));
    }

    private URLAudioStream urlAudioStream() {
        URLAudioStream urlAudioStream = mock(URLAudioStream.class);
        when(urlAudioStream.getURL()).thenReturn(AUDIO_URL);
        return urlAudioStream;
    }
}
