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
package org.openhab.binding.lutron.internal.hw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

/**
 * Tests for the message handling shared by the HomeWorks bridges in {@link HwBridgeHandler}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwBridgeHandlerTest {
    private static final String DIMMER_ADDRESS = "[01:01:03:06:01]";

    private static class TestBridgeHandler extends HwBridgeHandler {
        final List<String> sent = new ArrayList<>();

        TestBridgeHandler(Bridge bridge) {
            super(bridge);
        }

        @Override
        public void initialize() {
        }

        @Override
        public void sendCommand(String command) {
            sent.add(command);
        }

        void receive(String line) {
            handleIncomingMessage(line);
        }

        void startup() {
            sendStartupCommands();
        }
    }

    private final Bridge bridge = mock(Bridge.class);
    private final HwDimmerHandler dimmerHandler = mock(HwDimmerHandler.class);
    private final HwDiscoveryService discoveryService = mock(HwDiscoveryService.class);

    private TestBridgeHandler handler = new TestBridgeHandler(bridge);

    @BeforeEach
    public void setUp() {
        Thing dimmer = mock(Thing.class);
        when(dimmer.getHandler()).thenReturn(dimmerHandler);
        when(dimmerHandler.getAddress()).thenReturn(DIMMER_ADDRESS);
        when(bridge.getThings()).thenReturn(List.of(dimmer));
        handler = new TestBridgeHandler(bridge);
        handler.setDiscoveryService(discoveryService);
    }

    @Test
    public void dimmerLevelUpdateReachesTheConfiguredDimmer() {
        // The processor pads the level with spaces, and a thing may be configured with different spacing or case.
        handler.receive("DL, [01:01:03:06:01],  30");

        verify(dimmerHandler).handleLevelChange(30);
        verify(discoveryService, never()).declareUnknownDimmer(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void decimalDimmerLevelsAreRounded() {
        handler.receive("DL, [01:01:03:06:01], 100.00");
        handler.receive("DL, [01:01:03:06:01],   0.00");
        handler.receive("DL, [01:01:03:06:01],  49.50");

        verify(dimmerHandler).handleLevelChange(100);
        verify(dimmerHandler).handleLevelChange(0);
        verify(dimmerHandler).handleLevelChange(50);
    }

    @Test
    public void dimmerLevelForUnknownDimmerIsDeclaredToDiscovery() {
        handler.receive("DL, [01:01:02:06:04],   0");

        verify(dimmerHandler, never()).handleLevelChange(org.mockito.ArgumentMatchers.anyInt());
        verify(discoveryService).declareUnknownDimmer("[01:01:02:06:04]");
    }

    @Test
    public void deviceWithFourFieldAddressIsNotDiscoveredAsDimmer() {
        // QED shades report their level with a DL message too, with an address that has four fields
        handler.receive("DL, [01:04:02:12], 100.00");
        handler.receive("DL, [01:04:2:12],   0.00");

        verify(discoveryService, never()).declareUnknownDimmer(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void unknownDimmerWithoutDiscoveryServiceIsIgnored() {
        TestBridgeHandler noDiscovery = new TestBridgeHandler(bridge);

        noDiscovery.receive("DL, [01:01:02:06:04],   0");

        verify(dimmerHandler, never()).handleLevelChange(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    public void malformedAndUnrelatedMessagesAreIgnored() {
        handler.receive("");
        handler.receive("DL, [01:01:03:06:01]");
        handler.receive("DL, [01:01:03:06:01], abc");
        handler.receive("KBP, [01:05:25],  1");

        verify(dimmerHandler, never()).handleLevelChange(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    public void lineListenersSeeEveryMessageUntilRemoved() {
        List<String> seen = new ArrayList<>();
        java.util.function.Consumer<String> listener = seen::add;
        handler.addLineListener(listener);

        handler.receive("KBP, [01:05:25],  1");
        handler.receive("DL, [01:01:03:06:01],  30");
        handler.removeLineListener(listener);
        handler.receive("KBR, [01:05:25],  1");

        assertEquals(List.of("KBP, [01:05:25],  1", "DL, [01:01:03:06:01],  30"), seen);
    }

    @Test
    public void startupCommandsDisablePromptsAndEnableDimmerMonitoring() {
        handler.startup();

        assertEquals(List.of("PROMPTOFF", "KBMOFF", "KLMOFF", "GSMOFF", "DLMON"), handler.sent);
    }
}
