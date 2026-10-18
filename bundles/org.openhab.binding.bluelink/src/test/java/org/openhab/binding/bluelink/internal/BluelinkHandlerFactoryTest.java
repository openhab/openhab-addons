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
package org.openhab.binding.bluelink.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.*;

import java.time.ZoneId;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bluelink.internal.handler.BluelinkAccountHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Carlo Dischler - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class BluelinkHandlerFactoryTest {

    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) HttpClient firstClient;
    private @Mock @NonNullByDefault({}) HttpClient secondClient;
    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @NonNullByDefault({}) BluelinkHandlerFactory factory;

    @BeforeEach
    void setUp() {
        lenient().when(bridge.getThingTypeUID()).thenReturn(THING_TYPE_ACCOUNT);
        factory = new BluelinkHandlerFactory(httpClientFactory, () -> ZoneId.of("Europe/Berlin"), () -> Locale.GERMAN);
    }

    @Test
    void testEachAccountGetsItsOwnHttpClient() throws Exception {
        when(httpClientFactory.createHttpClient(BINDING_ID)).thenReturn(firstClient, secondClient);

        final ThingHandler first = factory.createHandler(bridge);
        final ThingHandler second = factory.createHandler(bridge);

        assertInstanceOf(BluelinkAccountHandler.class, first);
        assertInstanceOf(BluelinkAccountHandler.class, second);
        final InOrder inOrder = inOrder(firstClient);
        inOrder.verify(firstClient).setRequestBufferSize(HTTP_REQUEST_BUFFER_SIZE);
        inOrder.verify(firstClient).start();
        verify(secondClient).start();

        assertNotNull(first);
        factory.removeHandler(first);
        verify(firstClient).stop();
        verify(secondClient, never()).stop();
    }

    @Test
    void testStopsHttpClientWhenStartFails() throws Exception {
        when(httpClientFactory.createHttpClient(BINDING_ID)).thenReturn(firstClient);
        doThrow(new IllegalStateException("boom")).when(firstClient).start();

        assertThrows(IllegalStateException.class, () -> factory.createHandler(bridge));
        verify(firstClient).stop();
    }
}
