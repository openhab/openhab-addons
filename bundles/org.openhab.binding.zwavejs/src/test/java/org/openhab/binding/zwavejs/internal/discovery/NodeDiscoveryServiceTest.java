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
package org.openhab.binding.zwavejs.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.api.dto.DeviceConfig;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.handler.ZwaveJSBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class NodeDiscoveryServiceTest {

    private NodeDiscoveryService nodeDiscoveryService = spy(new NodeDiscoveryService());
    private ZwaveJSBridgeHandler thingHandler = mock(ZwaveJSBridgeHandler.class);
    private Bridge bridge = mock(Bridge.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(bridge.getUID()).thenReturn(new ThingUID(BindingConstants.BINDING_ID, "test-bridge"));
        when(thingHandler.getThing()).thenReturn(bridge);
        nodeDiscoveryService.setThingHandler(thingHandler);
    }

    @Test
    public void testAddNodeDiscovery() {
        Node node = new Node();
        node.nodeId = 1;
        node.deviceConfig = new DeviceConfig();
        node.deviceConfig.label = "Test Device";
        node.deviceConfig.manufacturer = "Test Manufacturer";
        node.isListening = true;
        node.isRouting = true;
        node.isSecure = true;
        node.lastSeen = Date.from(java.time.Instant.parse("2023-10-01T12:00:00Z"));
        node.isFrequentListening = true;

        ThingUID bridgeUID = new ThingUID("zwavejs", "bridge");
        when(thingHandler.getThing().getUID()).thenReturn(bridgeUID);

        nodeDiscoveryService.addNodeDiscovery(node);

        ArgumentCaptor<DiscoveryResult> captor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(nodeDiscoveryService).thingDiscovered(captor.capture());

        DiscoveryResult result = captor.getValue();
        Map<String, Object> expectedProperties = new HashMap<>();
        expectedProperties.put("id", node.nodeId);
        expectedProperties.put("isListening", node.isListening);
        expectedProperties.put("isRouting", node.isRouting);
        expectedProperties.put("isSecure", node.isSecure);
        expectedProperties.put("manufacturer", node.deviceConfig.manufacturer);
        expectedProperties.put("product", node.deviceConfig.label);
        expectedProperties.put("lastSeen", node.lastSeen);
        expectedProperties.put("isFrequentListening", node.isFrequentListening);

        assertEquals(expectedProperties, result.getProperties());
        assertEquals(bridgeUID, result.getBridgeUID());
        assertEquals("Test Manufacturer Test Device (node 1)", result.getLabel());
    }
}
