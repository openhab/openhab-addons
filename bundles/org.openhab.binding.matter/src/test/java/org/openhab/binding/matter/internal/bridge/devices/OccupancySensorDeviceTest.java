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
package org.openhab.binding.matter.internal.bridge.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MatterDeviceOptions;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;

import com.google.gson.JsonObject;

/**
 * Test class for OccupancySensorDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class OccupancySensorDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;
    @NonNullByDefault({})
    private SwitchItem switchItem;
    @NonNullByDefault({})
    private ContactItem contactItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private OccupancySensorDevice switchDevice;
    @NonNullByDefault({})
    private OccupancySensorDevice contactDevice;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        switchItem = Mockito.spy(new SwitchItem("testSwitch"));
        contactItem = Mockito.spy(new ContactItem("testContact"));

        switchDevice = new OccupancySensorDevice(metadataRegistry, client, switchItem);
        contactDevice = new OccupancySensorDevice(metadataRegistry, client, contactItem);
    }

    @Test
    void testDeviceType() {
        assertEquals("OccupancySensor", switchDevice.deviceType());
    }

    @Test
    void testUpdateStateWithSwitch() {
        JsonObject occupiedJson = new JsonObject();
        occupiedJson.addProperty("occupied", true);
        JsonObject unoccupiedJson = new JsonObject();
        unoccupiedJson.addProperty("occupied", false);

        switchItem.setState(OnOffType.ON);
        switchDevice.updateState(switchItem, OnOffType.ON);
        verify(client).setEndpointState(any(), eq("occupancySensing"), eq("occupancy"), eq(occupiedJson));

        switchItem.setState(OnOffType.OFF);
        switchDevice.updateState(switchItem, OnOffType.OFF);
        verify(client).setEndpointState(any(), eq("occupancySensing"), eq("occupancy"), eq(unoccupiedJson));
    }

    @Test
    void testUpdateStateWithContact() {
        JsonObject occupiedJson = new JsonObject();
        occupiedJson.addProperty("occupied", true);
        JsonObject unoccupiedJson = new JsonObject();
        unoccupiedJson.addProperty("occupied", false);

        contactItem.setState(OpenClosedType.OPEN);
        contactDevice.updateState(contactItem, OpenClosedType.OPEN);
        verify(client).setEndpointState(any(), eq("occupancySensing"), eq("occupancy"), eq(occupiedJson));

        contactItem.setState(OpenClosedType.CLOSED);
        contactDevice.updateState(contactItem, OpenClosedType.CLOSED);
        verify(client).setEndpointState(any(), eq("occupancySensing"), eq("occupancy"), eq(unoccupiedJson));
    }

    @Test
    void testActivateWithSwitch() {
        JsonObject occupiedJson = new JsonObject();
        occupiedJson.addProperty("occupied", true);

        switchItem.setState(OnOffType.ON);
        MatterDeviceOptions options = switchDevice.activate();

        Map<String, Object> occupancyMap = options.clusters.get("occupancySensing");
        assertNotNull(occupancyMap);
        Object occupancy = occupancyMap.get("occupancy");
        assertNotNull(occupancy);
        assertEquals(occupiedJson.toString(), occupancy.toString());
    }

    @Test
    void testActivateWithContact() {
        JsonObject occupiedJson = new JsonObject();
        occupiedJson.addProperty("occupied", true);

        contactItem.setState(OpenClosedType.OPEN);
        MatterDeviceOptions options = contactDevice.activate();

        Map<String, Object> occupancyMap = options.clusters.get("occupancySensing");
        assertNotNull(occupancyMap);
        Object occupancy = occupancyMap.get("occupancy");
        assertNotNull(occupancy);
        assertEquals(occupiedJson.toString(), occupancy.toString());
    }

    @Test
    void testActivateWithUnoccupiedState() {
        JsonObject unoccupiedJson = new JsonObject();
        unoccupiedJson.addProperty("occupied", false);

        switchItem.setState(OnOffType.OFF);
        MatterDeviceOptions options = switchDevice.activate();

        Map<String, Object> occupancyMap = options.clusters.get("occupancySensing");
        assertNotNull(occupancyMap);
        Object occupancy = occupancyMap.get("occupancy");
        assertNotNull(occupancy);
        assertEquals(unoccupiedJson.toString(), occupancy.toString());
    }
}
