/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
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

    private @NonNullByDefault({}) BridgeDiscoveryParticipant fixture;

    private final String url = "https://192.168.0.123:8446/smarthome/public/information";
    private final String urlOtherDevice = "https://192.168.0.1:8446/smarthome/public/information";

    private @Mock @NonNullByDefault({}) ServiceInfo shcBridge;
    private @Mock @NonNullByDefault({}) ServiceInfo otherDevice;

    private @Mock @NonNullByDefault({}) ContentResponse contentResponse;

    /**
     * Spy needed because some final methods can't be mocked
     */
    private @Spy @NonNullByDefault({}) HttpClient mockHttpClient;

    @BeforeEach
    public void beforeEach() throws Exception {
        when(shcBridge.getHostAddresses()).thenReturn(new String[] { "192.168.0.123" });
        when(shcBridge.getName()).thenReturn("Bosch SHC [xx-xx-xx-xx-xx-xx]");

        when(otherDevice.getHostAddresses()).thenReturn(new String[] { "192.168.0.1" });
        when(otherDevice.getName()).thenReturn("Other Device");

        when(contentResponse.getContentAsString()).thenReturn("""
                {
                    "apiVersions": ["2.9","3.2"],
                    "macAddress": "64-da-a0-ab-cd-ef",
                    "shcIpAddress": "192.168.0.123",
                    "shcGeneration": "SHC_1"
                }
                """);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        Request mockRequest = mock(Request.class);
        when(mockRequest.send()).thenReturn(contentResponse);
        when(mockRequest.method(any(HttpMethod.class))).thenReturn(mockRequest);
        when(mockRequest.timeout(anyLong(), any())).thenReturn(mockRequest);

        when(mockHttpClient.newRequest(url)).thenReturn(mockRequest);

        Request failingRequest = mock(Request.class);
        when(failingRequest.method(any(HttpMethod.class))).thenReturn(failingRequest);
        when(failingRequest.timeout(anyLong(), any())).thenReturn(failingRequest);
        when(failingRequest.send()).thenThrow(new ExecutionException(new ConnectException("Connection refused")));

        when(mockHttpClient.newRequest(urlOtherDevice)).thenReturn(failingRequest);

        fixture = new BridgeDiscoveryParticipant(mockHttpClient);
    }

    /**
     *
     * Method: getSupportedThingTypeUIDs()
     *
     */

    @Test
    void testGetSupportedThingTypeUIDs() {
        assertTrue(fixture.getSupportedThingTypeUIDs().contains(BoschSHCBindingConstants.THING_TYPE_SHC));
    }

    /**
     *
     * Method: getServiceType()
     *
     */
    @Test
    void testGetServiceType() {
        assertThat(fixture.getServiceType(), is("_http._tcp.local."));
    }

    @Test
    void testCreateResult() {
        DiscoveryResult result = fixture.createResult(shcBridge);

        assertNotNull(result);
        assertThat(result.getBindingId(), is(BoschSHCBindingConstants.BINDING_ID));
        assertThat(result.getThingUID().getId(), is("64daa0abcdef"));
        assertThat(result.getThingTypeUID().getId(), is("shc"));
        assertThat(result.getLabel(), is("Bosch Smart Home Controller (192.168.0.123)"));
        Map<String, Object> properties = result.getProperties();
        assertThat(properties.get(BridgeHandler.CONFIGURATION_PARAMETER_IP_ADDRESS), is("192.168.0.123"));
        assertThat(properties.get(Thing.PROPERTY_MAC_ADDRESS), is("64-da-a0-ab-cd-ef"));
        assertThat(properties.get(BridgeHandler.THING_PROPERTY_SHC_GENERATION), is("SHC_1"));
        assertThat(properties.get(BridgeHandler.THING_PROPERTY_API_VERSIONS), is("2.9, 3.2"));
        assertThat(result.getRepresentationProperty(), is(Thing.PROPERTY_MAC_ADDRESS));
    }

    @Test
    void testCreateResultOtherDevice() {
        DiscoveryResult result = fixture.createResult(otherDevice);

        assertNull(result);
    }

    @Test
    void testCreateResultNoIPAddress() {
        when(shcBridge.getHostAddresses()).thenReturn(new String[] { "" });

        DiscoveryResult result = fixture.createResult(shcBridge);

        assertNull(result);
    }

    @Test
    void testGetThingUID() {
        ThingUID thingUID = fixture.getThingUID(shcBridge);

        assertNotNull(thingUID);
        assertThat(thingUID.getBindingId(), is(BoschSHCBindingConstants.BINDING_ID));
        assertThat(thingUID.getId(), is("64daa0abcdef"));
    }

    @Test
    void testGetThingUIDOtherDevice() {
        assertNull(fixture.getThingUID(otherDevice));
    }

    @Test
    void testGetBridgeAddress() {
        @Nullable
        PublicInformation bridgeInformation = fixture.discoverBridge("192.168.0.123");

        assertThat(bridgeInformation, not(nullValue()));
        assertThat(bridgeInformation.shcIpAddress, is("192.168.0.123"));
    }

    @Test
    void testGetBridgeAddressOtherDevice() {
        assertThat(fixture.discoverBridge("192.168.0.1"), is(nullValue()));
    }

    @Test
    void testGetPublicInformationFromPossibleBridgeAddress() {
        @Nullable
        PublicInformation bridgeInformation = fixture.getPublicInformationFromPossibleBridgeAddress("192.168.0.123");

        assertThat(bridgeInformation, not(nullValue()));
        assertThat(bridgeInformation.shcIpAddress, is("192.168.0.123"));
    }

    @Test
    void testGetPublicInformationFromPossibleBridgeAddressInvalidContent() {
        when(contentResponse.getContentAsString()).thenReturn("{\"nothing\":\"useful\"}");

        fixture = new BridgeDiscoveryParticipant(mockHttpClient);

        assertThat(fixture.getPublicInformationFromPossibleBridgeAddress("192.168.0.123"), is(nullValue()));
    }

    @Test
    void testGetPublicInformationFromPossibleBridgeAddressInvalidStatus() {
        when(contentResponse.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);

        fixture = new BridgeDiscoveryParticipant(mockHttpClient);

        assertThat(fixture.getPublicInformationFromPossibleBridgeAddress("192.168.0.123"), is(nullValue()));
    }

    @Test
    void testGetOrComputePublicInformation() {
        @Nullable
        PublicInformation result = fixture.getOrComputePublicInformation("192.168.0.123");
        assertNotNull(result);
        @Nullable
        PublicInformation result2 = fixture.getOrComputePublicInformation("192.168.0.123");
        assertSame(result, result2);
    }

    @Test
    void testPublicConstructor() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);

        fixture = new BridgeDiscoveryParticipant(httpClientFactory);

        verify(httpClientFactory).createHttpClient(eq(BoschSHCBindingConstants.BINDING_ID), any());
    }
}
