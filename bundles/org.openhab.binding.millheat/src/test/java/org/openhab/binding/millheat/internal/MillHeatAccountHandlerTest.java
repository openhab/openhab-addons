/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.millheat.internal.config.MillheatAccountConfiguration;
import org.openhab.binding.millheat.internal.handler.MillheatAccountHandler;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * @author Arne Seime - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MillHeatAccountHandlerTest {

    private WireMockServer wireMockServer;

    private HttpClient httpClient;

    private @Mock BundleContext bundleContext;
    private @Mock Configuration configuration;
    private @Mock Bridge millheatAccountMock;

    @BeforeEach
    public void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        int port = wireMockServer.port();
        WireMock.configureFor("localhost", port);

        httpClient = new HttpClient();
        httpClient.start();

        MillheatAccountHandler.authEndpoint = "http://localhost:" + port + "/zc-account/v1/";
        MillheatAccountHandler.serviceEndpoint = "http://localhost:" + port + "/millService/v1/";
    }

    @AfterEach
    public void shutdown() throws Exception {
        httpClient.stop();
        wireMockServer.stop();
        wireMockServer.resetAll();
    }

    @Test
    public void testUpdateModel() throws InterruptedException, IOException, MillheatCommunicationException {
        final String getHomesResponse = new String(
                getClass().getResourceAsStream("/select_home_list_ok.json").readAllBytes(), StandardCharsets.UTF_8);
        final String getRoomsByHomeResponse = new String(
                getClass().getResourceAsStream("/get_rooms_by_home_ok.json").readAllBytes(), StandardCharsets.UTF_8);
        final String getDeviceByRoomResponse = new String(
                getClass().getResourceAsStream("/get_device_by_room_ok.json").readAllBytes(), StandardCharsets.UTF_8);
        final String getIndependentDevicesResponse = new String(
                getClass().getResourceAsStream("/get_independent_devices_ok.json").readAllBytes(),
                StandardCharsets.UTF_8);

        stubFor(post(urlEqualTo("/millService/v1/selectHomeList"))
                .willReturn(aResponse().withStatus(200).withBody(getHomesResponse)));
        stubFor(post(urlEqualTo("/millService/v1/selectRoombyHome"))
                .willReturn(aResponse().withStatus(200).withBody(getRoomsByHomeResponse)));
        stubFor(post(urlEqualTo("/millService/v1/selectDevicebyRoom"))
                .willReturn(aResponse().withStatus(200).withBody(getDeviceByRoomResponse)));
        stubFor(post(urlEqualTo("/millService/v1/getIndependentDevices"))
                .willReturn(aResponse().withStatus(200).withBody(getIndependentDevicesResponse)));

        when(millheatAccountMock.getConfiguration()).thenReturn(configuration);
        when(millheatAccountMock.getUID()).thenReturn(new ThingUID("millheat:account:thinguid"));

        final MillheatAccountConfiguration accountConfig = new MillheatAccountConfiguration();
        accountConfig.username = "username";
        accountConfig.password = "password";
        when(configuration.as(eq(MillheatAccountConfiguration.class))).thenReturn(accountConfig);

        final MillheatAccountHandler subject = new MillheatAccountHandler(millheatAccountMock, httpClient,
                bundleContext);
        MillheatModel model = subject.refreshModel();
        assertEquals(1, model.getHomes().size());

        verify(postRequestedFor(urlMatching("/millService/v1/selectHomeList")));
        verify(postRequestedFor(urlMatching("/millService/v1/selectRoombyHome")));
        verify(postRequestedFor(urlMatching("/millService/v1/selectDevicebyRoom")));
        verify(postRequestedFor(urlMatching("/millService/v1/getIndependentDevices")));
    }
}
