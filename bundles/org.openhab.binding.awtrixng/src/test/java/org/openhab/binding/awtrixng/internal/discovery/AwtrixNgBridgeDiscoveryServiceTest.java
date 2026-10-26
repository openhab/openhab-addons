/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awtrixng.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.awtrixng.internal.AwtrixNgBindingConstants.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.awtrixng.internal.handler.AwtrixNgBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Test cases for the {@link AwtrixNgBridgeDiscoveryService}.
 *
 * @author Keith T. Garner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class AwtrixNgBridgeDiscoveryServiceTest {

    private static final int WAIT_MS = 2000;

    private @Mock AwtrixNgBridgeHandler bridgeHandler;
    private @Mock DiscoveryListener listener;

    private Bridge clock;
    private AwtrixNgBridgeDiscoveryService discovery;

    @BeforeEach
    void setUp() {
        clock = BridgeBuilder.create(THING_TYPE_CLOCK, "clock").withBridge(new ThingUID("mqtt", "broker", "b")).build();
        lenient().when(bridgeHandler.getThing()).thenReturn(clock);
        discovery = new AwtrixNgBridgeDiscoveryService();
        discovery.addDiscoveryListener(listener);
        discovery.setThingHandler(bridgeHandler);
    }

    @Test
    void registersItselfWithTheBridge() {
        verify(bridgeHandler).setAppDiscoveryCallback(discovery);
        assertSame(bridgeHandler, discovery.getThingHandler());
    }

    @Test
    void offersTheAppWithItsIdentity() {
        clock.setProperty(PROP_UNIQUEID, "a4cf12ab34cd");

        discovery.appDiscovered("weather");

        ArgumentCaptor<DiscoveryResult> result = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, timeout(WAIT_MS)).thingDiscovered(any(), result.capture());
        DiscoveryResult found = result.getValue();
        assertEquals(new ThingUID(THING_TYPE_APP, clock.getUID(), "weather"), found.getThingUID());
        assertEquals(clock.getUID(), found.getBridgeUID());
        assertEquals("weather", found.getProperties().get(PROP_APPNAME));
        assertEquals("a4cf12ab34cd-weather", found.getProperties().get(PROP_APPID));
        assertEquals(PROP_APPID, found.getRepresentationProperty());
        // The user is told what adopting the app does
        assertTrue(found.getLabel().contains("replaces"), found.getLabel());
    }

    @Test
    void offersNothingBeforeTheClockToldItsUniqueId() {
        discovery.appDiscovered("weather");

        verify(listener, after(WAIT_MS).never()).thingDiscovered(any(), any());
    }

    @Test
    void deactivateUnregistersFromTheBridge() {
        discovery.deactivate();

        verify(bridgeHandler).removeAppDiscoveryCallback();
    }

    @Test
    void appIdMatchesWhatTheAppThingReports() {
        assertEquals("a4cf12ab34cd-weather", AwtrixNgBridgeDiscoveryService.appId("a4cf12ab34cd", "weather"));
    }
}
