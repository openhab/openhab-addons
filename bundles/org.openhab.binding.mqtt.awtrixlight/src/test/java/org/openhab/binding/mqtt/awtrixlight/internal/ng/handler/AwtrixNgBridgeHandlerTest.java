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
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgStateDescriptionProvider;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.action.AwtrixNgActions;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.discovery.AwtrixNgBridgeDiscoveryService;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.NotificationPayload;
import org.openhab.binding.mqtt.awtrixlight.internal.ng.dto.SoundSpec;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;

/**
 * Test cases for the {@link AwtrixNgBridgeHandler}.
 *
 * @author Keith T. Garner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AwtrixNgBridgeHandlerTest {

    private static final String BASE = "a4cf12ab34cd";

    private @Mock ThingHandlerCallback callback;
    private @Mock Bridge broker;
    private @Mock AbstractBrokerHandler brokerHandler;
    private @Mock MqttBrokerConnection connection;
    private @Mock AwtrixNgAppHandler appHandler;
    private @Mock Thing appThing;
    private @Mock AwtrixNgBridgeDiscoveryService discovery;
    private @Mock AwtrixNgStateDescriptionProvider stateDescriptionProvider;

    private Bridge clock;
    private AwtrixNgBridgeHandler handler;

    @BeforeEach
    void setUp() {
        clock = BridgeBuilder.create(THING_TYPE_NG_CLOCK, "clock").withBridge(new ThingUID("mqtt", "broker", "b"))
                .withConfiguration(new Configuration(java.util.Map.of("basetopic", BASE + "/"))).build();
        handler = new AwtrixNgBridgeHandler(clock, stateDescriptionProvider);
        handler.setCallback(callback);

        when(callback.getBridge(any())).thenReturn(broker);
        when(broker.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(broker.getHandler()).thenReturn(brokerHandler);
        when(brokerHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));
        when(appThing.getUID()).thenReturn(new ThingUID(THING_TYPE_NG_APP, "weather"));
    }

    private ChannelUID channel(String id) {
        return new ChannelUID(clock.getUID(), id);
    }

    private void message(String suffix, String payload) {
        handler.processMessage(BASE + suffix, payload.getBytes(StandardCharsets.UTF_8));
    }

    private void assertPublished(String suffix, String payload) {
        verify(connection).publish(eq(BASE + suffix), eq(payload.getBytes(StandardCharsets.UTF_8)), eq(1), eq(false));
    }

    private ThingStatusInfo lastStatus() {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(clock), captor.capture());
        return captor.getValue();
    }

    @Test
    void subscribesToStateAvailabilityAndErrorTopics() {
        handler.initialize();

        verify(connection).subscribe(BASE + "/availability", handler);
        verify(connection).subscribe(BASE + "/state/#", handler);
        verify(connection).subscribe(BASE + "/event/error", handler);
        assertEquals(ThingStatus.UNKNOWN, lastStatus().getStatus());
    }

    @Test
    void emptyBaseTopicIsAConfigurationError() {
        clock.getConfiguration().put("basetopic", "  ");

        handler.initialize();

        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, lastStatus().getStatusDetail());
        verifyNoInteractions(connection);
    }

    @Test
    void availabilityDrivesThingStatus() {
        handler.initialize();

        message("/availability", "online");
        assertEquals(ThingStatus.ONLINE, lastStatus().getStatus());

        message("/availability", "offline");
        assertEquals(ThingStatus.OFFLINE, lastStatus().getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, lastStatus().getStatusDetail());
    }

    @Test
    void disposeUnsubscribes() {
        handler.initialize();

        handler.dispose();

        verify(connection).unsubscribe(BASE + "/availability", handler);
        verify(connection).unsubscribe(BASE + "/state/#", handler);
        verify(connection).unsubscribe(BASE + "/event/error", handler);
    }

    @Test
    void deviceStateUpdatesChannels() {
        handler.initialize();

        message("/state/device",
                """
                        {"version":"1.0.12","uid":"a4cf12ab34cd","boardType":"awtrixng","ipAddress":"192.168.1.42",
                         "wifiRssi":-58,"uptimeSeconds":4213,"fps":41,"brightness":255,"lightLevel":29.3,
                         "batteryPercent":20,"lowBattery":true,"batteryVoltage":3.7,"temperature":21.5,"humidity":42,"pressureHpa":1013.2,
                         "matrixPower":true,"indicators":[{"on":true,"color":"#FF0000","blinkMs":0,"fadeMs":0},
                         {"on":false,"color":"#00FF00","blinkMs":0,"fadeMs":0},
                         {"on":false,"color":"#000000","blinkMs":0,"fadeMs":0}]}
                        """);

        verify(callback).stateUpdated(channel(CHANNEL_DISPLAY), OnOffType.ON);
        verify(callback).stateUpdated(channel(CHANNEL_BRIGHTNESS), new QuantityType<>(100, Units.PERCENT));
        verify(callback).stateUpdated(channel(CHANNEL_TEMPERATURE), new QuantityType<>(21.5, SIUnits.CELSIUS));
        verify(callback).stateUpdated(channel(CHANNEL_RSSI), new QuantityType<>(-58, Units.DECIBEL_MILLIWATTS));
        verify(callback).stateUpdated(channel(CHANNEL_FPS), new DecimalType(41));
        verify(callback).stateUpdated(channel(CHANNEL_IP_ADDRESS), new StringType("192.168.1.42"));
        verify(callback).stateUpdated(channel(CHANNEL_LOW_BATTERY), OnOffType.ON);
        verify(callback).stateUpdated(channel("indicator-1"), HSBType.RED);
        // An indicator that is off shows as black, whatever color it last had
        verify(callback).stateUpdated(channel("indicator-2"), HSBType.BLACK);
        assertEquals("a4cf12ab34cd", clock.getProperties().get(PROP_UNIQUEID));
        assertEquals("1.0.12", clock.getProperties().get(PROP_FIRMWARE));
        assertEquals("awtrixng", clock.getProperties().get(PROP_BOARD_TYPE));
    }

    @Test
    void lowBatteryFollowsWhatTheClockReports() {
        handler.initialize();

        message("/state/device", "{\"batteryPercent\":5,\"lowBattery\":false}");
        message("/state/device", "{\"batteryPercent\":4,\"lowBattery\":true}");

        verify(callback).stateUpdated(channel(CHANNEL_LOW_BATTERY), OnOffType.OFF);
        verify(callback).stateUpdated(channel(CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    void missingSensorsLeaveChannelsUntouched() {
        handler.initialize();

        message("/state/device", "{\"uid\":\"a4cf12ab34cd\",\"matrixPower\":false}");

        verify(callback, never()).stateUpdated(eq(channel(CHANNEL_TEMPERATURE)), any(State.class));
        verify(callback, never()).stateUpdated(eq(channel(CHANNEL_BATTERY_LEVEL)), any(State.class));
    }

    @Test
    void invalidStateIsIgnored() {
        handler.initialize();

        message("/state/device", "{not json");
        message("/state/settings", "");

        verify(callback, never()).stateUpdated(any(), any(State.class));
    }

    @Test
    void settingsUpdateAutoBrightness() {
        handler.initialize();

        message("/state/settings", "{\"autoBrightness\":true,\"brightness\":120}");

        verify(callback).stateUpdated(channel(CHANNEL_AUTO_BRIGHTNESS), OnOffType.ON);
    }

    @Test
    void buttonsTriggerEvents() {
        handler.initialize();

        message("/state/buttons/left", "1");
        message("/state/buttons/left", "0");
        message("/state/buttons/select", "1");

        verify(callback).channelTriggered(clock, channel(CHANNEL_BUTTON_LEFT), EVENT_PRESSED);
        verify(callback).channelTriggered(clock, channel(CHANNEL_BUTTON_LEFT), EVENT_RELEASED);
        verify(callback).channelTriggered(clock, channel(CHANNEL_BUTTON_SELECT), EVENT_PRESSED);
    }

    @Test
    void errorEventTriggersWithPayload() {
        handler.initialize();
        String error = "{\"source\":\"mqtt\",\"request\":\"x\",\"error\":{\"code\":\"invalidJson\"}}";

        message("/event/error", error);

        verify(callback).channelTriggered(clock, channel(CHANNEL_ERROR), error);
    }

    @Test
    void activeAppUpdatesChannelAndRequestsScreenWhenLinked() {
        handler.initialize();
        when(callback.isChannelLinked(channel(CHANNEL_SCREEN))).thenReturn(true);

        message("/state/apps/active", "weather");

        verify(callback).stateUpdated(channel(CHANNEL_APP), new StringType("weather"));
        assertPublished("/cmd/screen/get", "");
    }

    @Test
    void activeAppDoesNotRequestScreenWhenNotLinked() {
        handler.initialize();

        message("/state/apps/active", "weather");

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void screenFrameBecomesPng() {
        handler.initialize();
        when(callback.isChannelLinked(channel(CHANNEL_SCREEN))).thenReturn(true);

        message("/state/screen", "{\"width\":2,\"height\":1,\"pixels\":[16711680,255]}");

        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(callback).stateUpdated(eq(channel(CHANNEL_SCREEN)), state.capture());
        assertEquals("image/png", ((RawType) state.getValue()).getMimeType());
    }

    @Test
    void screenFrameIsNotRenderedWhenNotLinked() {
        handler.initialize();

        message("/state/screen", "{\"width\":2,\"height\":1,\"pixels\":[16711680,255]}");

        verify(callback, never()).stateUpdated(eq(channel(CHANNEL_SCREEN)), any(State.class));
    }

    @Test
    void displayCommand() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_DISPLAY), OnOffType.OFF);

        assertPublished("/cmd/display", "{\"power\":false}");
    }

    @Test
    void brightnessCommandIsScaledAndSwitchesAutoBrightnessOff() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_BRIGHTNESS), new QuantityType<>(50, Units.PERCENT));

        assertPublished("/cmd/settings", "{\"autoBrightness\":false,\"brightness\":128}");
        verify(callback).stateUpdated(channel(CHANNEL_AUTO_BRIGHTNESS), OnOffType.OFF);
    }

    @Test
    void brightnessOutOfRangeIsIgnored() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_BRIGHTNESS), new DecimalType(101));

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void autoBrightnessCommand() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_AUTO_BRIGHTNESS), OnOffType.ON);

        assertPublished("/cmd/settings", "{\"autoBrightness\":true}");
    }

    @Test
    void indicatorCommands() {
        handler.initialize();

        handler.handleCommand(channel("indicator-1"), HSBType.RED);
        handler.handleCommand(channel("indicator-2"), OnOffType.ON);
        handler.handleCommand(channel("indicator-3"), OnOffType.OFF);

        assertPublished("/cmd/indicators/1", "{\"color\":\"#FF0000\"}");
        assertPublished("/cmd/indicators/2", "{\"color\":\"#00FF00\"}");
        assertPublished("/cmd/indicators/3", "{}");
    }

    @Test
    void blackIndicatorColorSwitchesItOff() {
        handler.initialize();

        handler.handleCommand(channel("indicator-1"), HSBType.BLACK);

        assertPublished("/cmd/indicators/1", "{}");
    }

    @Test
    void unknownIndicatorChannelIsIgnored() {
        handler.initialize();

        handler.handleCommand(channel("indicator-4"), OnOffType.ON);
        handler.handleCommand(channel("indicator-x"), OnOffType.ON);

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void soundAndRtttlCommands() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_SOUND), new StringType("ding"));
        handler.handleCommand(channel(CHANNEL_RTTTL), new StringType("two:d=4,o=5,b=200:c,e"));

        assertPublished("/cmd/audio/play", "{\"file\":\"ding\"}");
        assertPublished("/cmd/audio/play", "{\"rtttl\":\"two:d=4,o=5,b=200:c,e\"}");
    }

    @Test
    void appCommandSwitchesApp() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_APP), new StringType("weather"));

        assertPublished("/cmd/apps/switch", "weather");
    }

    @Test
    void screenRefreshRequestsFrame() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_SCREEN), RefreshType.REFRESH);

        assertPublished("/cmd/screen/get", "");
    }

    @Test
    void commandsBeforeConnectionAreDropped() {
        // initialize() was not called, so there is no connection
        handler.handleCommand(channel(CHANNEL_DISPLAY), OnOffType.ON);

        verifyNoInteractions(connection);
    }

    @Test
    void brokerOfflineMarksThingOffline() {
        handler.initialize();
        message("/availability", "online");

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));

        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, lastStatus().getStatusDetail());
        handler.handleCommand(channel(CHANNEL_DISPLAY), OnOffType.ON);
        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void connectionThatArrivesAfterTheBrokerWentOfflineIsNotUsed() {
        CompletableFuture<MqttBrokerConnection> pending = new CompletableFuture<>();
        when(brokerHandler.getConnectionAsync()).thenReturn(pending);
        handler.initialize();
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));

        pending.complete(connection);

        verify(connection, never()).subscribe(anyString(), any());
        assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, lastStatus().getStatusDetail());
    }

    @Test
    void unknownBrokerIsNotAnError() {
        when(broker.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null));

        handler.initialize();

        assertEquals(ThingStatus.UNKNOWN, lastStatus().getStatus());
    }

    @Test
    void settingsUpdateSettingsChannels() {
        handler.initialize();

        message("/state/settings", "{\"volume\":60,\"timeMode\":2,\"uppercase\":false,\"textColor\":\"#FF0000\"}");

        verify(callback).stateUpdated(channel("volume"), new QuantityType<>(60, Units.PERCENT));
        verify(callback).stateUpdated(channel("time-mode"), new DecimalType(2));
        verify(callback).stateUpdated(channel("uppercase"), OnOffType.OFF);
        verify(callback).stateUpdated(channel("text-color"), HSBType.RED);
    }

    @Test
    void settingsChannelCommandPublishesPartialSettings() {
        handler.initialize();

        handler.handleCommand(channel("transition-effect"), new StringType("Fade"));
        handler.handleCommand(channel("app-duration"), new QuantityType<>(10, Units.SECOND));

        assertPublished("/cmd/settings", "{\"transitionEffect\":\"Fade\"}");
        assertPublished("/cmd/settings", "{\"appDurationMs\":10000}");
    }

    @Test
    void settingsChannelCommandOfWrongTypeIsIgnored() {
        handler.initialize();

        handler.handleCommand(channel("volume"), OnOffType.ON);

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void moodlightCommands() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_MOODLIGHT), HSBType.BLUE);
        handler.handleCommand(channel(CHANNEL_MOODLIGHT), OnOffType.ON);
        handler.handleCommand(channel(CHANNEL_MOODLIGHT), OnOffType.OFF);

        assertPublished("/cmd/display/moodlight", "{\"color\":\"#0000FF\"}");
        assertPublished("/cmd/display/moodlight", "{\"color\":\"#FFFFFF\"}");
        // Off is an empty payload, sent for OFF
        assertPublished("/cmd/display/moodlight", "");
    }

    @Test
    void blackMoodlightSwitchesItOff() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_MOODLIGHT), HSBType.BLACK);

        assertPublished("/cmd/display/moodlight", "");
    }

    @Test
    void overlayCommand() {
        handler.initialize();

        handler.handleCommand(channel(CHANNEL_OVERLAY), new StringType("rain"));
        handler.handleCommand(channel(CHANNEL_OVERLAY), new StringType(""));

        assertPublished("/cmd/display", "{\"overlay\":\"rain\"}");
        assertPublished("/cmd/display", "{\"overlay\":null}");
    }

    private void registerApp(String name) {
        when(appHandler.getAppName()).thenReturn(name);
        when(appHandler.getThing()).thenReturn(appThing);
        handler.childHandlerInitialized(appHandler, appThing);
    }

    @Test
    void appStoreTopicIsSubscribedWhenTheAppAppearsAfterTheConnection() {
        handler.initialize();

        registerApp("weather");

        verify(connection).subscribe(BASE + "/openhab/apps/weather", appHandler);
    }

    @Test
    void appStoreTopicIsSubscribedWhenTheConnectionAppearsAfterTheApp() {
        registerApp("weather");

        handler.initialize();

        verify(connection).subscribe(BASE + "/openhab/apps/weather", appHandler);
    }

    @Test
    void appStoreTopicIsUnsubscribedWhenTheAppGoes() {
        handler.initialize();
        registerApp("weather");

        handler.childHandlerDisposed(appHandler, appThing);

        verify(connection).unsubscribe(BASE + "/openhab/apps/weather", appHandler);
    }

    @Test
    void secondAppThingWithTheSameNameDoesNotTakeOverTheApp() {
        handler.initialize();
        registerApp("weather");
        AwtrixNgAppHandler other = mock(AwtrixNgAppHandler.class);
        lenient().when(other.getAppName()).thenReturn("weather");
        lenient().when(other.getThing()).thenReturn(appThing);

        handler.childHandlerInitialized(other, appThing);
        handler.childHandlerDisposed(other, appThing);

        // The thing that lost is told, as it may have passed the check of its own initialization at the same time
        verify(other).rejectAppName("weather");
        verify(appHandler, never()).rejectAppName(any());
        assertTrue(handler.isAppNameTaken("weather", other));
        assertFalse(handler.isAppNameTaken("weather", appHandler));
        verify(connection, never()).subscribe(BASE + "/openhab/apps/weather", other);
        verify(connection, never()).unsubscribe(any(), eq(appHandler));
    }

    @Test
    void appWithInvalidNameIsNotRegistered() {
        handler.initialize();

        registerApp("not valid");

        verify(connection, never()).subscribe(eq(BASE + "/openhab/apps/not valid"), any());
    }

    @Test
    void pushedAppIsSentAndStoredSeparately() {
        handler.initialize();

        handler.pushApp("weather", "{\"text\":\"21 C\"}");
        handler.storeApp("weather", "{\"text\":\"21 C\"}");

        assertPublished("/cmd/apps/pushed/weather", "{\"text\":\"21 C\"}");
        verify(connection).publish(eq(BASE + "/openhab/apps/weather"),
                eq("{\"text\":\"21 C\"}".getBytes(StandardCharsets.UTF_8)), eq(1), eq(true));
    }

    @Test
    void deletingAppClearsTheClockAndTheStore() {
        handler.initialize();

        handler.deleteApp("weather");

        assertPublished("/cmd/apps/pushed/weather", "");
        verify(connection).publish(eq(BASE + "/openhab/apps/weather"), eq(new byte[0]), eq(1), eq(true));
    }

    @Test
    void appDeletedWithoutTheBrokerIsDeletedWhenItIsBack() {
        handler.initialize();
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));
        clearInvocations(connection);

        handler.deleteApp("weather");

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        assertPublished("/cmd/apps/pushed/weather", "");
        verify(connection).publish(eq(BASE + "/openhab/apps/weather"), eq(new byte[0]), eq(1), eq(true));
    }

    @Test
    void appDeletedWithoutTheBrokerIsDeletedOnlyOnce() {
        handler.initialize();
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));
        handler.deleteApp("weather");
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        clearInvocations(connection);

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void appEnabledIsSentAsPlainBoolean() {
        handler.initialize();

        handler.setAppEnabled("weather", false);

        assertPublished("/cmd/apps/weather/enabled", "false");
    }

    @Test
    void appsArePushedOnceWhenTheClockRestarts() {
        handler.initialize();
        registerApp("weather");

        // A clock that connects publishes its availability and its capabilities
        message("/availability", "online");
        message("/state/capabilities", CAPABILITIES);

        verify(appHandler, times(1)).deviceOnline();
    }

    @Test
    void appsFollowTheActiveAppOfTheClock() {
        handler.initialize();
        registerApp("weather");

        message("/state/apps/active", "weather");
        message("/state/apps/active", "Time");

        verify(appHandler).setActive(true);
        verify(appHandler).setActive(false);
    }

    private void press(String button) {
        message("/state/buttons/" + button, "1");
        message("/state/buttons/" + button, "0");
    }

    /** A clock that shows a button controlled app, which is the situation in which the select button locks. */
    private void showButtonControlledApp() {
        handler.initialize();
        message("/availability", "online");
        registerApp("weather");
        when(appHandler.isButtonControlled()).thenReturn(true);
        message("/state/apps/active", "weather");
        clearInvocations(connection);
    }

    @Test
    void selectButtonLocksTheClockOnAButtonControlledApp() {
        showButtonControlledApp();

        press("select");

        assertPublished("/cmd/settings", "{\"autoTransition\":false,\"blockNavigation\":true}");
        assertPublished("/cmd/indicators/3", "{\"color\":\"#FF0000\"}");
        verify(appHandler, never()).handleButton(any(), any());
    }

    @Test
    void whileLockedButtonsGoToTheApp() {
        showButtonControlledApp();
        press("select");

        message("/state/buttons/left", "1");
        press("right");

        verify(appHandler).handleButton(CHANNEL_BUTTON_LEFT, EVENT_PRESSED);
        verify(appHandler).handleButton(CHANNEL_BUTTON_RIGHT, EVENT_PRESSED);
        verify(appHandler).handleButton(CHANNEL_BUTTON_RIGHT, EVENT_RELEASED);
        // Each press extends the lock, which the indicator shows
        verify(connection, times(4)).publish(eq(BASE + "/cmd/indicators/3"),
                eq("{\"color\":\"#FF0000\"}".getBytes(StandardCharsets.UTF_8)), eq(1), eq(false));
    }

    @Test
    void leavingTheLockRestoresWhatTheClockHadReported() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        press("select");
        // The lock itself comes back from the clock and must not be taken for the settings to restore
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":true}");

        handler.leaveAppControlMode();

        assertPublished("/cmd/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        assertPublished("/cmd/indicators/3", "{}");
    }

    @Test
    void leavingTheLockWithoutReportedSettingsUsesTheNgDefaults() {
        showButtonControlledApp();
        press("select");

        handler.leaveAppControlMode();

        assertPublished("/cmd/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
    }

    @Test
    void afterTheLockButtonsAreNotPassedOn() {
        showButtonControlledApp();
        press("select");
        handler.leaveAppControlMode();

        message("/state/buttons/left", "1");

        verify(appHandler, never()).handleButton(any(), any());
    }

    @Test
    void leavingWithoutALockDoesNothing() {
        showButtonControlledApp();

        handler.leaveAppControlMode();

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void disposeLeavesTheLock() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
        press("select");

        handler.dispose();

        assertPublished("/cmd/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
    }

    @Test
    void selectButtonDoesNotLockAnAppThatIsNotButtonControlled() {
        showButtonControlledApp();
        when(appHandler.isButtonControlled()).thenReturn(false);

        press("select");

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
        verify(callback, times(2)).channelTriggered(eq(clock), eq(channel(CHANNEL_BUTTON_SELECT)), anyString());
    }

    @Test
    void selectButtonDoesNotLockWhenNoAppIsKnown() {
        handler.initialize();
        message("/state/apps/active", "Time");

        press("select");

        verify(connection, never()).publish(eq(BASE + "/cmd/settings"), any(), anyInt(), anyBoolean());
    }

    @Test
    void unknownAppsAreOfferedToTheDiscovery() {
        handler.initialize();
        handler.setAppDiscoveryCallback(discovery);

        message("/state/apps/active", "weather");

        verify(discovery).appDiscovered("weather");
    }

    @Test
    void knownAppsAndAppsThatAreNoAppsAreNotOffered() {
        handler.initialize();
        handler.setAppDiscoveryCallback(discovery);
        registerApp("weather");

        message("/state/apps/active", "weather");
        message("/state/apps/active", "Notification");
        message("/state/apps/active", "Time");
        message("/state/apps/active", "Battery");
        message("/state/apps/active", "has space");
        message("/state/apps/active", "");

        verify(discovery, never()).appDiscovered(any());
    }

    @Test
    void builtInAppsAreOfferedWhenConfigured() {
        clock.getConfiguration().put("discoverDefaultApps", true);
        handler.initialize();
        handler.setAppDiscoveryCallback(discovery);

        message("/state/apps/active", "Time");
        message("/state/apps/active", "Notification");

        verify(discovery).appDiscovered("Time");
        verify(discovery, never()).appDiscovered("Notification");
    }

    @Test
    void removedDiscoveryIsNotCalled() {
        handler.initialize();
        handler.setAppDiscoveryCallback(discovery);
        handler.removeAppDiscoveryCallback();

        message("/state/apps/active", "weather");

        verify(discovery, never()).appDiscovered(any());
    }

    @Test
    void bridgeOffersTheDiscoveryService() {
        assertTrue(handler.getServices().contains(AwtrixNgBridgeDiscoveryService.class));
    }

    private static final String CAPABILITIES = """
            {"effects":["Fade","Plasma"],"paletteEffects":["Plasma"],"transitions":["Random","Slide"],
             "overlays":["rain","snow"],"palettes":["Cloud"],"fonts":[{"name":"small"},{"name":"large"}]}
            """;

    private static java.util.List<StateOption> options(String... names) {
        return java.util.Arrays.stream(names).map(name -> new StateOption(name, name)).toList();
    }

    private ChannelUID appChannel(String id) {
        return new ChannelUID(appThing.getUID(), id);
    }

    @Test
    void capabilitiesGiveTheClockChannelsTheirOptions() {
        handler.initialize();

        message("/state/capabilities", CAPABILITIES);

        verify(stateDescriptionProvider).setStateOptions(channel(CHANNEL_TRANSITION_EFFECT),
                options("Random", "Slide"));
        verify(stateDescriptionProvider).setStateOptions(channel(CHANNEL_OVERLAY), options("rain", "snow"));
    }

    @Test
    void capabilitiesGiveTheAppsTheirOptions() {
        when(appHandler.getThing()).thenReturn(appThing);
        handler.initialize();
        registerApp("weather");

        message("/state/capabilities", CAPABILITIES);

        verify(stateDescriptionProvider).setStateOptions(appChannel(CHANNEL_EFFECT), options("Fade", "Plasma"));
        verify(stateDescriptionProvider).setStateOptions(appChannel(CHANNEL_OVERLAY), options("rain", "snow"));
        verify(stateDescriptionProvider).setStateOptions(appChannel(CHANNEL_FONT), options("small", "large"));
    }

    @Test
    void anAppThatAppearsLaterGetsTheOptionsAlreadyKnown() {
        handler.initialize();
        message("/state/capabilities", CAPABILITIES);

        registerApp("weather");

        verify(stateDescriptionProvider).setStateOptions(appChannel(CHANNEL_EFFECT), options("Fade", "Plasma"));
    }

    @Test
    void optionsOfAnAppAreForgottenWithTheApp() {
        handler.initialize();
        registerApp("weather");

        handler.childHandlerDisposed(appHandler, appThing);

        verify(stateDescriptionProvider).removeStateOptions(appChannel(CHANNEL_EFFECT));
        verify(stateDescriptionProvider).removeStateOptions(appChannel(CHANNEL_OVERLAY));
        verify(stateDescriptionProvider).removeStateOptions(appChannel(CHANNEL_FONT));
    }

    @Test
    void optionsOfTheClockAreForgottenWithTheClock() {
        handler.initialize();

        handler.dispose();

        verify(stateDescriptionProvider).removeStateOptions(channel(CHANNEL_TRANSITION_EFFECT));
        verify(stateDescriptionProvider).removeStateOptions(channel(CHANNEL_OVERLAY));
    }

    @Test
    void listsTheClockDidNotReportKeepTheDefinedOptions() {
        handler.initialize();

        message("/state/capabilities", "{\"transitions\":[],\"overlays\":null}");
        message("/state/capabilities", "{not json");

        verify(stateDescriptionProvider, never()).setStateOptions(any(), any());
    }

    @Test
    void indicatorWithBlinkAndFade() {
        handler.initialize();

        handler.setIndicator(1, "ff0000", 500, null);
        handler.setIndicator(2, "#00FF00", null, 1000);

        assertPublished("/cmd/indicators/1", "{\"color\":\"#FF0000\",\"blinkMs\":500}");
        assertPublished("/cmd/indicators/2", "{\"color\":\"#00FF00\",\"fadeMs\":1000}");
    }

    @Test
    void invalidIndicatorIsNotSent() {
        handler.initialize();

        handler.setIndicator(0, "#FF0000", null, null);
        handler.setIndicator(4, "#FF0000", null, null);
        handler.setIndicator(1, "red", null, null);
        handler.setIndicator(1, "#FF0000", -1, null);
        handler.setIndicator(1, "#FF0000", null, 65536);
        handler.resetIndicator(7);

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void moodlightWithBrightness() {
        handler.initialize();

        handler.setMoodlight("#3366ff", 120);
        handler.setMoodlight("#3366FF", null);
        handler.setMoodlight("blue", null);
        handler.setMoodlight("#3366FF", 256);
        handler.clearMoodlight();

        assertPublished("/cmd/display/moodlight", "{\"color\":\"#3366FF\",\"brightness\":120}");
        assertPublished("/cmd/display/moodlight", "{\"color\":\"#3366FF\"}");
        assertPublished("/cmd/display/moodlight", "");
        verify(connection, times(3)).publish(eq(BASE + "/cmd/display/moodlight"), any(), anyInt(), anyBoolean());
    }

    @Test
    void deviceCommands() {
        handler.initialize();

        handler.reboot();
        handler.sleep(60000);
        handler.sleep(0);

        assertPublished("/cmd/device/reboot", "");
        assertPublished("/cmd/device/sleep", "{\"durationMs\":60000}");
        verify(connection, times(2)).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void soundsNgCannotPlayAreNotSent() {
        handler.initialize();

        SoundSpec track = new SoundSpec();
        track.track = 2999;
        handler.play(track);
        for (Integer invalid : new Integer[] { 0, 3000 }) {
            SoundSpec spec = new SoundSpec();
            spec.track = invalid;
            handler.play(spec);
        }
        SoundSpec blank = new SoundSpec();
        blank.file = " ";
        handler.play(blank);
        handler.play(new SoundSpec());

        assertPublished("/cmd/audio/play", "{\"track\":2999}");
        verify(connection, times(1)).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void lockThatEndsWhileTheClockIsOfflineIsEndedWhenItIsBack() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
        press("select");
        message("/availability", "offline");
        clearInvocations(connection);

        handler.leaveAppControlMode();

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());

        message("/availability", "online");

        assertPublished("/cmd/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
        verify(connection).publish(eq(BASE + "/openhab/applock"), eq(new byte[0]), eq(1), eq(true));
    }

    @Test
    void lockThatEndsWhileTheClockIsOfflinePutsTheThirdIndicatorBackWhenItIsBack() {
        showButtonControlledApp();
        message("/state/device", "{\"indicators\":[{\"on\":false},{\"on\":false},"
                + "{\"on\":true,\"color\":\"#0000FF\",\"blinkMs\":500,\"fadeMs\":0}]}");
        press("select");
        message("/availability", "offline");
        handler.leaveAppControlMode();
        clearInvocations(connection);

        message("/availability", "online");

        assertPublished("/cmd/indicators/3", "{\"color\":\"#0000FF\",\"blinkMs\":500}");
    }

    @Test
    void lockLeftByAnEarlierRunWaitsForTheClock() {
        handler.initialize();

        message("/openhab/applock", "{\"autoTransition\":true,\"blockNavigation\":false}");

        verify(connection, never()).publish(eq(BASE + "/cmd/settings"), any(), anyInt(), anyBoolean());

        message("/availability", "online");

        assertPublished("/cmd/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
    }

    @Test
    void lockPutsTheThirdIndicatorBackAsItWas() {
        showButtonControlledApp();
        message("/state/device", "{\"indicators\":[{\"on\":false},{\"on\":false},"
                + "{\"on\":true,\"color\":\"#0000FF\",\"blinkMs\":500,\"fadeMs\":0}]}");
        press("select");
        // The lock itself is reported by the clock
        message("/state/device", "{\"indicators\":[{\"on\":false},{\"on\":false},"
                + "{\"on\":true,\"color\":\"#FF0000\",\"blinkMs\":0,\"fadeMs\":0}]}");
        clearInvocations(connection);

        handler.leaveAppControlMode();

        assertPublished("/cmd/indicators/3", "{\"color\":\"#0000FF\",\"blinkMs\":500}");
    }

    @Test
    void stopSoundGroups() {
        handler.initialize();

        handler.stopSound(null);
        handler.stopSound("alert");
        handler.stopSound("radio");
        handler.stopSound("music");

        assertPublished("/cmd/audio/stop", "{}");
        assertPublished("/cmd/audio/stop", "{\"group\":\"alert\"}");
        assertPublished("/cmd/audio/stop", "{\"group\":\"radio\"}");
        verify(connection, times(3)).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void notificationAndDismissal() {
        handler.initialize();
        NotificationPayload notification = new NotificationPayload();
        notification.name = "door";
        notification.hold = true;

        handler.showNotification(notification);
        handler.dismissNotification(null);
        handler.dismissNotification("door");
        handler.dismissNotification("not valid");

        verify(connection).publish(eq(BASE + "/cmd/notify"), any(), eq(1), eq(false));
        assertPublished("/cmd/notify/dismiss", "");
        assertPublished("/cmd/notify/dismiss/door", "");
        verify(connection, times(3)).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void appNavigation() {
        handler.initialize();

        handler.nextApp();
        handler.previousApp();
        handler.switchApp("Time", true);
        handler.switchApp(" ", true);

        assertPublished("/cmd/apps/next", "");
        assertPublished("/cmd/apps/previous", "");
        assertPublished("/cmd/apps/switch", "{\"name\":\"Time\",\"fast\":true}");
        verify(connection, times(3)).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void appOrder() {
        handler.initialize();

        handler.setAppOrder(java.util.List.of("Time", "weather"), java.util.List.of("Battery"));
        handler.setAppOrder(null, java.util.List.of("Battery"));
        handler.setAppOrder(java.util.List.of(), java.util.List.of());
        handler.setAppOrder(java.util.List.of("not valid"), java.util.List.of());
        handler.setAppOrder(java.util.List.of("Time"), null);

        assertPublished("/cmd/apps/order", "{\"order\":[\"Time\",\"weather\"],\"disabled\":[\"Battery\"]}");
        assertPublished("/cmd/apps/order", "{\"disabled\":[\"Battery\"]}");
        assertPublished("/cmd/apps/order", "{\"disabled\":[]}");
        verify(connection, times(3)).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void appsAreToldTheUniqueIdOfTheClockWhenItGetsKnown() {
        registerApp("weather");
        handler.initialize();

        message("/state/device", "{\"uid\":\"a4cf12ab34cd\"}");

        verify(appHandler).updateAppId();
    }

    @Test
    void appActionsWithoutANameAreIgnored() {
        handler.initialize();

        handler.switchApp(null, true);
        handler.setAppEnabled(null, true);

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void appEnabledRejectsInvalidNames() {
        handler.initialize();

        handler.setAppEnabled("not valid", true);

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void bridgeOffersTheActions() {
        assertTrue(handler.getServices().contains(AwtrixNgActions.class));
    }

    @Test
    void releaseThatWasNotPressedIsNotAButtonEvent() {
        showButtonControlledApp();

        // A clock reports its buttons as 0 when it connects, which must neither fire an event nor lock the app
        message("/state/buttons/select", "0");
        message("/state/buttons/left", "0");

        verify(callback, never()).channelTriggered(any(), any(), any());
        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void pressesLostWithTheConnectionAreForgotten() {
        showButtonControlledApp();
        message("/state/buttons/select", "1");

        message("/availability", "offline");
        message("/state/buttons/select", "0");

        verify(callback).channelTriggered(clock, channel(CHANNEL_BUTTON_SELECT), EVENT_PRESSED);
        verify(callback, never()).channelTriggered(clock, channel(CHANNEL_BUTTON_SELECT), EVENT_RELEASED);
    }

    @Test
    void lockStoresWhatToGoBackToAndLeavingClearsIt() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");

        press("select");

        verify(connection).publish(eq(BASE + "/openhab/applock"),
                eq("{\"autoTransition\":false,\"blockNavigation\":false}".getBytes(StandardCharsets.UTF_8)), eq(1),
                eq(true));

        handler.leaveAppControlMode();

        verify(connection).publish(eq(BASE + "/openhab/applock"), eq(new byte[0]), eq(1), eq(true));
    }

    @Test
    void appLockTopicIsSubscribed() {
        handler.initialize();

        verify(connection).subscribe(BASE + "/openhab/applock", handler);
    }

    @Test
    void lockLeftByAnEarlierRunIsEnded() {
        handler.initialize();
        message("/availability", "online");

        message("/openhab/applock", "{\"autoTransition\":true,\"blockNavigation\":false}");

        assertPublished("/cmd/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
        assertPublished("/cmd/indicators/3", "{}");
        verify(connection).publish(eq(BASE + "/openhab/applock"), eq(new byte[0]), eq(1), eq(true));
    }

    @Test
    void ownStoredLockIsNotEnded() {
        showButtonControlledApp();
        press("select");
        clearInvocations(connection);

        message("/openhab/applock", "{\"autoTransition\":true,\"blockNavigation\":false}");

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void ownStoredLockDeliveredAfterTheLockEndedIsNotEnded() {
        showButtonControlledApp();
        press("select");
        handler.leaveAppControlMode();
        clearInvocations(connection);

        message("/openhab/applock", "{\"autoTransition\":true,\"blockNavigation\":false}");

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void emptyOrIncompleteStoredLockIsIgnored() {
        handler.initialize();

        message("/openhab/applock", "");
        message("/openhab/applock", "{\"autoTransition\":true}");
        message("/openhab/applock", "{not json");

        verify(connection, never()).publish(anyString(), any(), anyInt(), anyBoolean());
    }

    @Test
    void subscriptionsAreEndedWhenTheBrokerGoesOffline() {
        handler.initialize();
        registerApp("weather");

        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));

        verify(connection).unsubscribe(BASE + "/availability", handler);
        verify(connection).unsubscribe(BASE + "/state/#", handler);
        verify(connection).unsubscribe(BASE + "/openhab/applock", handler);
        verify(connection).unsubscribe(BASE + "/openhab/apps/weather", appHandler);
    }

    @Test
    void messagesAfterDisposeAreIgnored() {
        handler.initialize();
        handler.dispose();
        clearInvocations(callback);

        message("/availability", "online");
        message("/state/device", "{\"matrixPower\":true}");

        verifyNoInteractions(callback);
    }

    @Test
    void capabilitiesTellThatTheClockRestarted() {
        handler.initialize();
        registerApp("weather");

        message("/state/capabilities", CAPABILITIES);

        verify(appHandler).deviceOnline();
    }

    @Test
    void capabilitiesThatCannotBeReadStillTellThatTheClockRestarted() {
        handler.initialize();
        registerApp("weather");

        message("/state/capabilities", "{not json");

        verify(appHandler).deviceOnline();
    }

    @Test
    void holesInTheListsOfTheCapabilitiesAreLeftOut() {
        handler.initialize();
        registerApp("weather");

        message("/state/capabilities",
                "{\"overlays\":[null,\"rain\"],\"fonts\":[null,{\"name\":null},{\"name\":\"big\"}]}");

        verify(stateDescriptionProvider).setStateOptions(new ChannelUID(appThing.getUID(), CHANNEL_FONT),
                options("big"));
        verify(appHandler).deviceOnline();
    }

    @Test
    void holeInTheIndicatorsOfTheDeviceStateIsSkipped() {
        handler.initialize();

        message("/state/device", "{\"indicators\":[null,{\"on\":false},{\"on\":false}]}");

        verify(callback).stateUpdated(channel(CHANNEL_INDICATOR_PREFIX + 2), HSBType.BLACK);
    }

    @Test
    void appThatIsRegisteredAgainIsSubscribedOnce() {
        handler.initialize();

        registerApp("weather");
        registerApp("weather");

        verify(connection, times(1)).subscribe(BASE + "/openhab/apps/weather", appHandler);
    }

    @Test
    void settingsOfTheLockThatArriveAfterItEndedAreNotTheStateToGoBackTo() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        press("select");
        handler.leaveAppControlMode();

        // The lock was delivered late, the restore after it
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":true}");
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        clearInvocations(connection);
        press("select");

        verify(connection).publish(eq(BASE + "/openhab/applock"),
                eq("{\"autoTransition\":false,\"blockNavigation\":false}".getBytes(StandardCharsets.UTF_8)), eq(1),
                eq(true));
    }

    @Test
    void settingsAfterTheRestoreAreTheStateToGoBackToAgain() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        press("select");
        handler.leaveAppControlMode();

        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        message("/state/settings", "{\"autoTransition\":true,\"blockNavigation\":false}");
        clearInvocations(connection);
        press("select");

        verify(connection).publish(eq(BASE + "/openhab/applock"),
                eq("{\"autoTransition\":true,\"blockNavigation\":false}".getBytes(StandardCharsets.UTF_8)), eq(1),
                eq(true));
    }

    @Test
    void settingsOfTheLockThatArriveWhileTheEndIsPendingAreNotTheStateToGoBackTo() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        press("select");
        message("/availability", "offline");
        handler.leaveAppControlMode();

        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":true}");
        clearInvocations(connection);
        press("select");

        verify(connection).publish(eq(BASE + "/openhab/applock"),
                eq("{\"autoTransition\":false,\"blockNavigation\":false}".getBytes(StandardCharsets.UTF_8)), eq(1),
                eq(true));
    }

    @Test
    void indicatorOfTheLockThatIsReportedAfterItEndedIsNotPutBack() {
        showButtonControlledApp();
        message("/state/device", "{\"indicators\":[{\"on\":false},{\"on\":false},{\"on\":false}]}");
        press("select");
        handler.leaveAppControlMode();

        message("/state/device", "{\"indicators\":[{\"on\":false},{\"on\":false},"
                + "{\"on\":true,\"color\":\"#FF0000\",\"blinkMs\":0,\"fadeMs\":0}]}");
        press("select");
        clearInvocations(connection);
        handler.leaveAppControlMode();

        assertPublished("/cmd/indicators/3", "{}");
    }

    @Test
    void refreshGivesAChannelWhatTheClockReportedLast() {
        handler.initialize();
        message("/state/settings", "{\"volume\":60}");
        clearInvocations(callback);

        handler.handleCommand(channel("volume"), RefreshType.REFRESH);
        handler.handleCommand(channel("uppercase"), RefreshType.REFRESH);

        verify(callback).stateUpdated(channel("volume"), new QuantityType<>(60, Units.PERCENT));
        verify(callback, never()).stateUpdated(eq(channel("uppercase")), any(State.class));
    }

    @Test
    void whatWasLearnedFromTheClockIsForgottenWhenTheHandlerIsDisposed() {
        showButtonControlledApp();
        message("/state/settings", "{\"autoTransition\":false,\"blockNavigation\":false}");
        press("select");
        message("/availability", "offline");
        handler.leaveAppControlMode();

        handler.dispose();
        handler.initialize();
        clearInvocations(connection, callback);
        message("/availability", "online");
        handler.handleCommand(channel("volume"), RefreshType.REFRESH);

        // The lock that waited for the clock belonged to the clock before, and so did the volume
        verify(connection, never()).publish(eq(BASE + "/cmd/settings"), any(), anyInt(), anyBoolean());
        verify(callback, never()).stateUpdated(eq(channel("volume")), any(State.class));
    }

    @Test
    void screenFrameWithNullPixelsIsIgnored() {
        handler.initialize();
        when(callback.isChannelLinked(channel(CHANNEL_SCREEN))).thenReturn(true);

        assertDoesNotThrow(() -> message("/state/screen", "{\"width\":32,\"height\":8,\"pixels\":null}"));

        verify(callback, never()).stateUpdated(eq(channel(CHANNEL_SCREEN)), any(State.class));
    }

    @Test
    void screenFrameWithoutPixelsIsIgnored() {
        handler.initialize();
        when(callback.isChannelLinked(channel(CHANNEL_SCREEN))).thenReturn(true);

        message("/state/screen", "{\"width\":32,\"height\":8}");

        verify(callback, never()).stateUpdated(eq(channel(CHANNEL_SCREEN)), any(State.class));
    }
}
