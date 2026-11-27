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
package org.openhab.binding.myvaillant.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests the configuration checks of {@link AccountHandler}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class AccountHandlerTest {

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProvider;
    private @Mock @NonNullByDefault({}) Storage<String> requestCountStorage;

    @Test
    void countryThatDoesNotFitIntoTheLoginUrlIsAConfigurationError() {
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of("username", "user@example.com", "password",
                "secret", "brand", "vaillant", "country", "United Kingdom")));
        AccountHandler handler = new AccountHandler(bridge, httpClientFactory, timeZoneProvider, requestCountStorage);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(same(bridge), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR
                && "@text/offline.conf-error.invalid-country [\"United Kingdom\"]".equals(status.getDescription())));
        verifyNoInteractions(httpClientFactory);
    }

    @Test
    void requestCountContinuesOnTheSameDay() {
        LocalDate today = LocalDate.of(2026, 11, 20);
        assertEquals(123, AccountHandler.getRequestsToday("2026-11-20 123", today));
        assertEquals(0, AccountHandler.getRequestsToday("2026-11-19 123", today));
        assertEquals(0, AccountHandler.getRequestsToday(null, today));
        assertEquals(0, AccountHandler.getRequestsToday("2026-11-20", today));
        assertEquals(0, AccountHandler.getRequestsToday("yesterday 5", today));
        assertEquals(0, AccountHandler.getRequestsToday("2026-11-20 many", today));
        assertEquals(0, AccountHandler.getRequestsToday("2026-11-20 -3", today));
    }
}
