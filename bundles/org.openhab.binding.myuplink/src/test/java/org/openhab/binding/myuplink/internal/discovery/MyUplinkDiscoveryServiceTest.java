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
package org.openhab.binding.myuplink.internal.discovery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.myuplink.internal.connector.CommunicationStatus;
import org.openhab.binding.myuplink.internal.handler.MyUplinkAccountHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit Tests to verify behaviour of DiscoveryService implementation.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class MyUplinkDiscoveryServiceTest {

    private MyUplinkAccountHandler bridgeHandler = mock(MyUplinkAccountHandler.class);

    private CommunicationStatus communicationStatus = mock(CommunicationStatus.class);

    private MyUplinkDiscoveryService discoveryService = spy(MyUplinkDiscoveryService.class);

    private final String emptyResponseString = """
            {"page":1,"itemsPerPage":100,"numItems":0,"systems":[]}
              """;

    private JsonObject emptyResponse = new JsonObject();

    private final String testResponseString = """
            {
                "page": 0,
                "itemsPerPage": 0,
                "numItems": 0,
                "systems": [
                    {
                        "systemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                        "name": "string",
                        "securityLevel": "admin",
                        "hasAlarm": true,
                        "country": "string",
                        "devices": [
                            {
                                "id": "Dev-1337",
                                "connectionState": "Disconnected",
                                "currentFwVersion": "string",
                                "product": {
                                    "serialNumber": "1337",
                                    "name": "My Device 1337"
                                }
                            },
                            {
                                "id": "Dev-4712",
                                "connectionState": "Disconnected",
                                "currentFwVersion": "string",
                                "product": {
                                    "serialNumber": "4712",
                                    "name": "My Device 4712"
                                }
                            }
                        ]
                    }
                ]
            }
            """;

    private static JsonObject testResponse = new JsonObject();

    @BeforeEach
    public void prepareTestData() {
        emptyResponse = JsonParser.parseString(emptyResponseString).getAsJsonObject();
        testResponse = JsonParser.parseString(testResponseString).getAsJsonObject();

        discoveryService.setThingHandler(bridgeHandler);
    }

    @Test
    public void testEmptyResponse() {
        discoveryService.processMyUplinkDiscoveryResult(communicationStatus, emptyResponse);

        // testdata contains no systems -> no further processing
        verify(discoveryService, never()).handleSystemDiscovery(any());
        verify(discoveryService, never()).handleDeviceDiscovery(any(), any());
        verify(discoveryService, never()).initDiscoveryResultBuilder(any(), any(), any());
    }

    @Test
    public void testSampleResponse() {
        // mocking of bridgehandler needed to get an UID.
        Bridge mockThing = mock(Bridge.class);
        when(mockThing.getUID()).thenReturn(new ThingUID(THING_TYPE_ACCOUNT, "testAccount4711"));
        when(bridgeHandler.getThing()).thenReturn(mockThing);

        discoveryService.processMyUplinkDiscoveryResult(communicationStatus, testResponse);

        // testdata contains one system
        verify(discoveryService, times(1)).handleSystemDiscovery(any());
        // testdata contains two devices
        verify(discoveryService, times(2)).handleDeviceDiscovery(any(), any());
        // builder should be called once for each device
        verify(discoveryService, times(2)).initDiscoveryResultBuilder(any(), any(), any());

        // verify that correct values are extracted from data
        verify(discoveryService).initDiscoveryResultBuilder(DEVICE_GENERIC_DEVICE, "Dev-4712", "My Device 4712");
        verify(discoveryService).initDiscoveryResultBuilder(DEVICE_GENERIC_DEVICE, "Dev-1337", "My Device 1337");
    }
}
