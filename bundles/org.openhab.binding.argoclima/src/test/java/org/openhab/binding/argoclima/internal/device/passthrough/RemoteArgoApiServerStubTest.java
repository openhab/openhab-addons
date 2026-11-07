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
package org.openhab.binding.argoclima.internal.device.passthrough;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.argoclima.internal.ArgoClimaTranslationProvider;
import org.openhab.binding.argoclima.internal.configuration.ArgoClimaConfigurationLocal.DeviceSidePasswordDisplayMode;

/**
 * Tests the device-side HTTP handling of {@link RemoteArgoApiServerStub}.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
class RemoteArgoApiServerStubTest {

    private int port;
    private RemoteArgoApiServerStub stub = new RemoteArgoApiServerStub(Set.of(), 0, "test", Optional.empty(),
            Optional.empty(), DeviceSidePasswordDisplayMode.NEVER, mock(ArgoClimaTranslationProvider.class));

    @BeforeEach
    void setUp() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        stub = new RemoteArgoApiServerStub(Set.of(InetAddress.getLoopbackAddress()), port, "test", Optional.empty(),
                Optional.empty(), DeviceSidePasswordDisplayMode.NEVER, mock(ArgoClimaTranslationProvider.class));
        stub.start();
    }

    @AfterEach
    void tearDown() {
        stub.shutdown();
    }

    private HttpResponse<String> get(String pathQuery) throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest
                .newBuilder(URI
                        .create("http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":" + port + pathQuery))
                .timeout(Duration.ofSeconds(10)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void answersNtpRequest() throws Exception {
        HttpResponse<String> response = get("/UI/UI.php?CM=UI_NTP");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("NTP "), response.body());
    }

    @Test
    void answersUnknownRequestWithCannedResponse() throws Exception {
        HttpResponse<String> response = get("/UI/UI.php?CM=SOMETHING_ELSE");

        assertEquals(200, response.statusCode());
    }
}
