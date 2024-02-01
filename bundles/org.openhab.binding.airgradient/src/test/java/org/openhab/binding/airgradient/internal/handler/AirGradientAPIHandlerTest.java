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
package org.openhab.binding.airgradient.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * @author Jørgen Austvik - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class AirGradientAPIHandlerTest {

    private static final AirGradientAPIConfiguration config = new AirGradientAPIConfiguration() {
        {
            hostname = "abc123";
            token = "def456";
        }
    };

    private static final String SINGLE_CONTENT = """
             {"locationId":4321,"locationName":"Some other name","pm01":1,"pm02":2,"pm10":2,"pm003Count":536,"atmp":20.45,"rhum":16.61,"rco2":456,"tvoc":51.644928,"wifi":-54,"timestamp":"2024-01-07T13:00:20.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"serial","firmwareVersion":null,"tvocIndex":null,"noxIndex":null}
            """;

    private static final String MULTI_CONTENT = """
            [
             {"locationId":1234,"locationName":"Some Name","pm01":1,"pm02":2,"pm10":2,"pm003Count":536,"atmp":20.45,"rhum":16.61,"rco2":null,"tvoc":null,"wifi":-54,"timestamp":"2024-01-07T13:00:20.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"serial","firmwareVersion":null,"tvocIndex":null,"noxIndex":null},
             {"locationId":4321,"locationName":"Some other name","pm01":1,"pm02":2,"pm10":2,"pm003Count":536,"atmp":20.45,"rhum":16.61,"rco2":null,"tvoc":null,"wifi":-54,"timestamp":"2024-01-07T13:00:20.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"serial","firmwareVersion":null,"tvocIndex":null,"noxIndex":null}
            ]""";
    private static final String MULTI_CONTENT2 = """
            [{"locationId":654321,"locationName":"xxxx","pm01":0,"pm02":1,"pm10":1,"pm003Count":null,"atmp":24.2,"rhum":18,"rco2":519,"tvoc":50.793266,"wifi":-62,"timestamp":"2024-02-01T19:15:37.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"ecda3b1a2a50","firmwareVersion":null,"tvocIndex":52,"noxIndex":1},{"locationId":123456,"locationName":"yyyy","pm01":0,"pm02":0,"pm10":0,"pm003Count":105,"atmp":22.33,"rhum":24,"rco2":468,"tvoc":130.95694,"wifi":-50,"timestamp":"2024-02-01T19:15:34.000Z","ledMode":"co2","ledCo2Threshold1":1000,"ledCo2Threshold2":2000,"ledCo2ThresholdEnd":4000,"serialno":"84fce612e644","firmwareVersion":null,"tvocIndex":137,"noxIndex":1}]
            """;

    @Nullable
    private AirGradientAPIHandler sut;

    @Nullable
    Bridge bridge;

    @Nullable
    HttpClient httpClientMock;

    @BeforeEach
    public void setUp() {
        bridge = Mockito.mock(Bridge.class);
        httpClientMock = Mockito.mock(HttpClient.class);
        Configuration configuration = Mockito.mock(Configuration.class);

        Mockito.when(bridge.getConfiguration()).thenReturn(requireNonNull(configuration));
        Mockito.when(configuration.as(AirGradientAPIConfiguration.class)).thenReturn(config);

        sut = new AirGradientAPIHandler(requireNonNull(bridge), requireNonNull(httpClientMock));
    }

    @Test
    public void testGetRegisteredNone() {
        var res = sut.getRegisteredLocationIds();
        assertThat(res, is(empty()));
    }

    @Test
    public void testGetMeasuresNone() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.GET(anyString())).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(500);

        var res = sut.getMeasures();
        assertThat(res, is(empty()));
    }

    @Test
    public void testGetMeasuresSingle() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.GET(anyString())).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getContentAsString()).thenReturn(SINGLE_CONTENT);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(1));
        assertThat(res.get(0).locationName, is("Some other name"));
    }

    @Test
    public void testGetMeasuresMulti() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.GET(anyString())).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getContentAsString()).thenReturn(MULTI_CONTENT);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(2));
        assertThat(res.get(0).locationName, is("Some Name"));
        assertThat(res.get(1).locationName, is("Some other name"));
    }

    @Test
    public void testGetMeasuresMulti2() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.GET(anyString())).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getContentAsString()).thenReturn(MULTI_CONTENT2);

        var res = sut.getMeasures();
        assertThat(res, is(not(empty())));
        assertThat(res.size(), is(2));
        assertThat(res.get(0).locationName, is("xxxx"));
        assertThat(res.get(1).locationName, is("yyyy"));
    }

    @Test
    public void testPollNoData() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.GET(anyString())).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(500);
        ThingHandlerCallback callbackMock = Mockito.mock(ThingHandlerCallback.class);

        sut.setCallback(callbackMock);
        sut.pollingCode();

        verify(callbackMock).statusUpdated(requireNonNull(bridge),
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, null));
    }

    @Test
    public void testPollHasData() throws Exception {
        ContentResponse response = Mockito.mock(ContentResponse.class);
        Mockito.when(httpClientMock.GET(anyString())).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.getContentAsString()).thenReturn(MULTI_CONTENT);
        ThingHandlerCallback callbackMock = Mockito.mock(ThingHandlerCallback.class);

        sut.setCallback(callbackMock);
        sut.pollingCode();

        verify(callbackMock).statusUpdated(requireNonNull(bridge),
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }
}
