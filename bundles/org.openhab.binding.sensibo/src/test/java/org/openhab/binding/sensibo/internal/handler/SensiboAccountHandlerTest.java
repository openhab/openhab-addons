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
package org.openhab.binding.sensibo.internal.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sensibo.internal.config.SensiboAccountConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Arne Seime - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class SensiboAccountHandlerTest {
    private WireMockServer wireMockServer;

    private HttpClient httpClient;

    private @Mock Configuration configuration;
    private @Mock Bridge sensiboAccountMock;

    @BeforeEach
    public void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        int port = wireMockServer.port();
        WireMock.configureFor("localhost", port);

        httpClient = new HttpClient();
        httpClient.start();

        SensiboAccountHandler.API_ENDPOINT = "http://localhost:" + port + "/api"; // https://home.sensibo.com/api/v2
    }

    @AfterEach
    public void shutdown() throws Exception {
        httpClient.stop();
    }

    @Test
    public void testInitialize1() throws IOException {
        testInitialize("/get_pods_response.json", 1);
    }

    @Test
    public void testInitializeMarco() throws IOException {
        testInitialize("/get_pods_response.json", 1);
    }

    @Test
    public void testInitialize_Issue8018() throws IOException {
        testInitialize("/get_pods_response_partial_setup.json", 4);
    }

    private void testInitialize(String podsResponse, int numExpectedPods) throws IOException {
        // Setup account
        final SensiboAccountConfiguration accountConfig = new SensiboAccountConfiguration();
        accountConfig.apiKey = "APIKEY";
        when(configuration.as(eq(SensiboAccountConfiguration.class))).thenReturn(accountConfig);

        // Setup initial response
        final String getPodsResponse = new String(getClass().getResourceAsStream(podsResponse).readAllBytes(),
                StandardCharsets.UTF_8);
        stubFor(get(urlEqualTo("/api/v2/users/me/pods?fields=*&apiKey=APIKEY"))
                .withHeader("Accept-Encoding", equalTo("gzip"))
                .willReturn(aResponse().withStatus(200).withBody(getPodsResponse)));

        when(sensiboAccountMock.getConfiguration()).thenReturn(configuration);
        when(sensiboAccountMock.getUID()).thenReturn(new ThingUID("sensibo:account:thinguid"));

        final SensiboAccountHandler handler = new SensiboAccountHandler(sensiboAccountMock, httpClient);
        handler.initialize();

        // Async, poll for status
        await().until(() -> handler.getModel().getPods().size() == numExpectedPods);
    }
}
