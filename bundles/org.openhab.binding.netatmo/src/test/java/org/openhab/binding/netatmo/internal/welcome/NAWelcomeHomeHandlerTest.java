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

import io.swagger.client.model.NAWelcomeEvent;
import io.swagger.client.model.NAWelcomeHome;
import io.swagger.client.model.NAWelcomeHomeData;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Sven Strohschein
 */
@RunWith(MockitoJUnitRunner.class)
public class NAWelcomeHomeHandlerTest {

    private static final String DUMMY_HOME_ID = "1";

    @Mock
    private TimeZoneProvider timeZoneProviderMock;
    private Thing welcomeHomeThing;
    private NAWelcomeHomeHandler handler;
    @Mock
    private NetatmoBridgeHandler bridgeHandlerMock;

    @Before
    public void before() {
        welcomeHomeThing = new ThingImpl(new ThingTypeUID("netatmo", "NAWelcomeHome"), "1");
        handler = new NAWelcomeHomeHandler(welcomeHomeThing, timeZoneProviderMock) {
            @Override
            protected NetatmoBridgeHandler getBridgeHandler() {
                return bridgeHandlerMock;
            }

            @Override
            protected String getId() {
                return DUMMY_HOME_ID;
            }
        };
    }

    @Test
    public void testUpdateReadings_with_Events() {
        NAWelcomeEvent event_1 = new NAWelcomeEvent();
        event_1.setType(NAWebhookCameraEvent.EventTypeEnum.PERSON.toString());
        event_1.setTime(1592661881);

        NAWelcomeEvent event_2 = new NAWelcomeEvent();
        event_2.setType(NAWebhookCameraEvent.EventTypeEnum.MOVEMENT.toString());
        event_2.setTime(1592661882);

        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);
        home.setEvents(Arrays.asList(event_1, event_2));

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(homeData);

        handler.updateReadings();

        //the second (last) event is expected
        assertEquals(new StringType("movement"), handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));

        home.setEvents(Arrays.asList(event_2, event_1));
        //the second (last) event is still expected (independent from the order of these are added)
        assertEquals(new StringType("movement"), handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));

    }

    @Test
    public void testUpdateReadings_with_1_Event() {
        NAWelcomeEvent event = new NAWelcomeEvent();
        event.setType(NAWebhookCameraEvent.EventTypeEnum.PERSON.toString());

        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);
        home.setEvents(Collections.singletonList(event));

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(homeData);

        handler.updateReadings();

        assertEquals(new StringType("person"), handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadings_no_Events() {
        NAWelcomeHome home = new NAWelcomeHome();
        home.setId(DUMMY_HOME_ID);

        NAWelcomeHomeData homeData = new NAWelcomeHomeData();
        homeData.setHomes(Collections.singletonList(home));

        when(bridgeHandlerMock.getWelcomeDataBody(DUMMY_HOME_ID)).thenReturn(homeData);

        handler.updateReadings();

        assertEquals(UnDefType.UNDEF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadings_empty_HomeData() {
        when(bridgeHandlerMock.getWelcomeDataBody(any())).thenReturn(new NAWelcomeHomeData());

        handler.updateReadings();

        assertEquals(UnDefType.UNDEF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }

    @Test
    public void testUpdateReadings_no_HomeData() {
        handler.updateReadings();

        assertEquals(UnDefType.UNDEF, handler.getNAThingProperty(NetatmoBindingConstants.CHANNEL_WELCOME_EVENT_TYPE));
    }
}
