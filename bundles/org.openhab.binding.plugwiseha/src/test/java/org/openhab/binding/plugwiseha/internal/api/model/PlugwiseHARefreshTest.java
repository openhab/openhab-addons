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
package org.openhab.binding.plugwiseha.internal.api.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Tests periodic full synchronization while incremental polling is active.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class PlugwiseHARefreshTest {

    private final HttpClient client = mock(HttpClient.class);
    private final Request request = mock(Request.class, RETURNS_SELF);
    private final ZonedDateTime start = ZonedDateTime.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        when(client.newRequest(anyString())).thenReturn(request);
    }

    @Test
    void overdueFullRefreshTakesPriorityOverIncrementalRefresh() throws Exception {
        ZonedDateTime laterTime = start.plusMinutes(16);
        ContentResponse initial = response(start);
        ContentResponse later = response(laterTime);
        when(request.send()).thenReturn(initial, later);
        PlugwiseHAController controller = new PlugwiseHAController(client, "adam", 80, "smile", "abcdefgh", 15);

        try (MockedStatic<ZonedDateTime> clock = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            clock.when(ZonedDateTime::now).thenReturn(start);
            controller.refresh();

            clock.when(ZonedDateTime::now).thenReturn(laterTime);
            controller.refresh();
        }

        List<String> requests = requestedUris(2);
        assertEquals(requests.getFirst(), requests.getLast());
        assertFalse(requests.getLast().contains("modified_date:ge:"));
    }

    @Test
    void incrementalRefreshDoesNotPostponeFullSynchronization() throws Exception {
        ZonedDateTime incrementalTime = start.plusMinutes(1);
        ZonedDateTime fullTime = start.plusMinutes(16);
        ContentResponse initial = response(start);
        ContentResponse incremental = response(incrementalTime);
        ContentResponse full = response(fullTime);
        when(request.send()).thenReturn(initial, incremental, full);
        PlugwiseHAController controller = new PlugwiseHAController(client, "adam", 80, "smile", "abcdefgh", 15);

        try (MockedStatic<ZonedDateTime> clock = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            clock.when(ZonedDateTime::now).thenReturn(start);
            controller.refresh();
            clock.when(ZonedDateTime::now).thenReturn(incrementalTime);
            controller.refresh();
            clock.when(ZonedDateTime::now).thenReturn(fullTime);
            controller.refresh();
        }

        List<String> requests = requestedUris(3);
        assertTrue(requests.get(1).contains("modified_date:ge:" + start.toEpochSecond()));
        assertEquals(requests.getFirst(), requests.getLast());
    }

    @Test
    void fullRefreshStillRunsAfterARecentIncrementalRefresh() throws Exception {
        ZonedDateTime beforeDeadline = start.plusMinutes(15).minusSeconds(1);
        ZonedDateTime afterDeadline = start.plusMinutes(15).plusSeconds(1);
        ContentResponse initial = response(start);
        ContentResponse incremental = response(beforeDeadline);
        ContentResponse full = response(afterDeadline);
        when(request.send()).thenReturn(initial, incremental, full);
        PlugwiseHAController controller = new PlugwiseHAController(client, "adam", 80, "smile", "abcdefgh", 15);

        try (MockedStatic<ZonedDateTime> clock = mockStatic(ZonedDateTime.class, CALLS_REAL_METHODS)) {
            clock.when(ZonedDateTime::now).thenReturn(start);
            controller.refresh();
            clock.when(ZonedDateTime::now).thenReturn(beforeDeadline);
            controller.refresh();
            clock.when(ZonedDateTime::now).thenReturn(afterDeadline);
            controller.refresh();
        }

        List<String> requests = requestedUris(3);
        assertTrue(requests.get(1).contains("modified_date:ge:"));
        assertEquals(requests.getFirst(), requests.getLast());
    }

    private ContentResponse response(ZonedDateTime date) {
        ContentResponse response = mock(ContentResponse.class);
        HttpFields.Mutable headers = HttpFields.build();
        headers.put(HttpHeader.DATE, date.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        when(response.getStatus()).thenReturn(200);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getContentAsString()).thenReturn("""
                <domain_objects><gateway id="gateway"><name>Adam</name></gateway></domain_objects>
                """);
        return response;
    }

    private List<String> requestedUris(int count) {
        ArgumentCaptor<String> uris = ArgumentCaptor.forClass(String.class);
        verify(client, times(count)).newRequest(uris.capture());
        return uris.getAllValues();
    }
}
