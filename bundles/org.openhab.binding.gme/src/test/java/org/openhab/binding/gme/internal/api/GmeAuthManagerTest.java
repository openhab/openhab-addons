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
package org.openhab.binding.gme.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gme.internal.model.GmeGranularity;
import org.openhab.binding.gme.internal.model.GmePriceEntry;

@NonNullByDefault
@SuppressWarnings("null")
class GmeAuthManagerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 11);

    @Test
    void retriesOnceAfterAuthenticationError() throws Exception {
        GmeApiClient apiClient = mock(GmeApiClient.class);

        GmePriceEntry entry = new GmePriceEntry(DATE, 1, "MGP", "PUN", new java.math.BigDecimal("200.000000"), 0,
                GmeGranularity.PT60, null);

        when(apiClient.authenticate("user", "password")).thenReturn("token-1", "token-2");
        when(apiClient.requestPun(DATE, "token-1")).thenThrow(new GmeApiException("Unauthorized", 401));
        when(apiClient.requestPun(DATE, "token-2")).thenReturn(List.of(entry));

        GmeAuthManager authManager = new GmeAuthManager(apiClient, "user", "password");

        List<GmePriceEntry> result = authManager.requestPun(DATE);

        assertEquals(1, result.size());
        assertEquals(entry, result.getFirst());

        verify(apiClient, times(2)).authenticate("user", "password");
        verify(apiClient, times(1)).requestPun(DATE, "token-1");
        verify(apiClient, times(1)).requestPun(DATE, "token-2");
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    void retriesMarketPricesOnceAfterAuthenticationError() throws Exception {
        GmeApiClient apiClient = mock(GmeApiClient.class);

        GmePriceEntry entry = new GmePriceEntry(DATE, 1, "MGP", "PUN", new java.math.BigDecimal("200.000000"), 1,
                GmeGranularity.PT15, null);

        when(apiClient.authenticate("user", "password")).thenReturn("token-1", "token-2");
        when(apiClient.requestMarketPrices(DATE, "token-1", GmeGranularity.PT15))
                .thenThrow(new GmeApiException("Unauthorized", 401));
        when(apiClient.requestMarketPrices(DATE, "token-2", GmeGranularity.PT15)).thenReturn(List.of(entry));

        GmeAuthManager authManager = new GmeAuthManager(apiClient, "user", "password");

        List<GmePriceEntry> result = authManager.requestMarketPrices(DATE, GmeGranularity.PT15);

        assertEquals(List.of(entry), result);
        verify(apiClient, times(2)).authenticate("user", "password");
        verify(apiClient).requestMarketPrices(DATE, "token-1", GmeGranularity.PT15);
        verify(apiClient).requestMarketPrices(DATE, "token-2", GmeGranularity.PT15);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    void doesNotRetryMarketPricesMoreThanOnceAfterRepeatedAuthenticationError() throws Exception {
        GmeApiClient apiClient = mock(GmeApiClient.class);

        when(apiClient.authenticate("user", "password")).thenReturn("token-1", "token-2");
        when(apiClient.requestMarketPrices(DATE, "token-1", GmeGranularity.PT30))
                .thenThrow(new GmeApiException("Unauthorized", 401));
        when(apiClient.requestMarketPrices(DATE, "token-2", GmeGranularity.PT30))
                .thenThrow(new GmeApiException("Unauthorized", 401));

        GmeAuthManager authManager = new GmeAuthManager(apiClient, "user", "password");

        GmeApiException exception = assertThrows(GmeApiException.class,
                () -> authManager.requestMarketPrices(DATE, GmeGranularity.PT30));

        assertEquals(401, exception.getStatusCode());
        verify(apiClient, times(2)).authenticate("user", "password");
        verify(apiClient).requestMarketPrices(DATE, "token-1", GmeGranularity.PT30);
        verify(apiClient).requestMarketPrices(DATE, "token-2", GmeGranularity.PT30);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    void doesNotRetryMoreThanOnceAfterRepeatedAuthenticationError() throws Exception {
        GmeApiClient apiClient = mock(GmeApiClient.class);

        when(apiClient.authenticate("user", "password")).thenReturn("token-1", "token-2");
        when(apiClient.requestPun(DATE, "token-1")).thenThrow(new GmeApiException("Unauthorized", 401));
        when(apiClient.requestPun(DATE, "token-2")).thenThrow(new GmeApiException("Unauthorized", 401));

        GmeAuthManager authManager = new GmeAuthManager(apiClient, "user", "password");

        GmeApiException exception = assertThrows(GmeApiException.class, () -> authManager.requestPun(DATE));

        assertEquals(401, exception.getStatusCode());

        verify(apiClient, times(2)).authenticate("user", "password");
        verify(apiClient, times(1)).requestPun(DATE, "token-1");
        verify(apiClient, times(1)).requestPun(DATE, "token-2");
        verifyNoMoreInteractions(apiClient);
    }
}
