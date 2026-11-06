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
package org.openhab.binding.mqtt.awtrixlight.internal.ng.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonParser;

/**
 * Test cases for the {@link AwtrixNgAppHandler}.
 *
 * @author Keith T. Garner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AwtrixNgAppHandlerTest {

    private static final ThingUID CLOCK_UID = new ThingUID(THING_TYPE_NG_CLOCK, new ThingUID("mqtt", "broker", "b"),
            "c");
    private static final String STORE_TOPIC = "a4cf12ab34cd/openhab/apps/weather";

    private @Mock ThingHandlerCallback callback;
    private @Mock Bridge clock;
    private @Mock AwtrixNgBridgeHandler bridgeHandler;

    /** Changes the configuration the way openHAB does, which also drops the configuration the handler cached. */
    private static class TestHandler extends AwtrixNgAppHandler {
        TestHandler(Thing thing) {
            super(thing);
        }

        void configure(String appName) {
            configure(appName, false);
        }

        void configure(String appName, boolean useButtons) {
            updateConfiguration(new Configuration(Map.of("appname", appName, "useButtons", useButtons)));
        }
    }

    private Thing app;
    private TestHandler handler;

    @BeforeEach
    void setUp() {
        app = ThingBuilder.create(THING_TYPE_NG_APP, "weather").withBridge(CLOCK_UID)
                .withConfiguration(new Configuration(Map.of("appname", "weather"))).build();
        handler = new TestHandler(app);
        handler.setCallback(callback);

        when(callback.getBridge(CLOCK_UID)).thenReturn(clock);
        when(clock.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(clock.getHandler()).thenReturn(bridgeHandler);
        when(bridgeHandler.pushApp(any(), any())).thenReturn(true);
        when(bridgeHandler.storeApp(any(), any())).thenReturn(true);
    }

    private ChannelUID channel(String id) {
        return new ChannelUID(app.getUID(), id);
    }

    private ThingStatusInfo lastStatus() {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(app), captor.capture());
        return captor.getValue();
    }

    private void stored(String json) {
        handler.processMessage(STORE_TOPIC, json.getBytes(StandardCharsets.UTF_8));
    }

    private void assertPushed(String expectedJson) {
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(bridgeHandler, atLeastOnce()).pushApp(eq("weather"), payload.capture());
        assertEquals(JsonParser.parseString(expectedJson), JsonParser.parseString(payload.getValue()),
                payload.getValue());
    }

    private void assertStored(String expectedJson) {
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(bridgeHandler, atLeastOnce()).storeApp(eq("weather"), payload.capture());
        assertEquals(JsonParser.parseString(expectedJson), JsonParser.parseString(payload.getValue()),
                payload.getValue());
    }

    /** Starts the handler with the stored state it would get from the broker. */
    private void synchronizedWith(String json) {
        handler.initialize();
        stored(json);
        clearInvocations(bridgeHandler, callback);
    }

    @Test
    void waitsForTheStoredStateOfTheApp() {
        handler.initialize();

        assertEquals(ThingStatus.UNKNOWN, lastStatus().getStatus());
        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void storedStateIsRestoredAndPushedToTheClock() {
        handler.initialize();

        stored("{\"text\":\"21 C\",\"icon\":\"2422\",\"durationMs\":5000}");

        verify(callback).stateUpdated(channel("text"), new StringType("21 C"));
        verify(callback).stateUpdated(channel("icon"), new StringType("2422"));
        verify(callback).stateUpdated(channel("duration"), new QuantityType<>(5000, MetricPrefix.MILLI(Units.SECOND)));
        verify(callback).stateUpdated(channel("lifetime"), UnDefType.NULL);
        assertPushed("{\"text\":\"21 C\",\"icon\":\"2422\",\"durationMs\":5000}");
        assertEquals(ThingStatus.ONLINE, lastStatus().getStatus());
    }

    @Test
    void withoutStoredStateTheAppStartsWithEmptyText() {
        handler.initialize();

        handler.finishSynchronization();

        // An empty payload would delete the app on the clock, so the text is always sent
        assertPushed("{\"text\":\"\"}");
        assertEquals(ThingStatus.ONLINE, lastStatus().getStatus());
    }

    @Test
    void timeoutOfTheSynchronizationLeavesTheStoredStateAlone() {
        handler.initialize();

        handler.finishSynchronization();

        verify(bridgeHandler, never()).storeApp(any(), any());
    }

    @Test
    void storedStateThatArrivesAfterTheTimeoutIsUsed() {
        handler.initialize();
        handler.finishSynchronization();
        clearInvocations(bridgeHandler);

        stored("{\"text\":\"21 C\"}");

        assertPushed("{\"text\":\"21 C\"}");
        handler.deviceOnline();
        assertPushed("{\"text\":\"21 C\"}");
    }

    @Test
    void storedStateThatArrivesAfterTheAppWasChangedIsIgnored() {
        handler.initialize();
        handler.finishSynchronization();
        handler.handleCommand(channel("text"), new StringType("22 C"));
        clearInvocations(bridgeHandler);

        stored("{\"text\":\"21 C\"}");

        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void storedStateThatArrivesAfterTheTimeoutIsUsedOnce() {
        handler.initialize();
        handler.finishSynchronization();
        stored("{\"text\":\"21 C\"}");
        clearInvocations(bridgeHandler);

        stored("{\"text\":\"echo of another change\"}");

        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void synchronizationTimeoutThatRunsAfterDisposeChangesNothing() {
        handler.initialize();
        handler.dispose();

        handler.finishSynchronization();

        verify(bridgeHandler, never()).pushApp(any(), any());
        verify(bridgeHandler, never()).storeApp(any(), any());
    }

    @Test
    void invalidStoredStateStartsEmptyOnTheClockOnly() {
        handler.initialize();

        stored("{not json");

        assertPushed("{\"text\":\"\"}");
        // What cannot be read stays in the store, a change of the app replaces it
        verify(bridgeHandler, never()).storeApp(any(), any());
        assertEquals(ThingStatus.ONLINE, lastStatus().getStatus());
        handler.handleCommand(channel("text"), new StringType("22 C"));
        assertStored("{\"text\":\"22 C\"}");
    }

    @Test
    void emptyStoredStateStartsEmptyAndIsStored() {
        handler.initialize();

        stored("");

        assertPushed("{\"text\":\"\"}");
        assertStored("{\"text\":\"\"}");
    }

    @Test
    void storedStateWithoutTextStillSendsText() {
        handler.initialize();

        stored("{\"icon\":\"1\"}");

        assertPushed("{\"text\":\"\",\"icon\":\"1\"}");
    }

    @Test
    void laterStoredStateIsIgnored() {
        synchronizedWith("{\"text\":\"first\"}");

        stored("{\"text\":\"echo of another change\"}");

        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void commandChangesPushesAndStoresTheApp() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.handleCommand(channel("text-color"), HSBType.RED);
        handler.handleCommand(channel("duration"), new QuantityType<>(10, Units.SECOND));

        assertPushed("{\"text\":\"21 C\",\"textColor\":\"#FF0000\",\"durationMs\":10000}");
        assertStored("{\"text\":\"21 C\",\"textColor\":\"#FF0000\",\"durationMs\":10000}");
        // Every command republishes all states, so the color is reported again by the second command
        verify(callback, atLeastOnce()).stateUpdated(channel("text-color"), HSBType.RED);
        verify(callback, atLeastOnce()).stateUpdated(channel("text-palette"), OnOffType.OFF);
    }

    @Test
    void commandOfTheWrongTypeChangesNothing() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.handleCommand(channel("text-color"), OnOffType.ON);
        handler.handleCommand(channel("duration"), new StringType("long"));

        verify(bridgeHandler, never()).pushApp(any(), any());
        verify(bridgeHandler, never()).storeApp(any(), any());
    }

    @Test
    void commandsBeforeSynchronizationAreIgnored() {
        handler.initialize();

        handler.handleCommand(channel("text"), new StringType("too early"));

        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void resetStartsOverWithEmptyText() {
        synchronizedWith("{\"text\":\"21 C\",\"icon\":\"2422\"}");

        handler.handleCommand(channel(CHANNEL_RESET), OnOffType.ON);

        assertPushed("{\"text\":\"\"}");
        assertStored("{\"text\":\"\"}");
        verify(callback).stateUpdated(channel("icon"), UnDefType.NULL);
    }

    @Test
    void refreshRepublishesTheStatesOnly() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.handleCommand(channel("text"), RefreshType.REFRESH);

        verify(callback).stateUpdated(channel("text"), new StringType("21 C"));
        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void enabledCommandSwitchesTheAppInTheRotation() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.handleCommand(channel(CHANNEL_ENABLED), OnOffType.OFF);
        handler.handleCommand(channel(CHANNEL_ENABLED), OnOffType.ON);

        verify(bridgeHandler).setAppEnabled("weather", false);
        verify(bridgeHandler).setAppEnabled("weather", true);
    }

    @Test
    void clockComingOnlinePushesTheAppAgain() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.deviceOnline();

        assertPushed("{\"text\":\"21 C\"}");
        verify(bridgeHandler, never()).storeApp(any(), any());
    }

    @Test
    void stateThatCouldNotBeStoredIsStoredWhenTheClockComesOnline() {
        synchronizedWith("{\"text\":\"21 C\"}");
        when(bridgeHandler.storeApp(any(), any())).thenReturn(false);
        handler.handleCommand(channel("text"), new StringType("22 C"));
        when(bridgeHandler.storeApp(any(), any())).thenReturn(true);
        clearInvocations(bridgeHandler);

        handler.deviceOnline();
        handler.deviceOnline();

        assertStored("{\"text\":\"22 C\"}");
        // Once stored, a later start of the clock only sends the app
        verify(bridgeHandler, times(1)).storeApp(any(), any());
    }

    @Test
    void clockComingOnlineBeforeSynchronizationPushesNothing() {
        handler.initialize();

        handler.deviceOnline();

        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void activeChannelFollowsTheClock() {
        handler.setActive(true);
        handler.setActive(false);

        verify(callback).stateUpdated(channel(CHANNEL_ACTIVE), OnOffType.ON);
        verify(callback).stateUpdated(channel(CHANNEL_ACTIVE), OnOffType.OFF);
    }

    @Test
    void invalidAppNameIsAConfigurationError() {
        for (String name : new String[] { "", "has space", "ümlaut", "a".repeat(33) }) {
            handler.configure(name);

            handler.initialize();

            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, lastStatus().getStatusDetail(), name);
        }
        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void renamingMovesTheAppToTheNewName() {
        synchronizedWith("{\"text\":\"21 C\"}");
        when(bridgeHandler.unregisterApp("weather", handler, app.getUID())).thenReturn(true);
        when(bridgeHandler.registerApp(handler, app.getUID())).thenReturn(true);
        handler.configure("forecast");

        handler.initialize();

        verify(bridgeHandler).unregisterApp("weather", handler, app.getUID());
        verify(bridgeHandler).deleteApp("weather");
        verify(bridgeHandler).registerApp(handler, app.getUID());
        assertEquals("forecast", handler.getAppName());
        // What the app holds goes along, it is not emptied
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(bridgeHandler).pushApp(eq("forecast"), payload.capture());
        assertEquals(JsonParser.parseString("{\"text\":\"21 C\"}"), JsonParser.parseString(payload.getValue()));
        verify(bridgeHandler).storeApp(eq("forecast"), any());
    }

    @Test
    void appThatGetsAValidNameAfterAnInvalidOneIsRegistered() {
        handler.configure("has space");
        handler.initialize();
        verify(bridgeHandler, never()).registerApp(any(), any());

        handler.configure("weather");
        handler.initialize();

        verify(bridgeHandler).registerApp(handler, app.getUID());
    }

    @Test
    void unknownBridgeStatusIsNotAnError() {
        handler.initialize();

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null));

        assertEquals(ThingStatus.UNKNOWN, lastStatus().getStatus());
    }

    @Test
    void renamedAppThatDoesNotGetItsNameIsNotSent() {
        synchronizedWith("{\"text\":\"21 C\"}");
        when(bridgeHandler.unregisterApp("weather", handler, app.getUID())).thenReturn(true);
        handler.configure("forecast");

        handler.initialize();

        verify(bridgeHandler).registerApp(handler, app.getUID());
        verify(bridgeHandler, never()).pushApp(any(), any());
        verify(bridgeHandler, never()).storeApp(any(), any());
    }

    @Test
    void appNameThatTheBridgeRefusesIsAConfigurationErrorAndTheAppStaysQuiet() {
        handler.initialize();

        handler.rejectAppName("weather");
        stored("{\"text\":\"21 C\"}");

        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, lastStatus().getStatusDetail());
        verify(bridgeHandler, never()).pushApp(any(), any());
        verify(bridgeHandler, never()).storeApp(any(), any());
    }

    @Test
    void appThatWasRefusedItsNameDoesNotPushWhenTheClockComesOnline() {
        synchronizedWith("{\"text\":\"21 C\"}");
        handler.rejectAppName("weather");

        handler.deviceOnline();

        verify(bridgeHandler, never()).pushApp(any(), any());
    }

    @Test
    void commandsOfAnAppThatWasRefusedItsNameDoNotReachTheOwnersApp() {
        handler.initialize();
        handler.rejectAppName("weather");

        handler.handleCommand(channel(CHANNEL_ENABLED), OnOffType.OFF);

        verify(bridgeHandler, never()).setAppEnabled(any(), anyBoolean());
    }

    @Test
    void appNameOfAnotherThingIsAConfigurationError() {
        synchronizedWith("{\"text\":\"21 C\"}");
        when(bridgeHandler.isAppNameTaken("forecast", handler)).thenReturn(true);
        handler.configure("forecast");

        handler.initialize();

        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, lastStatus().getStatusDetail());
        // The app stays where it is, nothing is deleted or taken over
        assertEquals("weather", handler.getAppName());
        verify(bridgeHandler, never()).unregisterApp(any(), any(), any());
        verify(bridgeHandler, never()).deleteApp(any());
    }

    @Test
    void removalOfAThingThatDoesNotOwnTheAppLeavesTheAppAlone() {
        synchronizedWith("{\"text\":\"21 C\"}");
        when(bridgeHandler.isAppNameTaken("weather", handler)).thenReturn(true);

        handler.handleRemoval();

        verify(bridgeHandler, never()).deleteApp(any());
    }

    @Test
    void changingTheConfigurationKeepsWhatTheAppHolds() {
        synchronizedWith("{\"text\":\"21 C\",\"icon\":\"2422\"}");
        handler.configure("weather", true);

        handler.initialize();

        // The bridge is not told about this, so nothing is sent and nothing waits for a stored state again
        verify(bridgeHandler, never()).pushApp(any(), any());
        verify(bridgeHandler, never()).unregisterApp(any(), any(), any());
        assertEquals(ThingStatus.ONLINE, lastStatus().getStatus());
        handler.handleCommand(channel("text"), new StringType("22 C"));
        assertPushed("{\"text\":\"22 C\",\"icon\":\"2422\"}");
    }

    @Test
    void removalDeletesTheApp() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.handleRemoval();

        verify(bridgeHandler).deleteApp("weather");
    }

    @Test
    void offlineBridgeTakesTheAppOffline() {
        synchronizedWith("{\"text\":\"21 C\"}");

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));

        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, lastStatus().getStatusDetail());
    }

    @Test
    void textMarkupIsSentAsFragments() {
        synchronizedWith("{\"text\":\"\"}");

        handler.handleCommand(channel("text"), new StringType("a <font color=\"#00FF00\">b</font>"));

        assertPushed("{\"text\":[{\"text\":\"a \"},{\"text\":\"b\",\"color\":\"#00FF00\"}]}");
        verify(callback).stateUpdated(channel("text"), new StringType("a <font color=\"#00FF00\">b</font>"));
    }

    @Test
    void numberChannelsTakeNumbers() {
        synchronizedWith("{\"text\":\"x\"}");

        handler.handleCommand(channel("repeat"), new DecimalType(2));

        assertPushed("{\"text\":\"x\",\"repeat\":2}");
        State state = new DecimalType(2);
        verify(callback).stateUpdated(channel("repeat"), state);
    }

    @Test
    void buttonsOfALockedAppTriggerItsChannels() {
        handler.handleButton(CHANNEL_BUTTON_LEFT, EVENT_PRESSED);
        handler.handleButton(CHANNEL_BUTTON_SELECT, EVENT_RELEASED);

        verify(callback).channelTriggered(app, channel(CHANNEL_BUTTON_LEFT), EVENT_PRESSED);
        verify(callback).channelTriggered(app, channel(CHANNEL_BUTTON_SELECT), EVENT_RELEASED);
    }

    @Test
    void useButtonsComesFromTheConfiguration() {
        assertFalse(handler.isButtonControlled());

        handler.configure("weather", true);
        handler.initialize();

        assertTrue(handler.isButtonControlled());
    }

    @Test
    void appIdIsTheClockAndTheNameWhenTheClockKnowsItsUniqueId() {
        when(clock.getProperties()).thenReturn(Map.of(PROP_UNIQUEID, "a4cf12ab34cd"));

        handler.initialize();

        assertEquals("a4cf12ab34cd-weather", app.getProperties().get(PROP_APPID));
    }

    @Test
    void appIdIsLeftAloneWhenTheClockDoesNotKnowItsUniqueIdYet() {
        handler.initialize();

        assertNull(app.getProperties().get(PROP_APPID));
    }
}
