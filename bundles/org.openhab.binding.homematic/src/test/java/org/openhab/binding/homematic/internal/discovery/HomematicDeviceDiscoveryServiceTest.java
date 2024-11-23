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
package org.openhab.binding.homematic.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.homematic.test.util.BridgeHelper.createHomematicBridge;
import static org.openhab.binding.homematic.test.util.DimmerHelper.createDimmerHmDevice;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homematic.internal.communicator.HomematicGateway;
import org.openhab.binding.homematic.internal.handler.HomematicBridgeHandler;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.openhab.binding.homematic.test.util.SimpleDiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;

/**
 * Tests for {@link HomematicDeviceDiscoveryServiceTest}.
 *
 * @author Florian Stolte - Initial Contribution
 *
 */
public class HomematicDeviceDiscoveryServiceTest extends JavaTest {

    private HomematicDeviceDiscoveryService homematicDeviceDiscoveryService;
    private HomematicBridgeHandler homematicBridgeHandler;

    @BeforeEach
    public void setup() throws IOException {
        this.homematicBridgeHandler = mockHomematicBridgeHandler();
        this.homematicDeviceDiscoveryService = new HomematicDeviceDiscoveryService();
        this.homematicDeviceDiscoveryService.setThingHandler(homematicBridgeHandler);
    }

    private HomematicBridgeHandler mockHomematicBridgeHandler() throws IOException {
        HomematicBridgeHandler homematicBridgeHandler = mock(HomematicBridgeHandler.class);
        Bridge bridge = createHomematicBridge();
        HomematicGateway homematicGateway = mockHomematicGateway();
        HomematicTypeGenerator homematicTypeGenerator = mockHomematicTypeGenerator();

        when(homematicBridgeHandler.getThing()).thenReturn(bridge);
        when(homematicBridgeHandler.getGateway()).thenReturn(homematicGateway);
        when(homematicBridgeHandler.getTypeGenerator()).thenReturn(homematicTypeGenerator);

        return homematicBridgeHandler;
    }

    private HomematicGateway mockHomematicGateway() throws IOException {
        HomematicGateway homematicGateway = mock(HomematicGateway.class);

        when(homematicGateway.getInstallMode()).thenReturn(60);

        return homematicGateway;
    }

    private HomematicTypeGenerator mockHomematicTypeGenerator() {
        return mock(HomematicTypeGenerator.class);
    }

    @Test
    public void testDiscoveryResultIsReportedForNewDevice() {
        SimpleDiscoveryListener discoveryListener = new SimpleDiscoveryListener();
        homematicDeviceDiscoveryService.addDiscoveryListener(discoveryListener);

        HmDevice hmDevice = createDimmerHmDevice();
        homematicDeviceDiscoveryService.deviceDiscovered(hmDevice);

        assertThat(discoveryListener.discoveredResults.size(), is(1));
        discoveryResultMatchesHmDevice(discoveryListener.discoveredResults.element(), hmDevice);
    }

    @Test
    public void testDevicesAreLoadedFromBridgeDuringDiscovery() throws IOException {
        startScanAndWaitForLoadedDevices();

        verify(homematicBridgeHandler.getGateway()).loadAllDeviceMetadata();
    }

    @Test
    public void testInstallModeIsNotActiveDuringInitialDiscovery() throws IOException {
        startScanAndWaitForLoadedDevices();

        verify(homematicBridgeHandler.getGateway(), never()).setInstallMode(eq(true), anyInt());
    }

    @Test
    public void testInstallModeIsActiveDuringSubsequentDiscovery() throws IOException {
        homematicBridgeHandler.getThing()
                .setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));

        startScanAndWaitForLoadedDevices();

        verify(homematicBridgeHandler.getGateway()).setInstallMode(true, 60);
    }

    @Test
    public void testStoppingDiscoveryDisablesInstallMode() throws IOException {
        homematicBridgeHandler.getThing()
                .setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        homematicDeviceDiscoveryService.startScan();

        homematicDeviceDiscoveryService.stopScan();

        verify(homematicBridgeHandler.getGateway()).setInstallMode(false, 0);
    }

    private void startScanAndWaitForLoadedDevices() {
        homematicDeviceDiscoveryService.startScan();
        waitForAssert(() -> verify(homematicBridgeHandler).setOfflineStatus(), 1000, 50);
    }

    private void discoveryResultMatchesHmDevice(DiscoveryResult result, HmDevice device) {
        assertThat(result.getThingTypeUID().getId(), is(device.getType()));
        assertThat(result.getThingUID().getId(), is(device.getAddress()));
        assertThat(result.getLabel(), is(device.getName()));
    }
}
