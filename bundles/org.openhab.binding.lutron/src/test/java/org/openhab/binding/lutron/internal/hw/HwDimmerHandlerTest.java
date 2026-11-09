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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for the bridge-related behavior of {@link HwDimmerHandler}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwDimmerHandlerTest {
    private static final String ADDRESS = "[01:01:03:06:01]";
    private static final String LEVEL_QUERY = "RDL, " + ADDRESS;

    private static final ThingUID BRIDGE_UID = new ThingUID("lutron", "hwtelnetbridge", "test");
    private static final ThingUID THING_UID = new ThingUID("lutron:hwdimmer:test:test");

    private final Thing thing = mock(Thing.class);
    private final Bridge bridge = mock(Bridge.class);
    private final HwBridgeHandler bridgeHandler = mock(HwBridgeHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private HwDimmerHandler handler = new HwDimmerHandler(thing);

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getBridgeUID()).thenReturn(BRIDGE_UID);
        when(thing.getStatusInfo()).thenReturn(bridgeStatus(ThingStatus.OFFLINE));
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("address", ADDRESS)));
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        handler = new HwDimmerHandler(thing);
        handler.setCallback(callback);
    }

    private static ThingStatusInfo bridgeStatus(ThingStatus status) {
        return new ThingStatusInfo(status, ThingStatusDetail.NONE, null);
    }

    @Test
    public void queriesLevelWhenBridgeComesOnline() {
        handler.initialize();
        verify(bridgeHandler, times(1)).sendCommand(LEVEL_QUERY);

        handler.bridgeStatusChanged(bridgeStatus(ThingStatus.ONLINE));

        verify(bridgeHandler, times(2)).sendCommand(LEVEL_QUERY);
    }

    @Test
    public void offlineBridgeKeepsDimmerOfflineUntilItComesOnline() {
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);

        handler.initialize();

        verify(callback).statusUpdated(thing,
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null));
        verify(bridgeHandler, never()).sendCommand(org.mockito.ArgumentMatchers.anyString());

        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.bridgeStatusChanged(bridgeStatus(ThingStatus.ONLINE));

        verify(bridgeHandler).sendCommand(LEVEL_QUERY);
    }

    @Test
    public void missingBridgeKeepsDimmerOffline() {
        when(callback.getBridge(BRIDGE_UID)).thenReturn(null);

        handler.initialize();

        verify(callback).statusUpdated(thing,
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null));
        verify(bridgeHandler, never()).sendCommand(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void addressWrittenInAnotherFormatIsNormalized() {
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("address", "1.1.3.6.1")));
        handler = new HwDimmerHandler(thing);
        handler.setCallback(callback);

        handler.initialize();

        verify(bridgeHandler).sendCommand(LEVEL_QUERY);
        assertEquals(ADDRESS, handler.getAddress());
    }

    @Test
    public void doesNotQueryLevelWhenBridgeGoesOffline() {
        handler.initialize();

        handler.bridgeStatusChanged(bridgeStatus(ThingStatus.OFFLINE));

        verify(bridgeHandler, times(1)).sendCommand(LEVEL_QUERY);
    }

    @Test
    public void doesNotQueryLevelBeforeAddressIsConfigured() {
        handler.bridgeStatusChanged(bridgeStatus(ThingStatus.ONLINE));

        verify(bridgeHandler, never()).sendCommand(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void tapMessagesFireTriggerEventsOnlyWhenTheThingHasTheChannel() {
        org.openhab.core.thing.ChannelUID tap = new org.openhab.core.thing.ChannelUID(THING_UID, "tap");
        handler.handleTap("KBP");
        verify(callback, never()).channelTriggered(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        when(thing.getChannel("tap")).thenReturn(org.openhab.core.thing.binding.builder.ChannelBuilder.create(tap)
                .withKind(org.openhab.core.thing.type.ChannelKind.TRIGGER).build());
        handler.handleTap("KBP");
        handler.handleTap("KBR");
        handler.handleTap("KBH");
        handler.handleTap("KBDT");

        verify(callback).channelTriggered(thing, tap, "PRESSED");
        verify(callback).channelTriggered(thing, tap, "RELEASED");
        verify(callback).channelTriggered(thing, tap, "LONG_PRESSED");
        verify(callback).channelTriggered(thing, tap, "DOUBLE_PRESSED");
    }
}
