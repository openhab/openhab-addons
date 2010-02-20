/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.millheat.internal.config.MillheatAccountConfiguration;
import org.openhab.binding.millheat.internal.handler.MillheatAccountHandler;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.osgi.framework.BundleContext;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author Arne Seime - Initial contribution
 */
public class MillHeatAccountHandlerTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Mock
    private Bridge millheatAccountMock;

    private HttpClient httpClient;
    @Mock
    private Configuration configuration;

    @Mock
    private BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        httpClient = new HttpClient();
        httpClient.start();
        MillheatAccountHandler.authEndpoint = "http://localhost:" + wireMockRule.port() + "/zc-account/v1/";
        MillheatAccountHandler.serviceEndpoint = "http://localhost:" + wireMockRule.port() + "/millService/v1/";
    }

    @After
    public void shutdown() throws Exception {
        httpClient.stop();
    }

    @Test
    public void testUpdateModel() throws InterruptedException, IOException, MillheatCommunicationException {
        final String getHomesResponse = IOUtils.toString(getClass().getResourceAsStream("/select_home_list_ok.json"));
        final String getRoomsByHomeResponse = IOUtils
                .toString(getClass().getResourceAsStream("/get_rooms_by_home_ok.json"));
        final String getDeviceByRoomResponse = IOUtils
                .toString(getClass().getResourceAsStream("/get_device_by_room_ok.json"));
        final String getIndependentDevicesResponse = IOUtils
                .toString(getClass().getResourceAsStream("/get_independent_devices_ok.json"));

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
        Assert.assertEquals(1, model.getHomes().size());

        verify(postRequestedFor(urlMatching("/millService/v1/selectHomeList")));
        verify(postRequestedFor(urlMatching("/millService/v1/selectRoombyHome")));
        verify(postRequestedFor(urlMatching("/millService/v1/selectDevicebyRoom")));
        verify(postRequestedFor(urlMatching("/millService/v1/getIndependentDevices")));
    }
}
