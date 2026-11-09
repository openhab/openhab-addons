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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;

/**
 * Tests for {@link HwKeypadHandler}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwKeypadHandlerTest {
    private static final String ADDRESS = "[01:04:02]";
    private static final ThingUID BRIDGE_UID = new ThingUID("lutron", "hwtelnetbridge", "test");
    private static final ThingUID THING_UID = new ThingUID("lutron:hwkeypad:test:keypad");

    private final Bridge bridge = mock(Bridge.class);
    private final HwBridgeHandler bridgeHandler = mock(HwBridgeHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    private Thing thing = newThing(Map.of("address", ADDRESS));
    private HwKeypadHandler handler = new HwKeypadHandler(thing);

    private static Thing newThing(Map<String, Object> config) {
        return ThingBuilder.create(new ThingTypeUID("lutron", "hwkeypad"), THING_UID).withBridge(BRIDGE_UID)
                .withConfiguration(new Configuration(config)).build();
    }

    private void createHandler(Map<String, Object> config) {
        thing = newThing(config);
        handler = new HwKeypadHandler(thing);
        handler.setCallback(callback);
    }

    @BeforeEach
    public void setUp() {
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        doAnswer(invocation -> {
            Thing updated = invocation.getArgument(0);
            updated.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(), any());
        createHandler(Map.of("address", ADDRESS));
    }

    private static ChannelUID channel(String id) {
        return new ChannelUID(THING_UID, id);
    }

    @Test
    public void createsChannelsAndQueriesLedsOnInitialize() {
        handler.initialize();

        verify(callback).thingUpdated(any());
        verify(bridgeHandler).sendCommand("RKLS, " + ADDRESS);
        verify(callback).statusUpdated(eq(handler.getThing()),
                eq(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null)));
        org.junit.jupiter.api.Assertions.assertNotNull(handler.getThing().getChannel("button24"));
        org.junit.jupiter.api.Assertions.assertNotNull(handler.getThing().getChannel("led24"));
        org.junit.jupiter.api.Assertions.assertNotNull(handler.getThing().getChannel("press"));
    }

    @Test
    public void modelCreatesOnlyItsChannels() {
        createHandler(Map.of("address", ADDRESS, "model", "hwi-kp5"));

        handler.initialize();

        org.junit.jupiter.api.Assertions.assertEquals(
                List.of("button1", "button2", "button3", "button4", "button5", "button23", "button24", "led1", "led2",
                        "led3", "led4", "led5", "press"),
                handler.getThing().getChannels().stream().map(c -> c.getUID().getId()).toList());
    }

    @Test
    public void addressInAnotherFormatIsNormalized() {
        createHandler(Map.of("address", "1.4.2"));

        handler.initialize();

        verify(bridgeHandler).sendCommand("RKLS, " + ADDRESS);
        org.junit.jupiter.api.Assertions.assertEquals(ADDRESS, handler.getAddress());
    }

    @Test
    public void queriesLedsWhenBridgeComesOnline() {
        handler.initialize();

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verify(bridgeHandler, times(2)).sendCommand("RKLS, " + ADDRESS);
    }

    @Test
    public void offlineBridgeKeepsKeypadOfflineUntilItComesOnline() {
        when(bridge.getStatus()).thenReturn(ThingStatus.OFFLINE);

        handler.initialize();

        org.junit.jupiter.api.Assertions.assertEquals(ThingStatus.OFFLINE, handler.getThing().getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(ThingStatusDetail.BRIDGE_OFFLINE,
                handler.getThing().getStatusInfo().getStatusDetail());
        verify(bridgeHandler, never()).sendCommand(any());

        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        org.junit.jupiter.api.Assertions.assertEquals(ThingStatus.ONLINE, handler.getThing().getStatus());
        verify(bridgeHandler).sendCommand("RKLS, " + ADDRESS);
    }

    @Test
    public void missingBridgeKeepsKeypadOffline() {
        when(callback.getBridge(BRIDGE_UID)).thenReturn(null);

        handler.initialize();

        org.junit.jupiter.api.Assertions.assertEquals(ThingStatus.OFFLINE, handler.getThing().getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(ThingStatusDetail.BRIDGE_OFFLINE,
                handler.getThing().getStatusInfo().getStatusDetail());
        verify(bridgeHandler, never()).sendCommand(any());
    }

    @Test
    public void missingAddressAndUnknownModelAreConfigurationErrors() {
        createHandler(Map.of());
        handler.initialize();
        createHandler(Map.of("address", ADDRESS, "model", "NOSUCHMODEL"));
        handler.initialize();

        verify(callback, times(2)).statusUpdated(any(),
                org.mockito.ArgumentMatchers.argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        verify(bridgeHandler, never()).sendCommand(any());
    }

    @Test
    public void buttonMessagesFireTriggerEvents() {
        handler.initialize();

        handler.handleButton("KBP", 3);
        handler.handleButton("KBH", 3);
        handler.handleButton("KBDT", 3);
        handler.handleButton("KBR", 3);
        handler.handleButton("KBP", 99);

        ChannelUID button = channel("button3");
        verify(callback).channelTriggered(thing, button, CommonTriggerEvents.PRESSED);
        verify(callback).channelTriggered(thing, button, CommonTriggerEvents.LONG_PRESSED);
        verify(callback).channelTriggered(thing, button, CommonTriggerEvents.DOUBLE_PRESSED);
        verify(callback).channelTriggered(thing, button, CommonTriggerEvents.RELEASED);
        verify(callback, times(4)).channelTriggered(any(), any(), any());
    }

    @Test
    public void ledStatesUpdateTheLedChannels() {
        handler.initialize();

        handler.handleLedStates("0123");

        verify(callback).stateUpdated(channel("led1"), new StringType("OFF"));
        verify(callback).stateUpdated(channel("led2"), new StringType("ON"));
        verify(callback).stateUpdated(channel("led3"), new StringType("FLASH1"));
        verify(callback).stateUpdated(channel("led4"), new StringType("FLASH2"));
    }

    @Test
    public void invalidDigitsAndExtraDigitsAreIgnored() {
        handler.initialize();

        handler.handleLedStates("1x4" + "0".repeat(30));

        verify(callback).stateUpdated(channel("led1"), new StringType("ON"));
        verify(callback, never()).stateUpdated(eq(channel("led2")), any());
        verify(callback, never()).stateUpdated(eq(channel("led3")), any());
        verify(callback, never()).stateUpdated(eq(channel("led25")), any());
    }

    @Test
    public void ledCommandSendsSetLed() {
        handler.initialize();

        handler.handleCommand(channel("led5"), new StringType("flash2"));
        handler.handleCommand(channel("led6"), new StringType("OFF"));
        handler.handleCommand(channel("led7"), new StringType("BLINK"));

        verify(bridgeHandler).sendCommand("SETLED, " + ADDRESS + ", 5, 3");
        verify(bridgeHandler).sendCommand("SETLED, " + ADDRESS + ", 6, 0");
        verify(bridgeHandler, never()).sendCommand("SETLED, " + ADDRESS + ", 7, 4");
        verify(bridgeHandler, times(1 + 2)).sendCommand(any());
    }

    @Test
    @SuppressWarnings("PMD.SetDefaultLocale") // The regression only shows under a default locale with non-ASCII digits
    public void commandsUseAsciiDigitsInEveryLocale() {
        handler.initialize();
        Locale defaultLocale = Locale.getDefault();
        try {
            // Arabic (Egypt) formats numbers with Arabic-Indic digits by default
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            handler.handleCommand(channel("led5"), new StringType("ON"));
            handler.handleCommand(channel("press"), new StringType("12"));
        } finally {
            Locale.setDefault(defaultLocale);
        }

        verify(bridgeHandler).sendCommand("SETLED, " + ADDRESS + ", 5, 1");
        verify(bridgeHandler).sendCommand("KBP, " + ADDRESS + ", 12");
        verify(bridgeHandler).sendCommand("KBR, " + ADDRESS + ", 12");
    }

    @Test
    public void refreshOfALedQueriesAllLeds() {
        handler.initialize();

        handler.handleCommand(channel("led2"), RefreshType.REFRESH);

        verify(bridgeHandler, times(2)).sendCommand("RKLS, " + ADDRESS);
    }

    @Test
    public void pressCommandSimulatesAPressAndRelease() {
        handler.initialize();

        handler.handleCommand(channel("press"), new StringType("12"));
        handler.handleCommand(channel("press"), new StringType("25"));
        handler.handleCommand(channel("press"), new StringType("two"));

        verify(bridgeHandler).sendCommand("KBP, " + ADDRESS + ", 12");
        verify(bridgeHandler).sendCommand("KBR, " + ADDRESS + ", 12");
        verify(bridgeHandler, times(1 + 2)).sendCommand(any());
    }
}
