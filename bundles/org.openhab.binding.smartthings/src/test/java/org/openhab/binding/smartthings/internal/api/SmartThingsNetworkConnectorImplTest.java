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
package org.openhab.binding.smartthings.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * Tests for {@link SmartThingsNetworkConnectorImpl}.
 */
@NonNullByDefault
class SmartThingsNetworkConnectorImplTest {

    @Test
    void usesOpenhabCommonHttpClient() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = new HttpClient();
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);

        SmartThingsNetworkConnectorImpl connector = new SmartThingsNetworkConnectorImpl(httpClientFactory);

        assertSame(httpClient, connector.httpClient);
        verify(httpClientFactory).getCommonHttpClient();
    }

    @Test
    void requestDescriptionIncludesMethodAndUri() {
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getURI()).thenReturn(URI.create("https://api.smartthings.com/v1/apps"));

        assertEquals("POST https://api.smartthings.com/v1/apps",
                SmartThingsNetworkConnectorImpl.describeRequest(request));
    }
}
