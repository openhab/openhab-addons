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
package org.openhab.binding.boschshc.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * BridgeDiscoveryParticipant Tester.
 *
 * @author Gerd Zanker - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class BridgeDiscoveryParticipantTest {

    @Nullable
    private BridgeDiscoveryParticipant fixture;

    private final String url = "https://192.168.0.123:8446/smarthome/public/information";

    private @Mock @NonNullByDefault({}) ServiceInfo shcBridge;
    private @Mock @NonNullByDefault({}) ServiceInfo otherDevice;

    @BeforeEach
    public void beforeEach() throws Exception {
        when(shcBridge.getHostAddresses()).thenReturn(new String[] { "192.168.0.123" });
        when(otherDevice.getHostAddresses()).thenReturn(new String[] { "192.168.0.1" });

        ContentResponse contentResponse = mock(ContentResponse.class);
        when(contentResponse.getContentAsString()).thenReturn(
                "{\"apiVersions\":[\"2.9\",\"3.2\"], \"shcIpAddress\":\"192.168.0.123\", \"shcGeneration\":\"SHC_1\"}");
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        Request mockRequest = mock(Request.class);
        when(mockRequest.send()).thenReturn(contentResponse);
        when(mockRequest.method((HttpMethod) any())).thenReturn(mockRequest);

        HttpClient mockHttpClient = spy(HttpClient.class); // spy needed, because some final methods can't be mocked
        when(mockHttpClient.newRequest(url)).thenReturn(mockRequest);

        fixture = new BridgeDiscoveryParticipant(mockHttpClient);
    }

    /**
     *
     * Method: getSupportedThingTypeUIDs()
     *
     */

    @Test
    void testGetSupportedThingTypeUIDs() {
        assert fixture != null;
        assertTrue(fixture.getSupportedThingTypeUIDs().contains(BoschSHCBindingConstants.THING_TYPE_SHC));
    }

    /**
     *
     * Method: getServiceType()
     *
     */
    @Test
    void testGetServiceType() throws Exception {
        assert fixture != null;
        assertThat(fixture.getServiceType(), is("_http._tcp.local."));
    }

    @Test
    void testCreateResult() throws Exception {
        assert fixture != null;
        DiscoveryResult result = fixture.createResult(shcBridge);
        assertNotNull(result);
        assertThat(result.getBindingId(), is(BoschSHCBindingConstants.BINDING_ID));
        assertThat(result.getThingUID().getId(), is("192-168-0-123"));
        assertThat(result.getThingTypeUID().getId(), is("shc"));
        assertThat(result.getLabel(), is("Bosch Smart Home Controller (192.168.0.123)"));
    }

    @Test
    void testCreateResultOtherDevice() throws Exception {
        assert fixture != null;
        DiscoveryResult result = fixture.createResult(otherDevice);
        assertNull(result);
    }

    @Test
    void testGetThingUID() throws Exception {
        assert fixture != null;
        ThingUID thingUID = fixture.getThingUID(shcBridge);
        assertNotNull(thingUID);
        assertThat(thingUID.getBindingId(), is(BoschSHCBindingConstants.BINDING_ID));
        assertThat(thingUID.getId(), is("192-168-0-123"));
    }

    @Test
    void testGetThingUIDOtherDevice() throws Exception {
        assert fixture != null;
        assertNull(fixture.getThingUID(otherDevice));
    }

    @Test
    void testGetBridgeAddress() throws Exception {
        assert fixture != null;
        assertThat(fixture.discoverBridge(shcBridge).shcIpAddress, is("192.168.0.123"));
    }

    @Test
    void testGetBridgeAddressOtherDevice() throws Exception {
        assert fixture != null;
        assertThat(fixture.discoverBridge(otherDevice).shcIpAddress, is(""));
    }

    @Test
    void testGetPublicInformationFromPossibleBridgeAddress() throws Exception {
        assert fixture != null;
        assertThat(fixture.getPublicInformationFromPossibleBridgeAddress("192.168.0.123").shcIpAddress,
                is("192.168.0.123"));
    }

    @Test
    void testGetPublicInformationFromPossibleBridgeAddressInvalidContent() throws Exception {
        assert fixture != null;

        ContentResponse contentResponse = mock(ContentResponse.class);
        when(contentResponse.getContentAsString()).thenReturn("{\"nothing\":\"useful\"}");
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        Request mockRequest = mock(Request.class);
        when(mockRequest.send()).thenReturn(contentResponse);
        when(mockRequest.method((HttpMethod) any())).thenReturn(mockRequest);

        HttpClient mockHttpClient = spy(HttpClient.class); // spy needed, because some final methods can't be mocked
        when(mockHttpClient.newRequest(url)).thenReturn(mockRequest);

        fixture = new BridgeDiscoveryParticipant(mockHttpClient);
        assertThat(fixture.getPublicInformationFromPossibleBridgeAddress("shcAddress").shcIpAddress, is(""));
    }

    @Test
    void testGetPublicInformationFromPossibleBridgeAddressInvalidStatus() throws Exception {
        assert fixture != null;

        ContentResponse contentResponse = mock(ContentResponse.class);
        // when(contentResponse.getContentAsString()).thenReturn("{\"nothing\":\"useful\"}"); no content needed
        when(contentResponse.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);

        Request mockRequest = mock(Request.class);
        when(mockRequest.send()).thenReturn(contentResponse);
        when(mockRequest.method((HttpMethod) any())).thenReturn(mockRequest);

        HttpClient mockHttpClient = spy(HttpClient.class); // spy needed, because some final methods can't be mocked
        when(mockHttpClient.newRequest(url)).thenReturn(mockRequest);

        fixture = new BridgeDiscoveryParticipant(mockHttpClient);
        assertThat(fixture.getPublicInformationFromPossibleBridgeAddress("shcAddress").shcIpAddress, is(""));
    }
}
