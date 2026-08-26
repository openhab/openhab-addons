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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Verifies HTTP client creation of the handler factory.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class TapoControlHandlerFactoryTest {
    private static final String LEGAL_CONSUMER_NAME_PATTERN = "[a-zA-Z0-9_-]+";

    @Mock
    private HttpClientFactory httpClientFactory;

    @Test
    void createdHttpClientsUseLegalConsumerNames() {
        // core's WebClientFactoryImpl rejects consumer names outside [a-zA-Z0-9_-]
        List<String> consumerNames = new ArrayList<>();
        when(httpClientFactory.createHttpClient(anyString(), any(SslContextFactory.class))).thenAnswer(invocation -> {
            consumerNames.add(invocation.getArgument(0));
            return mock(HttpClient.class);
        });

        new TapoControlHandlerFactory(httpClientFactory, mock(TapoStateDescriptionProvider.class));

        assertEquals(List.of("tapocontrol", "tapocontrol-camera"), consumerNames);
        consumerNames.forEach(name -> assertTrue(name.matches(LEGAL_CONSUMER_NAME_PATTERN)));
    }
}
