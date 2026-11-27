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
package org.openhab.binding.amplipi.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.amplipi.internal.audio.PAAudioSink;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;

/**
 * Tests for the {@link AmpliPiZoneHandler}, in particular the opt-in registration of the zone audio sink.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AmpliPiZoneHandlerTest {

    @Test
    public void testAudioSinkRegisteredWhenEnabled() {
        AmpliPiZoneHandler handler = createZoneHandler(Boolean.TRUE);

        assertTrue(handler.getServices().contains(PAAudioSink.class));
    }

    @Test
    public void testAudioSinkNotRegisteredWhenDisabled() {
        AmpliPiZoneHandler handler = createZoneHandler(Boolean.FALSE);

        assertEquals(0, handler.getServices().size());
    }

    private AmpliPiZoneHandler createZoneHandler(Boolean audioSink) {
        Thing thing = mock(Thing.class);
        Configuration configuration = new Configuration();
        configuration.put(AmpliPiBindingConstants.CFG_PARAM_AUDIO_SINK, audioSink);
        when(thing.getConfiguration()).thenReturn(configuration);
        return new AmpliPiZoneHandler(thing, mock(HttpClient.class));
    }
}
