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
package org.openhab.binding.tapocontrol.internal.devices.camera;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tapocontrol.internal.api.camera.TapoCameraApi;
import org.openhab.binding.tapocontrol.internal.api.camera.TapoCameraApiException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Behavior tests for {@link TapoCameraHandler} with a mocked API.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TapoCameraHandlerTest {
    private static final ThingUID THING_UID = new ThingUID("tapocontrol:camera:test");

    @Mock
    private TapoCameraApi api;

    private Thing thing;
    private HandlerUnderTest handler;
    private final List<ThingStatusInfo> statuses = new ArrayList<>();
    private volatile ThingStatusInfo lastStatus;
    private final Map<String, State> updatedStates = new ConcurrentHashMap<>();
    private final CountDownLatch terminalStatus = new CountDownLatch(1);
    private final CountDownLatch thingUpdate = new CountDownLatch(1);

    /** Subclass exposing the api injection point and lifecycle control. */
    class HandlerUnderTest extends TapoCameraHandler {
        HandlerUnderTest(Thing thing) {
            super(thing, null);
        }

        @Override
        protected TapoCameraApi createApi(TapoCameraConfiguration config) {
            return api;
        }

        void simulateInitialize() {
            setCallback(callback);
            initialize();
        }
    }

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    /** Waits for the asynchronous poll cycle to reach a terminal status. */
    private void awaitTerminalStatus() throws InterruptedException {
        assertTrue(terminalStatus.await(10, TimeUnit.SECONDS), "poll cycle did not reach a terminal status");
    }

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            ChannelUID channelUID = invocation.getArgument(0);
            State state = invocation.getArgument(1);
            updatedStates.put(channelUID.getId(), state);
            return null;
        }).when(callback).stateUpdated(any(), any());
        doAnswer(invocation -> {
            ThingStatusInfo info = invocation.getArgument(1);
            statuses.add(info);
            lastStatus = info;
            // UNKNOWN is initialize()'s transient status until the first poll reports a real one; ignore it
            boolean terminal = info.getStatus() == ThingStatus.ONLINE
                    || info.getStatus() == ThingStatus.OFFLINE && info.getStatusDetail() != ThingStatusDetail.NONE;
            if (terminal) {
                terminalStatus.countDown();
            }
            return null;
        }).when(callback).statusUpdated(any(), any());
        doAnswer(invocation -> {
            thingUpdate.countDown();
            return null;
        }).when(callback).thingUpdated(any());
        thing = ThingBuilder.create(new ThingTypeUID("tapocontrol:camera"), THING_UID)
                .withConfiguration(new Configuration(Map.of("ipAddress", "192.168.1.50", "httpPort", 443, "username",
                        "admin", "password", "password", "pollingInterval", 0)))
                .withChannels(List.of(
                        ChannelBuilder.create(new ChannelUID(THING_UID, "alarm#manualAlarm"), "Switch").build(),
                        ChannelBuilder.create(new ChannelUID(THING_UID, "alarm#alarmMode"), "String").build(),
                        ChannelBuilder.create(new ChannelUID(THING_UID, "privacy#privacyMode"), "Switch").build(),
                        ChannelBuilder.create(new ChannelUID(THING_UID, "motionDetection#enabled"), "Switch").build(),
                        ChannelBuilder.create(new ChannelUID(THING_UID, "presets#gotoPreset"), "Number").build(),
                        ChannelBuilder.create(new ChannelUID(THING_UID, "system#ledStatus"), "Switch").build()))
                .build();
        handler = new HandlerUnderTest(thing);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    /** Stub helper: dispatch on which command JSON arrives; unknown sections fail as unsupported. */
    private void respondWith(Map<String, String> fixtureByModuleSection) throws Exception {
        when(api.sendCommand(any())).thenAnswer(invocation -> {
            JsonObject cmd = invocation.getArgument(0);
            String method = cmd.get("method").getAsString();
            if (!"get".equals(method)) {
                return json("{\"error_code\":0}");
            }
            String module = cmd.keySet().stream().filter(k -> !"method".equals(k)).findFirst().orElseThrow();
            String section = cmd.getAsJsonObject(module).getAsJsonArray("name").get(0).getAsString();
            String key = module + "#" + section;
            String inner = fixtureByModuleSection.get(key);
            if (inner == null) {
                throw new TapoCameraApiException("method specific failure", -40100);
            }
            return json("{\"error_code\":0,\"result\":{\"" + module + "\":{\"" + section + "\":" + inner + "}}}");
        });
    }

    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    @Test
    void successfulPollUpdatesChannelsFromResponses() throws Exception {
        respondWith(Map.of("lens_mask#lens_mask_info", "{\"enabled\":\"on\"}", //
                "msg_alarm#chn1_msg_alarm_info", "{\"enabled\":\"off\",\"alarm_mode\":[\"sound\",\"light\"]}", //
                "system#last_alarm_info", "{\"last_alarm_type\":\"motion\",\"last_alarm_time\":1689317707}", //
                "motion_detection#motion_det", "{\"enabled\":\"on\",\"digital_sensitivity\":60}", //
                "device_info#basic_info",
                "{\"device_model\":\"C200\",\"mac\":\"00:11:22:33:44:55\",\"sw_version\":\"1.3.0\"}", //
                "led#config", "{\"enabled\":\"on\"}", //
                "preset#preset", "{}"));
        handler.simulateInitialize();
        awaitTerminalStatus();

        assertEquals(ThingStatus.UNKNOWN, statuses.get(0).getStatus()); // pre-poll placeholder
        assertEquals(OnOffType.ON, updatedStates.get("privacy#privacyMode"));
        // manualAlarm reflects momentary siren commands, not the persistent alarm config polled here
        assertFalse(updatedStates.containsKey("alarm#manualAlarm"));
        assertEquals(new StringType("both"), updatedStates.get("alarm#alarmMode"));
        assertEquals(new StringType("motion"), updatedStates.get("alarm#lastAlarmType"));
        assertNotNull(updatedStates.get("alarm#lastAlarmTime"));
        assertEquals(OnOffType.ON, updatedStates.get("motionDetection#enabled"));
        assertEquals(OnOffType.ON, updatedStates.get("system#ledStatus"));
        assertEquals("C200", thing.getProperties().get(Thing.PROPERTY_MODEL_ID));
        assertEquals("00:11:22:33:44:55", thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS));
        assertEquals("1.3.0", thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));
        assertEquals(ThingStatus.ONLINE, lastStatus.getStatus());
        assertTrue(handler.getDetectedFeatures()
                .containsAll(java.util.EnumSet.of(TapoCameraFeature.ALARM, TapoCameraFeature.PRIVACY)));
    }

    @Test
    void missingHttpClientSetsOfflineConfigurationError() {
        TapoCameraHandler rawHandler = new TapoCameraHandler(thing, null);
        rawHandler.setCallback(callback);
        rawHandler.initialize();

        assertEquals(ThingStatus.OFFLINE, lastStatus.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, lastStatus.getStatusDetail());
        assertEquals("no httpClient configured", lastStatus.getDescription());
    }

    @Test
    void unsupportedFeatureIsSkippedNotFatal() throws Exception {
        when(api.sendCommand(any())).thenAnswer(invocation -> {
            JsonObject cmd = invocation.getArgument(0);
            String method = cmd.get("method").getAsString();
            if ("get".equals(method) && cmd.has("msg_alarm")) {
                throw new TapoCameraApiException("unsupported", -40100);
            }
            if (!"get".equals(method)) {
                return json("{\"error_code\":0}");
            }
            String module = cmd.keySet().stream().filter(k -> !"method".equals(k)).findFirst().orElseThrow();
            String section = cmd.getAsJsonObject(module).getAsJsonArray("name").get(0).getAsString();
            return json(
                    "{\"error_code\":0,\"result\":{\"" + module + "\":{\"" + section + "\":{\"enabled\":\"on\"}}}}");
        });
        handler.simulateInitialize();
        awaitTerminalStatus();

        assertEquals(OnOffType.ON, updatedStates.get("privacy#privacyMode")); // privacy still updated
        assertFalse(handler.getDetectedFeatures().contains(TapoCameraFeature.ALARM));
        assertTrue(handler.getDetectedFeatures().contains(TapoCameraFeature.PRIVACY));
        assertEquals(ThingStatus.ONLINE, lastStatus.getStatus());
    }

    @Test
    void transportFailureSetsOfflineKeepsChannelValues() throws Exception {
        when(api.sendCommand(any())).thenThrow(new TapoCameraApiException("timeout", 0));
        handler.simulateInitialize();
        awaitTerminalStatus();

        ThingStatusInfo last = statuses.get(statuses.size() - 1);
        assertEquals(ThingStatus.OFFLINE, last.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, last.getStatusDetail());
        assertTrue(updatedStates.isEmpty()); // no fabricated channel updates
    }

    @Test
    void transientErrorAbortsCycleInsteadOfDroppingFeature() throws Exception {
        when(api.sendCommand(any())).thenAnswer(invocation -> {
            JsonObject cmd = invocation.getArgument(0);
            if ("get".equals(cmd.get("method").getAsString()) && cmd.has("msg_alarm")) {
                // a non-capability, non-auth failure (e.g. device busy) must abort the cycle, not drop the feature
                throw new TapoCameraApiException("device busy", -40301);
            }
            return json("{\"error_code\":0,\"result\":{}}");
        });
        handler.simulateInitialize();
        awaitTerminalStatus();

        ThingStatusInfo last = statuses.get(statuses.size() - 1);
        assertEquals(ThingStatus.OFFLINE, last.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, last.getStatusDetail());
        assertTrue(handler.getDetectedFeatures().contains(TapoCameraFeature.ALARM)); // feature not dropped
    }

    @Test
    void unsupportedParameterDropsFeatureAndStaysOnline() throws Exception {
        when(api.sendCommand(any())).thenAnswer(invocation -> {
            JsonObject cmd = invocation.getArgument(0);
            String method = cmd.get("method").getAsString();
            if (!"get".equals(method)) {
                return json("{\"error_code\":0}");
            }
            // C125 (no pan/tilt) rejects the preset module with -40101 ("parameter does not exist")
            if (cmd.has("preset")) {
                throw new TapoCameraApiException("parameter does not exist", -40101);
            }
            String module = cmd.keySet().stream().filter(k -> !"method".equals(k)).findFirst().orElseThrow();
            String section = cmd.getAsJsonObject(module).getAsJsonArray("name").get(0).getAsString();
            return json(
                    "{\"error_code\":0,\"result\":{\"" + module + "\":{\"" + section + "\":{\"enabled\":\"on\"}}}}");
        });
        handler.simulateInitialize();
        assertTrue(thingUpdate.await(10, TimeUnit.SECONDS), "channel update did not complete");
        awaitTerminalStatus();

        assertEquals(ThingStatus.ONLINE, lastStatus.getStatus());
        assertFalse(handler.getDetectedFeatures().contains(TapoCameraFeature.PRESETS));
        assertTrue(handler.getDetectedFeatures().contains(TapoCameraFeature.PRIVACY));
        verify(callback).thingUpdated(argThat(updatedThing -> updatedThing.getChannels().stream()
                .noneMatch(channel -> "presets".equals(channel.getUID().getGroupId()))));
    }

    @Test
    void unwrappedPollResponsesUpdateChannels() throws Exception {
        // Some firmwares (e.g. C125) return secured-command results without a "result" wrapper:
        // the module object sits at the top level. readSection must still extract the section.
        when(api.sendCommand(any())).thenAnswer(invocation -> {
            JsonObject cmd = invocation.getArgument(0);
            String method = cmd.get("method").getAsString();
            if (!"get".equals(method)) {
                return json("{\"error_code\":0}");
            }
            String module = cmd.keySet().stream().filter(k -> !"method".equals(k)).findFirst().orElseThrow();
            String section = cmd.getAsJsonObject(module).getAsJsonArray("name").get(0).getAsString();
            // unwrapped: module at top level, no "result" key
            return json("{\"error_code\":0,\"" + module + "\":{\"" + section + "\":{\"enabled\":\"on\"}}}");
        });
        handler.simulateInitialize();
        awaitTerminalStatus();

        assertEquals(OnOffType.ON, updatedStates.get("privacy#privacyMode"));
        assertEquals(OnOffType.ON, updatedStates.get("system#ledStatus"));
        assertEquals(OnOffType.ON, updatedStates.get("motionDetection#enabled"));
        assertEquals(ThingStatus.ONLINE, lastStatus.getStatus());
    }

    @Test
    void authFailureTriggersSingleReLoginThenRecovers() throws Exception {
        var calls = new AtomicInteger();
        when(api.sendCommand(any())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                throw new TapoCameraApiException("expired", -40401);
            }
            return json("{\"error_code\":0,\"result\":{\"led\":{\"config\":{\"enabled\":\"on\"}}}}");
        });
        handler.simulateInitialize();
        awaitTerminalStatus();

        verify(api, times(2)).login(); // initial + bounded retry within same poll
        assertEquals(ThingStatus.ONLINE, lastStatus.getStatus());
    }

    @Test
    void privacyCommandSendsLensSetThenReadsBack() throws Exception {
        respondWith(Map.of("lens_mask#lens_mask_info", "{\"enabled\":\"on\"}", //
                "msg_alarm#chn1_msg_alarm_info", "{}", "system#last_alarm_info", "{}", //
                "motion_detection#motion_det", "{}", "led#config", "{}", "preset#preset", "{}"));
        handler.simulateInitialize();
        awaitTerminalStatus();
        updatedStates.clear();

        when(api.isLoggedIn()).thenReturn(true);
        var sent = new CopyOnWriteArrayList<JsonObject>();
        doAnswer(invocation -> {
            sent.add(invocation.getArgument(0));
            return json("{\"error_code\":0}");
        }).when(api).sendCommand(any());

        handler.processCommand(new ChannelUID(THING_UID, "privacy#privacyMode"), OnOffType.ON);

        var payloads = sent.stream().map(JsonObject::toString).collect(Collectors.toList());
        assertTrue(payloads.contains(
                "{\"method\":\"multipleRequest\",\"params\":{\"requests\":[{\"method\":\"setLensMaskConfig\",\"params\":{\"lens_mask\":{\"lens_mask_info\":{\"enabled\":\"on\"}}}}]}}"));
        assertTrue(payloads.stream().anyMatch(p -> p.startsWith("{\"method\":\"get\""))); // refresh read follows
    }

    @Test
    void motionSensitivityIgnoresNonNumericCommand() throws Exception {
        respondWith(Map.of("lens_mask#lens_mask_info", "{}", "msg_alarm#chn1_msg_alarm_info", "{}", //
                "system#last_alarm_info", "{}", "motion_detection#motion_det", "{}", //
                "led#config", "{}", "preset#preset", "{}"));
        handler.simulateInitialize();
        awaitTerminalStatus();
        when(api.isLoggedIn()).thenReturn(true);
        var sent = new CopyOnWriteArrayList<JsonObject>();
        doAnswer(invocation -> {
            sent.add(invocation.getArgument(0));
            return json("{\"error_code\":0}");
        }).when(api).sendCommand(any());

        handler.processCommand(new ChannelUID(THING_UID, "motionDetection#sensitivity"), IncreaseDecreaseType.INCREASE);

        assertTrue(sent.isEmpty()); // no command forwarded to the camera
    }

    @Test
    void alarmModeBothMapsToSoundAndLight() throws Exception {
        respondWith(Map.of("lens_mask#lens_mask_info", "{}", "msg_alarm#chn1_msg_alarm_info", "{}", //
                "system#last_alarm_info", "{}", "motion_detection#motion_det", "{}", //
                "led#config", "{}", "preset#preset", "{}"));
        when(api.isLoggedIn()).thenReturn(true);
        handler.simulateInitialize();
        awaitTerminalStatus();
        var sent = new CopyOnWriteArrayList<JsonObject>();
        doAnswer(invocation -> {
            sent.add(invocation.getArgument(0));
            return json("{\"error_code\":0}");
        }).when(api).sendCommand(any());

        handler.processCommand(new ChannelUID(THING_UID, "alarm#alarmMode"), new StringType("both"));

        assertTrue(sent.stream().map(JsonObject::toString)
                .anyMatch(p -> p.contains("\"alarm_mode\":[\"sound\",\"light\"]")));
    }

    @Test
    void manualAlarmCommandUpdatesStateOptimistically() throws Exception {
        respondWith(Map.of("lens_mask#lens_mask_info", "{}", "msg_alarm#chn1_msg_alarm_info", "{}", //
                "system#last_alarm_info", "{}", "motion_detection#motion_det", "{}", //
                "led#config", "{}", "preset#preset", "{}"));
        when(api.isLoggedIn()).thenReturn(true);
        handler.simulateInitialize();
        awaitTerminalStatus();
        doAnswer(invocation -> json("{\"error_code\":0}")).when(api).sendCommand(any());

        handler.processCommand(new ChannelUID(THING_UID, "alarm#manualAlarm"), OnOffType.ON);

        assertEquals(OnOffType.ON, updatedStates.get("alarm#manualAlarm"));
    }
}
