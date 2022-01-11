/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.api.responses.SceneCollections.SceneCollection;
import org.openhab.binding.hdpowerview.internal.api.responses.Scenes.Scene;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents;
import org.openhab.binding.hdpowerview.internal.api.responses.ScheduledEvents.ScheduledEvent;
import org.openhab.binding.hdpowerview.internal.builders.AutomationChannelBuilder;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;

/**
 * Unit tests for {@link AutomationChannelBuilder}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class AutomationChannelBuilderTest {

    private static final ChannelGroupUID CHANNEL_GROUP_UID = new ChannelGroupUID(
            new ThingUID(HDPowerViewBindingConstants.BINDING_ID, AutomationChannelBuilderTest.class.getSimpleName()),
            HDPowerViewBindingConstants.CHANNELTYPE_AUTOMATION_ENABLED);

    private static final HDPowerViewTranslationProvider translationProvider = new HDPowerViewTranslationProvider(
            mock(Bundle.class), new TranslationProviderForTests(), new LocaleProviderForTests());
    private AutomationChannelBuilder builder = AutomationChannelBuilder.create(translationProvider, CHANNEL_GROUP_UID);
    private List<Scene> scenes = new ArrayList<>();
    private List<SceneCollection> sceneCollections = new ArrayList<>();

    @BeforeEach
    private void setUp() {
        builder = AutomationChannelBuilder.create(translationProvider, CHANNEL_GROUP_UID);

        Scene scene = new Scene();
        scene.id = 1;
        scene.name = Base64.getEncoder().encodeToString(("TestScene").getBytes());
        scenes = new ArrayList<>(List.of(scene));

        SceneCollection sceneCollection = new SceneCollection();
        sceneCollection.id = 2;
        sceneCollection.name = Base64.getEncoder().encodeToString(("TestSceneCollection").getBytes());
        sceneCollections = new ArrayList<>(List.of(sceneCollection));
    }

    @Test
    public void sceneSunriseWeekends() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        scheduledEvent.daySaturday = true;
        scheduledEvent.daySunday = true;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, At sunrise, Weekends", channels.get(0).getLabel());
    }

    @Test
    public void sceneSunsetWeekdays() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNSET);
        scheduledEvent.dayMonday = true;
        scheduledEvent.dayTuesday = true;
        scheduledEvent.dayWednesday = true;
        scheduledEvent.dayThursday = true;
        scheduledEvent.dayFriday = true;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, At sunset, Weekdays", channels.get(0).getLabel());
    }

    @Test
    public void sceneTimeAllDays() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_TIME);
        scheduledEvent.dayMonday = true;
        scheduledEvent.dayTuesday = true;
        scheduledEvent.dayWednesday = true;
        scheduledEvent.dayThursday = true;
        scheduledEvent.dayFriday = true;
        scheduledEvent.daySaturday = true;
        scheduledEvent.daySunday = true;
        scheduledEvent.hour = 6;
        scheduledEvent.minute = 30;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, 06:30, All days", channels.get(0).getLabel());
    }

    @Test
    public void sceneMinutesBeforeSunriseMondayTuesday() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        scheduledEvent.dayMonday = true;
        scheduledEvent.dayTuesday = true;
        scheduledEvent.minute = -15;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, 15m before sunrise, Mon, Tue", channels.get(0).getLabel());
    }

    @Test
    public void sceneHoursMinutesAfterSunriseMonday() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        scheduledEvent.dayMonday = true;
        scheduledEvent.minute = 61;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, 1hr 1m after sunrise, Mon", channels.get(0).getLabel());
    }

    @Test
    public void sceneMinutesBeforeSunsetWednesdayThursdayFriday() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNSET);
        scheduledEvent.dayWednesday = true;
        scheduledEvent.dayThursday = true;
        scheduledEvent.dayFriday = true;
        scheduledEvent.minute = -59;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, 59m before sunset, Wed, Thu, Fri", channels.get(0).getLabel());
    }

    @Test
    public void sceneHourAfterSunsetFridaySaturdaySunday() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNSET);
        scheduledEvent.dayFriday = true;
        scheduledEvent.daySaturday = true;
        scheduledEvent.daySunday = true;
        scheduledEvent.minute = 60;

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals("TestScene, 1hr after sunset, Fri, Sat, Sun", channels.get(0).getLabel());
    }

    @Test
    public void sceneCollection() {
        ScheduledEvent scheduledEvent = createScheduledEventWithSceneCollection(
                ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);

        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withSceneCollections(sceneCollections).withScheduledEvents(scheduledEvents)
                .build();

        assertEquals(1, channels.size());
        assertEquals("TestSceneCollection, At sunrise, ", channels.get(0).getLabel());
    }

    @Test
    public void suppliedListIsUsed() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> existingChannels = new ArrayList<>(0);
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents)
                .withChannels(existingChannels).build();

        assertEquals(existingChannels, channels);
    }

    @Test
    public void emptyListWhenNoScheduledEvents() {
        List<Channel> channels = builder.build();

        assertEquals(0, channels.size());
    }

    @Test
    public void emptyListWhenNoScenesOrSceneCollections() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScheduledEvents(scheduledEvents).build();

        assertEquals(0, channels.size());
    }

    @Test
    public void emptyListWhenNoSceneForScheduledEvent() {
        ScheduledEvent scheduledEvent = createScheduledEventWithSceneCollection(
                ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(0, channels.size());
    }

    @Test
    public void emptyListWhenNoSceneCollectionForScheduledEvent() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));

        List<Channel> channels = builder.withSceneCollections(sceneCollections).withScheduledEvents(scheduledEvents)
                .build();

        assertEquals(0, channels.size());
    }

    @Test
    public void groupAndIdAreCorrect() {
        ScheduledEvent scheduledEvent = createScheduledEventWithScene(ScheduledEvents.SCHEDULED_EVENT_TYPE_SUNRISE);
        scheduledEvent.id = 42;
        List<ScheduledEvent> scheduledEvents = new ArrayList<>(List.of(scheduledEvent));
        List<Channel> channels = builder.withScenes(scenes).withScheduledEvents(scheduledEvents).build();

        assertEquals(1, channels.size());
        assertEquals(CHANNEL_GROUP_UID.getId(), channels.get(0).getUID().getGroupId());
        assertEquals(Integer.toString(scheduledEvent.id), channels.get(0).getUID().getIdWithoutGroup());
    }

    private ScheduledEvent createScheduledEventWithScene(int eventType) {
        ScheduledEvent scheduledEvent = new ScheduledEvent();
        scheduledEvent.id = 1;
        scheduledEvent.sceneId = scenes.get(0).id;
        scheduledEvent.eventType = eventType;
        return scheduledEvent;
    }

    private ScheduledEvent createScheduledEventWithSceneCollection(int eventType) {
        ScheduledEvent scheduledEvent = new ScheduledEvent();
        scheduledEvent.id = 1;
        scheduledEvent.sceneCollectionId = sceneCollections.get(0).id;
        scheduledEvent.eventType = eventType;
        return scheduledEvent;
    }
}
