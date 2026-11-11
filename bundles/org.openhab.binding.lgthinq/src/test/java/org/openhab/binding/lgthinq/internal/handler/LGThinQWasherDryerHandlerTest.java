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
package org.openhab.binding.lgthinq.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.LG_API_PLATFORM_TYPE_V2;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lgthinq.handler.JsonUtils;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for the remote start payload and the state publication of the {@link LGThinQWasherDryerHandler}.
 *
 * @author Carlo Dischler - Initial contribution
 * @author Nemer Daud - Add state publication tests
 */
@NonNullByDefault
class LGThinQWasherDryerHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private @NonNullByDefault({}) WasherDryerCapability cap;
    private @NonNullByDefault({}) Map<String, Object> cmdTemplate;
    private final ThingUID thingUID = new ThingUID(THING_TYPE_WASHING_MACHINE, "test");
    private ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private LGThinQWasherDryerHandler handler = createHandler();

    @BeforeEach
    void setUp() throws JsonProcessingException, LGThinqException {
        cap = CapabilityFactory.getInstance().create(mapper.readTree(JsonUtils.loadJson("thinq-washer-v2-cap.json")),
                WasherDryerCapability.class);
        CommandDefinition cmd = Objects.requireNonNull(cap.getCommandsDefinition().get(cap.getCommandRemoteStart()));
        cmdTemplate = cmd.getData();
    }

    private WasherDryerSnapshot snapshot(String course) throws JsonProcessingException {
        Map<String, Object> raw = mapper.readValue("""
                {"courseFL24inchBaseTitan": "%s", "smartCourseFL24inchBaseTitan": "NOT_SELECTED",
                 "temp": "TEMP_60", "spin": "SPIN_1400", "rinse": "RINSE_NORMAL", "steam": "STEAM_ON",
                 "turboWash": null, "reserveTimeHour": 0.0, "reserveTimeMinute": 0.0}
                """.formatted(course), new TypeReference<HashMap<String, Object>>() {
        });
        WasherDryerSnapshot shot = new WasherDryerSnapshot();
        shot.setCourse(course);
        shot.setSmartCourse("NOT_SELECTED");
        shot.setRawData(raw);
        return shot;
    }

    @Test
    void unknownSmartCourseUsesTypeOfSelectedCourse() throws JsonProcessingException {
        WasherDryerSnapshot shot = snapshot("COTTON");
        shot.setSmartCourse("");

        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, shot, "COTTON",
                Map.of(), false);

        assertEquals("Course", data.get("courseType"));
        assertEquals("NOT_SELECTED", data.get("smartCourseFL24inchBaseTitan"));
    }

    @Test
    void keepsMachineSettingsForCurrentCourse() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "COTTON", Map.of(), false);

        assertEquals("COTTON", data.get("courseFL24inchBaseTitan"));
        assertEquals("TEMP_60", data.get("temp"));
        assertEquals("SPIN_1400", data.get("spin"));
        assertEquals("RINSE_NORMAL", data.get("rinse"));
        assertEquals("STEAM_ON", data.get("steam"));
        assertEquals("Course", data.get("courseType"));
        assertFalse(data.containsValue(null));
    }

    @Test
    void usesCourseDefaultsForOtherCourse() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "EASYCARE", Map.of(), false);

        assertEquals("EASYCARE", data.get("courseFL24inchBaseTitan"));
        assertEquals("TEMP_40", data.get("temp"));
        assertEquals("STEAM_OFF", data.get("steam"));
        // numeric course defaults must not replace the snapshot value with null
        assertEquals(0.0, data.get("reserveTimeHour"));
        assertFalse(data.containsValue(null));
    }

    @Test
    void selectedFeaturesOverrideSnapshot() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "COTTON", Map.of("temp", "TEMP_30", "spin", "SPIN_800"), false);

        assertEquals("TEMP_30", data.get("temp"));
        assertEquals("SPIN_800", data.get("spin"));
        assertEquals("RINSE_NORMAL", data.get("rinse"));
    }

    @Test
    void standbyUsesSettingsRememberedBeforeStandby() throws JsonProcessingException {
        WasherDryerSnapshot remembered = snapshot("COTTON");
        WasherDryerSnapshot rsShot = LGThinQWasherDryerHandler.selectRemoteStartShot(cap, snapshot("NOT_SELECTED"),
                remembered);
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, rsShot, "COTTON",
                Map.of(), false);

        assertSame(remembered, rsShot);
        assertEquals("TEMP_60", data.get("temp"));
        assertEquals("STEAM_ON", data.get("steam"));
    }

    @Test
    void currentSnapshotPreferredWhenCourseSelected() throws JsonProcessingException {
        WasherDryerSnapshot current = snapshot("EASYCARE");

        assertSame(current, LGThinQWasherDryerHandler.selectRemoteStartShot(cap, current, snapshot("COTTON")));
        assertSame(current, LGThinQWasherDryerHandler.selectRemoteStartShot(cap, current, null));
    }

    @Test
    void initialStateRequestsCourseFromCommand() throws JsonProcessingException {
        Map<String, Object> waiting = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate,
                snapshot("COTTON"), "COTTON", Map.of(), true);
        WasherDryerSnapshot pausedShot = snapshot("COTTON");
        pausedShot.getRawData().put("initialBit", "INITIAL_BIT_ON");
        Map<String, Object> paused = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, pausedShot,
                "COTTON", Map.of(), false);

        assertEquals("INITIAL_BIT_ON", waiting.get("initialBit"));
        assertEquals("INITIAL_BIT_OFF", paused.get("initialBit"));
    }

    @Test
    void missingSnapshotValuesUseCourseDefaultsThenTemplate() throws JsonProcessingException {
        Map<String, Object> data = LGThinQWasherDryerHandler.buildRemoteStartData(cap, cmdTemplate, snapshot("COTTON"),
                "COTTON", Map.of(), false);

        assertEquals("SOILWASH_NORMAL", data.get("soilWash"));
        assertEquals("TURBOWASH_OFF", data.get("turboWash"));
        assertEquals("CREASECARE_OFF", data.get("creaseCare"));
    }

    @BeforeEach
    void setUpHandler() {
        callback = mock(ThingHandlerCallback.class);
        handler = createHandler();
        handler.setCallback(callback);
    }

    @Test
    void publishesCurrentPreviousAndProcessStatesIndependently() throws LGThinqApiException {
        WasherDryerSnapshot snapshot = new WasherDryerSnapshot();
        snapshot.setState("RUNNING");
        snapshot.setPreviousState("DETECTING");
        snapshot.setProcessState("WASHING");

        // the remote start handling of updateDeviceChannels reads the capability
        handler.thinQCapability = cap;
        handler.updateDeviceChannels(snapshot);

        verify(callback).stateUpdated(channel(CHANNEL_WMD_STATE_ID), new StringType("RUNNING"));
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PREVIOUS_STATE_ID), new StringType("DETECTING"));
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PROCESS_STATE_ID), new StringType("WASHING"));
    }

    @Test
    void publishesUndefWhenProcessStateIsUnavailable() throws LGThinqApiException {
        WasherDryerSnapshot snapshot = new WasherDryerSnapshot();
        snapshot.setState("RUNNING");
        snapshot.setPreviousState("DETECTING");

        // the remote start handling of updateDeviceChannels reads the capability
        handler.thinQCapability = cap;
        handler.updateDeviceChannels(snapshot);

        verify(callback).stateUpdated(channel(CHANNEL_WMD_STATE_ID), new StringType("RUNNING"));
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PREVIOUS_STATE_ID), new StringType("DETECTING"));
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PROCESS_STATE_ID), UnDefType.UNDEF);
    }

    @Test
    void clearsDashboardChannelsWhenDeviceDisconnects() {
        handler.onDeviceDisconnected();

        verify(callback).stateUpdated(channel(CHANNEL_AC_POWER_ID), OnOffType.OFF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_STATE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PREVIOUS_STATE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_PROCESS_STATE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_COURSE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_TEMP_LEVEL_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_DOOR_LOCK_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(channel(CHANNEL_WMD_REMAIN_TIME_ID), UnDefType.UNDEF);
    }

    private LGThinQWasherDryerHandler createHandler() {
        Thing thing = ThingBuilder.create(THING_TYPE_WASHING_MACHINE, thingUID)
                .withProperties(Map.of(PROP_INFO_PLATFORM_TYPE, LG_API_PLATFORM_TYPE_V2)).build();
        return new LGThinQWasherDryerHandler(thing, mock(LGThinQStateDescriptionProvider.class),
                mock(ItemChannelLinkRegistry.class), mock(HttpClientFactory.class));
    }

    private ChannelUID channel(String channelId) {
        return new ChannelUID(thingUID, CHANNEL_DASHBOARD_GRP_ID + "#" + channelId);
    }
}
