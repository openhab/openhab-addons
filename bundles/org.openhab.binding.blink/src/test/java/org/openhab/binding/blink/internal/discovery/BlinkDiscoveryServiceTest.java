/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.discovery;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.blink.internal.dto.BlinkCamera;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.binding.blink.internal.dto.BlinkNetwork;
import org.openhab.binding.blink.internal.handler.AccountHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class BlinkDiscoveryServiceTest {

    private static final ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID("blink", "camera");
    private static final ThingTypeUID THING_TYPE_NETWORK = new ThingTypeUID("blink", "network");
    @NonNullByDefault({})
    BlinkDiscoveryService discoveryService;
    @Mock
    @NonNullByDefault({})
    AccountHandler accountHandler;
    @NonNullByDefault({})
    Thing bridge;

    @BeforeEach
    void setup() {
        discoveryService = spy(new BlinkDiscoveryService());
        discoveryService.accountHandler = accountHandler;
        bridge = new BridgeImpl(new ThingTypeUID("blink", "account"), "1234");
        bridge.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    @Test
    void testConstructorParameters() {
        assertThat(discoveryService.isBackgroundDiscoveryEnabled(), is(true));
        assertThat(discoveryService.getSupportedThingTypes(),
                containsInAnyOrder(THING_TYPE_CAMERA, THING_TYPE_NETWORK));
        assertThat(discoveryService.getScanTimeout(), is(15));
    }

    @Test
    void testStartScanCallsDiscover() {
        doNothing().when(discoveryService).discover();
        discoveryService.startScan();
        verify(discoveryService).discover();
    }

    BlinkHomescreen testHomescreen() {
        BlinkHomescreen homescreen = new BlinkHomescreen();
        homescreen.cameras = new ArrayList<>();
        homescreen.networks = new ArrayList<>();
        homescreen.owls = new ArrayList<>();
        return homescreen;
    }

    @Test
    void testDiscoverOneCamera() {
        doReturn(bridge).when(accountHandler).getThing();
        BlinkHomescreen homescreen = testHomescreen();
        BlinkCamera camera = new BlinkCamera(123L, 234L);
        camera.name = "Testcam1";
        homescreen.cameras.add(camera);
        doReturn(homescreen).when(accountHandler).getDevices(false);
        discoveryService.discover();
        verify(discoveryService).thingDiscovered(any());
    }

    @Test
    void testDiscoverOneOwl() {
        doReturn(bridge).when(accountHandler).getThing();
        BlinkHomescreen homescreen = testHomescreen();
        BlinkCamera camera = new BlinkCamera(123L, 234L);
        camera.name = "Testcam1";
        homescreen.owls.add(camera);
        doReturn(homescreen).when(accountHandler).getDevices(false);
        discoveryService.discover();
        verify(discoveryService).thingDiscovered(any());
    }

    @Test
    void testDiscoverOneNetwork() {
        doReturn(bridge).when(accountHandler).getThing();
        BlinkHomescreen homescreen = testHomescreen();
        BlinkNetwork network = new BlinkNetwork(123L);
        network.name = "Testnet1";
        homescreen.networks.add(network);
        doReturn(homescreen).when(accountHandler).getDevices(false);
        discoveryService.discover();
        verify(discoveryService).thingDiscovered(any());
    }

    @Test
    void testDiscoverMulti() {
        doReturn(bridge).when(accountHandler).getThing();
        BlinkHomescreen homescreen = testHomescreen();
        BlinkNetwork network = new BlinkNetwork(123L);
        network.name = "Testnet1";
        homescreen.networks.add(network);
        network = new BlinkNetwork(234L);
        network.name = "Testnet2";
        homescreen.networks.add(network);
        BlinkCamera camera = new BlinkCamera(123L, 234L);
        camera.name = "Testcam1";
        homescreen.cameras.add(camera);
        camera = new BlinkCamera(567L, 789L);
        camera.name = "Testcam2";
        homescreen.cameras.add(camera);
        camera = new BlinkCamera(890L, 567L);
        camera.name = "TestMini1";
        homescreen.owls.add(camera);
        doReturn(homescreen).when(accountHandler).getDevices(false);
        discoveryService.discover();
        verify(discoveryService, times(5)).thingDiscovered(any());
    }
}
