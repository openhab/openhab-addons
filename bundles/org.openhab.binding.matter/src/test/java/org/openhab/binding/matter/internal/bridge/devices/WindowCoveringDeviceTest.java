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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.bridge.devices.BaseDevice.MatterDeviceOptions;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.WindowCoveringCluster;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;

/**
 * Test class for WindowCoveringDevice
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class WindowCoveringDeviceTest {

    @Mock
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient client;
    @NonNullByDefault({})
    private RollershutterItem rollershutterItem;
    @NonNullByDefault({})
    private DimmerItem dimmerItem;
    @NonNullByDefault({})
    private SwitchItem switchItem;
    @NonNullByDefault({})
    private StringItem stringItem;
    @NonNullByDefault({})
    private Metadata metadata;
    @NonNullByDefault({})
    private WindowCoveringDevice shutterDevice;
    @NonNullByDefault({})
    private WindowCoveringDevice dimmerDevice;
    @NonNullByDefault({})
    private WindowCoveringDevice switchDevice;
    @NonNullByDefault({})
    private WindowCoveringDevice stringDevice;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MetadataKey key = new MetadataKey("matter", "test");
        Map<String, Object> config = Map.of("OPEN", "UP", "CLOSED", "DOWN");
        metadata = new Metadata(key, "test", config);
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(metadata);
        when(client.setEndpointState(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        rollershutterItem = Mockito.spy(new RollershutterItem("testRoller"));
        dimmerItem = Mockito.spy(new DimmerItem("testDimmer"));
        switchItem = Mockito.spy(new SwitchItem("testSwitch"));
        stringItem = Mockito.spy(new StringItem("testString"));

        shutterDevice = new WindowCoveringDevice(metadataRegistry, client, rollershutterItem);
        dimmerDevice = new WindowCoveringDevice(metadataRegistry, client, dimmerItem);
        switchDevice = new WindowCoveringDevice(metadataRegistry, client, switchItem);
        stringDevice = new WindowCoveringDevice(metadataRegistry, client, stringItem);

        // activate so fully initialized, the "shutterDevice" will be activated by a test
        dimmerDevice.activate();
        switchDevice.activate();
        stringDevice.activate();
    }

    @Test
    void testDeviceType() {
        assertEquals("WindowCovering", shutterDevice.deviceType());
    }

    @Test
    void testHandleMatterEventPosition() {
        shutterDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 5000.0);
        verify(rollershutterItem).send(new PercentType(50));
    }

    @Test
    void testHandleMatterEventStop() {
        AbstractMap<String, Object> stoppedStatus = new LinkedHashMap<String, Object>();
        stoppedStatus.put("global", WindowCoveringCluster.MovementStatus.STOPPED.getValue());

        shutterDevice.handleMatterEvent("windowCovering", "operationalStatus", stoppedStatus);
        verify(rollershutterItem).send(StopMoveType.STOP);
    }

    @Test
    void testUpdateState() throws InterruptedException {
        rollershutterItem.setState(new PercentType(50));
        shutterDevice.updateState(rollershutterItem, rollershutterItem.getState());
        Thread.sleep(1100); // Wait for timer
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(5000));
    }

    @Test
    void testActivate() {
        rollershutterItem.setState(new PercentType(50));
        MatterDeviceOptions options = shutterDevice.activate();

        Map<String, Object> coveringMap = options.clusters.get("windowCovering");
        assertNotNull(coveringMap);
        assertEquals(5000, coveringMap.get("currentPositionLiftPercent100ths"));
    }

    @Test
    void testHandleMatterEventWithDimmer() {
        // Test 50% position
        dimmerDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 5000.0);
        verify(dimmerItem).send(new PercentType(50));

        // Test fully open
        dimmerDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 0.0);
        verify(dimmerItem).send(new PercentType(0));

        // Test fully closed
        dimmerDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 10000.0);
        verify(dimmerItem).send(new PercentType(100));
    }

    @Test
    void testHandleMatterEventWithRollershutter() {
        // Test fully closed
        shutterDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 10000.0);
        verify(rollershutterItem).send(UpDownType.DOWN);

        // Test fully open
        shutterDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 0.0);
        verify(rollershutterItem).send(UpDownType.UP);

        // Test 50% position
        shutterDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 5000.0);
        verify(rollershutterItem).send(new PercentType(50));
    }

    @Test
    void testHandleMatterEventWithSwitch() {
        // Test fully closed
        switchDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 10000.0);
        verify(switchItem).send(OnOffType.ON);

        // Test fully open
        switchDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 0.0);
        verify(switchItem).send(OnOffType.OFF);
    }

    @Test
    void testHandleMatterEventWithString() {
        // Test fully open
        stringDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 0.0);
        verify(stringItem).send(new StringType("UP"));

        // Test fully closed
        stringDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 10000.0);
        verify(stringItem).send(new StringType("DOWN"));
    }

    @Test
    void testUpdateStateWithDimmer() throws InterruptedException {
        dimmerItem.setState(new PercentType(50));
        Thread.sleep(1100); // Wait for timer
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(5000));
    }

    @Test
    void testUpdateStateWithSwitch() throws InterruptedException {
        switchItem.setState(OnOffType.ON);
        Thread.sleep(1100); // Wait for timer
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(10000));

        switchItem.setState(OnOffType.OFF);
        Thread.sleep(1100); // Wait for timer
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(0));
    }

    @Test
    void testUpdateStateWithString() throws InterruptedException {
        stringItem.setState(new StringType("UP"));
        Thread.sleep(1100); // Wait for timer
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(0));

        stringItem.setState(new StringType("DOWN"));
        Thread.sleep(1100); // Wait for timer
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(10000));
    }

    @Test
    void testSwitchItemWithInvertedLogic() {
        // Create metadata with invert=true configuration
        MetadataKey key = new MetadataKey("matter", "testSwitch");
        Map<String, Object> config = Map.of("invert", true);
        Metadata invertedMetadata = new Metadata(key, "test", config);
        when(metadataRegistry.get(any(MetadataKey.class))).thenReturn(invertedMetadata);

        // Create new device instance with inverted switch
        @SuppressWarnings("null")
        SwitchItem invertedSwitchItem = Mockito.spy(new SwitchItem("testInvertedSwitch"));
        WindowCoveringDevice invertedSwitchDevice = new WindowCoveringDevice(metadataRegistry, client,
                invertedSwitchItem);
        invertedSwitchDevice.activate();
        // Test fully closed (100%) - should result in OFF for inverted switch
        invertedSwitchDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 10000.0);
        verify(invertedSwitchItem).send(OnOffType.OFF);

        // Test fully open (0%) - should result in ON for inverted switch
        invertedSwitchDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 0.0);
        verify(invertedSwitchItem).send(OnOffType.ON);

        // Test state updates from switch to position
        invertedSwitchDevice.updateState(invertedSwitchItem, OnOffType.ON);
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(0));

        invertedSwitchDevice.updateState(invertedSwitchItem, OnOffType.OFF);
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(10000));

        invertedSwitchDevice.dispose();
    }

    @Test
    void testSwitchItemWithNormalLogic() {
        // Test fully closed (100%) - should result in ON for normal switch
        switchDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 10000.0);
        verify(switchItem).send(OnOffType.ON);

        // Test fully open (0%) - should result in OFF for normal switch
        switchDevice.handleMatterEvent("windowCovering", "targetPositionLiftPercent100ths", 0.0);
        verify(switchItem).send(OnOffType.OFF);

        // Test state updates from switch to position
        switchDevice.updateState(switchItem, OnOffType.ON);
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(10000));

        switchDevice.updateState(switchItem, OnOffType.OFF);
        verify(client).setEndpointState(any(), eq("windowCovering"), eq("currentPositionLiftPercent100ths"), eq(0));
    }

    @AfterEach
    void tearDown() {
        shutterDevice.dispose();
        dimmerDevice.dispose();
        switchDevice.dispose();
        stringDevice.dispose();
    }
}
