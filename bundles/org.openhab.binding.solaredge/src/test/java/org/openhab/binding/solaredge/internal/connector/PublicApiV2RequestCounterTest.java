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
package org.openhab.binding.solaredge.internal.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.storage.Storage;

/**
 * Tests persistence and expiry of the local Monitoring API V2 request counter.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class PublicApiV2RequestCounterTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-14T10:00:00Z"), ZoneOffset.UTC);

    @Test
    public void persistsRecordedRequests() {
        Map<String, String> values = new HashMap<>();
        Storage<String> storage = storage(values);

        PublicApiV2RequestCounter counter = new PublicApiV2RequestCounter(storage, CLOCK);

        assertEquals(1, counter.recordRequest());
        assertEquals(1, new PublicApiV2RequestCounter(storage, CLOCK).getRequestCount());
    }

    @Test
    public void removesBucketsOutsideRollingWindow() {
        long currentHour = CLOCK.instant().getEpochSecond() / 3600;
        Map<String, String> values = new HashMap<>();
        values.put("publicApiV2RequestCounts",
                (currentHour - 720) + "=5," + (currentHour - 719) + "=2," + currentHour + "=3");

        PublicApiV2RequestCounter counter = new PublicApiV2RequestCounter(storage(values), CLOCK);

        assertEquals(5, counter.getRequestCount());
    }

    @SuppressWarnings("unchecked")
    private Storage<String> storage(Map<String, String> values) {
        Storage<String> storage = mock(Storage.class);
        when(storage.get(anyString())).thenAnswer(invocation -> values.get(invocation.getArgument(0)));
        doAnswer(invocation -> values.put(invocation.getArgument(0), invocation.getArgument(1))).when(storage)
                .put(anyString(), anyString());
        return storage;
    }
}
