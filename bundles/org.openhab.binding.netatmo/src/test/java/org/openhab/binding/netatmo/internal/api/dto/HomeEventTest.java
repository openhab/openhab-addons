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
package org.openhab.binding.netatmo.internal.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * @author Gaël L'hopital - Initial contribution
 */
public class HomeEventTest {
    private static final long PAST_EPOCH_SECONDS = 946684800L; // 2000-01-01T00:00:00Z
    private static final long FUTURE_EPOCH_SECONDS = 4102444800L; // 2100-01-01T00:00:00Z

    private static NADeserializer gson;

    @BeforeAll
    public static void init() {
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        gson = new NADeserializer(timeZoneProvider);
    }

    @Test
    public void testSnapshotUrlDeserializationWithFutureExpiration() throws NetatmoException {
        String event = """
                {\
                  "snapshot": {\
                    "url": "https://example.netatmo/snapshot.jpg",\
                    "expires_at": %d\
                  }\
                }\
                """.formatted(FUTURE_EPOCH_SECONDS);

        HomeEvent object = gson.deserialize(HomeEvent.class, event);

        assertEquals("https://example.netatmo/snapshot.jpg", object.getSnapshotUrl());
    }

    @Test
    public void testVignetteUrlDeserializationWithPastExpiration() throws NetatmoException {
        String event = """
                {\
                  "vignette": {\
                    "url": "https://example.netatmo/vignette.jpg",\
                    "expires_at": %d\
                  }\
                }\
                """.formatted(PAST_EPOCH_SECONDS);

        HomeEvent object = gson.deserialize(HomeEvent.class, event);

        assertNull(object.getVignetteUrl());
    }
}
