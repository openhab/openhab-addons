/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;

/**
 * Tests cases for {@link BoschHttpClient}.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
class BoschHttpClientTest {

    @Nullable
    private BoschHttpClient httpClient;

    @BeforeAll
    static void beforeAll() {
        BoschSslUtilTest.prepareTempFolderForKeyStore();
    }

    @BeforeEach
    void beforeEach() throws PairingFailedException {
        SslContextFactory sslFactory = new BoschSslUtil("127.0.0.1").getSslContextFactory();
        httpClient = new BoschHttpClient("127.0.0.1", "dummy", sslFactory);
        assertNotNull(httpClient);
    }

    @Test
    void getPublicInformationUrl() {
        assertEquals("https://127.0.0.1:8446/smarthome/public/information", httpClient.getPublicInformationUrl());
    }

    @Test
    void getPairingUrl() {
        assertEquals("https://127.0.0.1:8443/smarthome/clients", httpClient.getPairingUrl());
    }

    @Test
    void getBoschShcUrl() {
        assertEquals("https://127.0.0.1:8444/testEndpoint", httpClient.getBoschShcUrl("testEndpoint"));
    }

    @Test
    void getBoschSmartHomeUrl() {
        assertEquals("https://127.0.0.1:8444/smarthome/endpointForTest",
                httpClient.getBoschSmartHomeUrl("endpointForTest"));
    }

    @Test
    void getServiceUrl() {
        assertEquals("https://127.0.0.1:8444/smarthome/devices/testDevice/services/testService/state",
                httpClient.getServiceUrl("testService", "testDevice"));
    }

    @Test
    void isAccessPossible() throws InterruptedException {
        assertFalse(httpClient.isAccessPossible());
    }

    @Test
    void isOnline() throws InterruptedException {
        assertFalse(httpClient.isOnline());
    }

    @Test
    void doPairing() throws InterruptedException {
        assertFalse(httpClient.doPairing());
    }

    @Test
    void createRequest() {
        Request request = httpClient.createRequest("https://127.0.0.1", HttpMethod.GET);
        assertNotNull(request);
    }

    @Test
    void createRequestWithObject() {
        Request request = httpClient.createRequest("https://127.0.0.1", HttpMethod.GET, "someData");
        assertNotNull(request);
    }

    @Test
    void sendRequest() {
        Request request = httpClient.createRequest("https://127.0.0.1", HttpMethod.GET);
        // Null pointer exception is expected, because localhost will not answer request
        assertThrows(NullPointerException.class, () -> {
            httpClient.sendRequest(request, SubscribeResult.class, SubscribeResult::isValid, null);
        });
    }
}
