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
package org.openhab.binding.tapocontrol.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

class TapoControlHandlerFactoryTest {
    @Test
    void doesNotCreateHttpClientsUntilAHandlerNeedsOne() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);

        new TapoControlHandlerFactory(httpClientFactory, mock(TapoStateDescriptionProvider.class));

        verifyNoInteractions(httpClientFactory);
    }

    @Test
    void createsStandardHttpClientWhenBridgeHandlerIsCreated() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        Bridge bridge = BridgeBuilder.create(TapoThingConstants.BRIDGE_THING_TYPE, "testbridge").build();
        when(httpClientFactory.createHttpClient(anyString(), any(SslContextFactory.class))).thenReturn(httpClient);

        TapoControlHandlerFactory factory = new TapoControlHandlerFactory(httpClientFactory,
                mock(TapoStateDescriptionProvider.class));
        factory.createHandler(bridge);

        verify(httpClientFactory).createHttpClient(eq("tapocontrol"), any(SslContextFactory.class));
    }
}
