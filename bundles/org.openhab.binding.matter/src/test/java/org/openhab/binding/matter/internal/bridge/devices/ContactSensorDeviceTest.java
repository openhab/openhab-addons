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

/**
 * Test class for ContactSensorDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ContactSensorDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;

    @NonNullByDefault({})
    private ContactItem contactItem;
    @NonNullByDefault({})
    private SwitchItem switchItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private ContactSensorDevice contactDevice;
    @NonNullByDefault({})
    private ContactSensorDevice switchDevice;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        metadata = new Metadata(key, "test", Map.of());
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);

        contactItem = Mockito.spy(new ContactItem("testContact"));
        switchItem = Mockito.spy(new SwitchItem("testSwitch"));
        contactDevice = new ContactSensorDevice(metadataRegistry, client, contactItem);
        switchDevice = new ContactSensorDevice(metadataRegistry, client, switchItem);
    }

    @Test
    void testDeviceType() {
        assertEquals("ContactSensor", contactDevice.deviceType());
    }

    @Test
    void testUpdateStateWithContact() {
        contactItem.setState(OpenClosedType.OPEN);
        contactDevice.updateState(contactItem, OpenClosedType.OPEN);
        verify(client).setEndpointState(any(), eq("booleanState"), eq("stateValue"), eq(false));

        contactItem.setState(OpenClosedType.CLOSED);
        contactDevice.updateState(contactItem, OpenClosedType.CLOSED);
        verify(client).setEndpointState(any(), eq("booleanState"), eq("stateValue"), eq(true));
    }

    @Test
    void testUpdateStateWithSwitch() {
        switchItem.setState(OnOffType.ON);
        switchDevice.updateState(switchItem, OnOffType.ON);
        verify(client).setEndpointState(any(), eq("booleanState"), eq("stateValue"), eq(false));

        switchItem.setState(OnOffType.OFF);
        switchDevice.updateState(switchItem, OnOffType.OFF);
        verify(client).setEndpointState(any(), eq("booleanState"), eq("stateValue"), eq(true));
    }

    @Test
    void testActivateWithContact() {
        contactItem.setState(OpenClosedType.OPEN);
        MatterDeviceOptions options = contactDevice.activate();

        Map<String, Object> booleanStateMap = options.clusters.get("booleanState");
        assertNotNull(booleanStateMap);
        assertEquals(false, booleanStateMap.get("stateValue"));

        contactItem.setState(OpenClosedType.CLOSED);
        options = contactDevice.activate();
        booleanStateMap = options.clusters.get("booleanState");
        assertNotNull(booleanStateMap);
        assertEquals(true, booleanStateMap.get("stateValue"));
    }

    @Test
    void testActivateWithSwitch() {
        switchItem.setState(OnOffType.ON);
        MatterDeviceOptions options = switchDevice.activate();

        Map<String, Object> booleanStateMap = options.clusters.get("booleanState");
        assertNotNull(booleanStateMap);
        assertEquals(false, booleanStateMap.get("stateValue"));

        switchItem.setState(OnOffType.OFF);
        options = switchDevice.activate();
        booleanStateMap = options.clusters.get("booleanState");
        assertNotNull(booleanStateMap);
        assertEquals(true, booleanStateMap.get("stateValue"));
    }
}
