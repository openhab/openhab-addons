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
package org.openhab.binding.sensibo.internal.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.sensibo.internal.config.SensiboAccountConfiguration;
import org.openhab.binding.sensibo.internal.model.SensiboSky;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * @author Arne Seime - Initial contribution
 */
public class SensiboAccountHandlerTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Mock
    private Bridge sensiboAccountMock;

    private HttpClient httpClient;
    @Mock
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        httpClient = new HttpClient();
        httpClient.start();
        SensiboAccountHandler.API_ENDPOINT = "http://localhost:" + wireMockRule.port() + "/api"; // https://home.sensibo.com/api/v2
    }

    @After
    public void shutdown() throws Exception {
        httpClient.stop();
    }

    @Test
    public void testInitialize1() throws InterruptedException, IOException {
        testInitialize("/get_pods_response.json", "/get_pod_details_response.json");
    }

    @Test
    public void testInitializeMarco() throws InterruptedException, IOException {
        testInitialize("/get_pods_response.json", "/get_pod_details_response_marco.json");
    }

    private void testInitialize(String podsResponse, String podDetailsResponse)
            throws InterruptedException, IOException {
        // Setup account
        final SensiboAccountConfiguration accountConfig = new SensiboAccountConfiguration();
        accountConfig.apiKey = "APIKEY";
        when(configuration.as(eq(SensiboAccountConfiguration.class))).thenReturn(accountConfig);

        // Setup initial response
        final String getPodsResponse = IOUtils.toString(getClass().getResourceAsStream(podsResponse));
        stubFor(get(urlEqualTo("/api/v2/users/me/pods?apiKey=APIKEY"))
                .willReturn(aResponse().withStatus(200).withBody(getPodsResponse)));

        // Setup 2nd response with details
        final String getPodDetailsResponse = IOUtils.toString(getClass().getResourceAsStream(podDetailsResponse));
        stubFor(get(urlEqualTo("/api/v2/pods/PODID?apiKey=APIKEY&fields=*"))
                .willReturn(aResponse().withStatus(200).withBody(getPodDetailsResponse)));

        when(sensiboAccountMock.getConfiguration()).thenReturn(configuration);
        when(sensiboAccountMock.getUID()).thenReturn(new ThingUID("sensibo:account:thinguid"));

        final SensiboAccountHandler subject = new SensiboAccountHandler(sensiboAccountMock, httpClient);
        // Async, poll for status
        subject.initialize();

        // Verify num things found == 1
        int numPods = 0;
        for (int i = 0; i < 20; i++) {
            final List<SensiboSky> things = subject.getModel().getPods();
            numPods = things.size();
            if (numPods == 1) {
                break;
            } else {
                // Wait some more
                Thread.sleep(200);
            }
        }

        assertEquals(1, numPods);
    }
}
