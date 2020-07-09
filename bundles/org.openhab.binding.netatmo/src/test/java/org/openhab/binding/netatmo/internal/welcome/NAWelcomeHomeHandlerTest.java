/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.welcome;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.internal.ThingImpl;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.netatmo.internal.NetatmoBindingConstants;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.webhook.NAWebhookCameraEvent;

import io.swagger.client.model.NAWelcomeEvent;
import io.swagger.client.model.NAWelcomeHome;
import io.swagger.client.model.NAWelcomeHomeData;
import io.swagger.client.model.NAWelcomeSubEvent;

/**
 * @author Sven Strohschein - Initial contribution
 */
@RunWith(MockitoJUnitRunner.class)
public class NAWelcomeHomeHandlerTest {

    private static final String DUMMY_HOME_ID = "1";

    @Mock
    private TimeZoneProvider timeZoneProviderMock;
    private Thing welcomeHomeThing;
    private NAWelcomeHomeHandlerAccessible handler;
    @Mock
    private NetatmoBridgeHandler bridgeHandlerMock;

    @Before
    public void before() {
        welcomeHomeThing = new ThingImpl(new ThingTypeUID("netatmo", "NAWelcomeHome"), "1");
        handler = new NAWelcomeHomeHandlerAccessible(welcomeHomeThing);
    }

    @Test
    public void testUpdateReadingsWithEvents() {
        NAWelcomeEvent event1 = createEvent(1592661881, NAWebhookCameraEvent.EventTypeEnum.PERSON);
        NAWelcomeEvent event2 = createEvent(1592661882, NAWebhookCameraEvent.EventTypeEnum.MOVEMENT);

        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);
        home.setEvents(Arrays.asList(event1, event2));

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(Optional.of(homeData));

        handler.updateReadings();

        // the second (last) event is expected
        assertEquals(new StringType("movement"),
                handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));

        home.setEvents(Arrays.asList(event2, event1));
        // the second (last) event is still expected (independent from the order of these are added)
        assertEquals(new StringType("movement"),
                handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadingsWith1Event() {
        NAWelcomeEvent event = createEvent(1592661881, NAWebhookCameraEvent.EventTypeEnum.PERSON);

        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);
        home.setEvents(Collections.singletonList(event));

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(Optional.of(homeData));

        handler.updateReadings();

        assertEquals(new StringType("person"),
                handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadingsNoEvents() {
        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(Optional.of(homeData));

        handler.updateReadings();

        assertEquals(UnDefType.UNDEF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadingsEmptyHomeData() {
        NAWelcomeHomeData homeData = new NAWelcomeHomeData();

        when(bridgeHandlerMock.getWelcomeDataBody(any())).thenReturn(Optional.of(homeData));

        handler.updateReadings();

        assertEquals(UnDefType.UNDEF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadingsNoHomeData() {
        handler.updateReadings();

        assertEquals(UnDefType.UNDEF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testTriggerChannelIfRequired() {
        NAWelcomeEvent event1 = createPresenceEvent(1592661881, NAWelcomeSubEvent.TypeEnum.ANIMAL);
        NAWelcomeEvent event2 = createPresenceEvent(1592661882, NAWelcomeSubEvent.TypeEnum.HUMAN);
        NAWelcomeEvent event3 = createEvent(1592661883, NAWebhookCameraEvent.EventTypeEnum.MOVEMENT);

        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);
        home.setEvents(Collections.singletonList(event1));

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(Optional.of(homeData));

        handler.updateReadings();
        handler.triggerChannelIfRequired(NetatmoBindingConstants.CHANNEL_CAMERA_EVENT);

        // No triggered event is expected, because the binding is just started (with existing events).
        assertEquals(0, handler.getTriggerChannelCount());

        home.setEvents(Arrays.asList(event1, event2));

        handler.updateReadings();
        handler.triggerChannelIfRequired(NetatmoBindingConstants.CHANNEL_CAMERA_EVENT);

        // 1 triggered event is expected, because there is 1 new event since binding start (outdoor / detected human).
        assertEquals(1, handler.getTriggerChannelCount());
        assertEquals(new StringType("outdoor"),
                handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));

        home.setEvents(Arrays.asList(event1, event2));

        handler.updateReadings();
        handler.triggerChannelIfRequired(NetatmoBindingConstants.CHANNEL_CAMERA_EVENT);

        // No new triggered event is expected, because there are still the same events as before the refresh.
        assertEquals(1, handler.getTriggerChannelCount());
        assertEquals(new StringType("outdoor"),
                handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));

        home.setEvents(Arrays.asList(event1, event2, event3));

        handler.updateReadings();
        handler.triggerChannelIfRequired(NetatmoBindingConstants.CHANNEL_CAMERA_EVENT);

        // 1 new triggered event is expected (2 in sum), because there is 1 new event since the last triggered event
        // (movement after outdoor / detected human).
        assertEquals(2, handler.getTriggerChannelCount());
        assertEquals(new StringType("movement"),
                handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    private static NAWelcomeEvent createPresenceEvent(int eventTime, NAWelcomeSubEvent.TypeEnum detectedObjectType) {
        NAWelcomeSubEvent subEvent = new NAWelcomeSubEvent();
        subEvent.setTime(eventTime);
        subEvent.setType(detectedObjectType);

        NAWelcomeEvent event = createEvent(eventTime, NAWebhookCameraEvent.EventTypeEnum.OUTDOOR);
        event.setEventList(Collections.singletonList(subEvent));
        return event;
    }

    private static NAWelcomeEvent createEvent(int eventTime, NAWebhookCameraEvent.EventTypeEnum eventType) {
        NAWelcomeEvent event = new NAWelcomeEvent();
        event.setType(eventType.toString());
        event.setTime(eventTime);
        return event;
    }

    private class NAWelcomeHomeHandlerAccessible extends NAWelcomeHomeHandler {

        private int triggerChannelCount;

        private NAWelcomeHomeHandlerAccessible(Thing thing) {
            super(thing, timeZoneProviderMock);
        }

        @Override
        protected Optional<NetatmoBridgeHandler> getBridgeHandler() {
            return Optional.of(bridgeHandlerMock);
        }

        @Override
        protected String getId() {
            return DUMMY_HOME_ID;
        }

        @Override
        protected void triggerChannel(@NonNull String channelID, @NonNull String event) {
            triggerChannelCount++;
            super.triggerChannel(channelID, event);
        }

        private int getTriggerChannelCount() {
            return triggerChannelCount;
        }
    }
}
