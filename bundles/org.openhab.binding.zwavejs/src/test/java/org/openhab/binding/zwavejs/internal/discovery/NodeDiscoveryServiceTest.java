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
import static org.openhab.binding.zwavejs.internal.BindingConstants.PROPERTY_NODE_FREQ_LISTENING;
import static org.openhab.binding.zwavejs.internal.BindingConstants.PROPERTY_NODE_IS_LISTENING;
import static org.openhab.binding.zwavejs.internal.BindingConstants.PROPERTY_NODE_IS_ROUTING;
import static org.openhab.binding.zwavejs.internal.BindingConstants.PROPERTY_NODE_IS_SECURE;
import static org.openhab.binding.zwavejs.internal.BindingConstants.PROPERTY_NODE_LASTSEEN;
import static org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.core.thing.Thing.PROPERTY_MODEL_ID;
import static org.openhab.core.thing.Thing.PROPERTY_VENDOR;

import java.time.Instant;
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
        node.lastSeen = Instant.parse("2023-10-01T12:00:00Z");
        node.isFrequentListening = true;

        ThingUID bridgeUID = new ThingUID("zwavejs", "bridge");
        when(thingHandler.getThing().getUID()).thenReturn(bridgeUID);

        nodeDiscoveryService.addNodeDiscovery(node);

        ArgumentCaptor<DiscoveryResult> captor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(nodeDiscoveryService).thingDiscovered(captor.capture());

        DiscoveryResult result = captor.getValue();
        Map<String, Object> expectedProperties = new HashMap<>();
        expectedProperties.put("id", node.nodeId);
        expectedProperties.put(PROPERTY_NODE_IS_LISTENING, node.isListening);
        expectedProperties.put(PROPERTY_NODE_IS_ROUTING, node.isRouting);
        expectedProperties.put(PROPERTY_NODE_IS_SECURE, node.isSecure);
        expectedProperties.put(PROPERTY_VENDOR, node.deviceConfig.manufacturer);
        expectedProperties.put(PROPERTY_MODEL_ID, node.deviceConfig.label);
        expectedProperties.put(PROPERTY_NODE_LASTSEEN, node.lastSeen);
        expectedProperties.put(PROPERTY_NODE_FREQ_LISTENING, node.isFrequentListening);
        expectedProperties.put(PROPERTY_FIRMWARE_VERSION, node.firmwareVersion);

        assertEquals(expectedProperties, result.getProperties());
        assertEquals(bridgeUID, result.getBridgeUID());
        assertEquals("Test Manufacturer Test Device (node 1)", result.getLabel());
    }
}
