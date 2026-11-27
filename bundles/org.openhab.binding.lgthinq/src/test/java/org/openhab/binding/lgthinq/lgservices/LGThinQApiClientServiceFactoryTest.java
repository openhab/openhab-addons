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
package org.openhab.binding.lgthinq.lgservices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class LGThinQApiClientServiceFactoryTest {

    private HttpClientFactory mockHttpFactory() {
        HttpClientFactory factory = mock(HttpClientFactory.class);
        when(factory.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        return factory;
    }

    @ParameterizedTest
    @CsvSource({ "thinq1, LGThinQACApiV1ClientServiceImpl", "thinq2, LGThinQACApiV2ClientServiceImpl" })
    void newACApiClientServiceReturnsCorrectImpl(String platform, String expectedClass) {
        Object service = LGThinQApiClientServiceFactory.newACApiClientService(platform, mockHttpFactory());
        assertEquals(expectedClass, service.getClass().getSimpleName());
    }

    @ParameterizedTest
    @CsvSource({ "thinq1, LGThinQFridgeApiV1ClientServiceImpl", "thinq2, LGThinQFridgeApiV2ClientServiceImpl" })
    void newFridgeApiClientServiceReturnsCorrectImpl(String platform, String expectedClass) {
        Object service = LGThinQApiClientServiceFactory.newFridgeApiClientService(platform, mockHttpFactory());
        assertEquals(expectedClass, service.getClass().getSimpleName());
    }

    @ParameterizedTest
    @CsvSource({ "thinq1, LGThinQWMApiV1ClientServiceImpl", "thinq2, LGThinQWMApiV2ClientServiceImpl" })
    void newWMApiClientServiceReturnsCorrectImpl(String platform, String expectedClass) {
        Object service = LGThinQApiClientServiceFactory.newWMApiClientService(platform, mockHttpFactory());
        assertEquals(expectedClass, service.getClass().getSimpleName());
    }

    @ParameterizedTest
    @CsvSource({ "thinq1, LGThinQDishWasherApiV1ClientServiceImpl", "thinq2, LGThinQDishWasherApiV2ClientServiceImpl" })
    void newDishWasherApiClientServiceReturnsCorrectImpl(String platform, String expectedClass) {
        Object service = LGThinQApiClientServiceFactory.newDishWasherApiClientService(platform, mockHttpFactory());
        assertEquals(expectedClass, service.getClass().getSimpleName());
    }
}
