/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.handler.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.Event
import org.eclipse.smarthome.core.events.EventSubscriber
import org.eclipse.smarthome.core.items.events.AbstractItemEventSubscriber
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.types.RefreshType
import org.eclipse.smarthome.core.types.State
import org.junit.Test
import org.openhab.binding.astro.AstroBindingConstants
import org.openhab.binding.astro.test.AstroOSGiTest
import org.openhab.binding.astro.test.AstroOSGiTest.AcceptedItemType

/**
 * OSGi test for the {@link AstroThingHandler}
 *
 * This class tests the commands for the astro thing.
 *
 * @author Petar Valchev
 *
 */
class AstroCommandTest extends AstroOSGiTest {
    // a listener for item state update events
    private EventSubscriberMock eventSubscriberMock

    @Test
    public void 'refresh command updates the state of the channels'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        initialize(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration)

        waitForAssert({
            assertThat "The status of the thing $astroThing was not as expected",
                    astroThing.getStatus(),
                    is(equalTo(ThingStatus.ONLINE))
        })

        eventSubscriberMock = new EventSubscriberMock()
        registerService(eventSubscriberMock, EventSubscriber.class.getName())

        ChannelUID testItemChannelUID = getChannelUID(DEFAULT_TEST_CHANNEL_ID)

        astroHandler.handleCommand(testItemChannelUID, RefreshType.REFRESH)
        waitForAssert({
            assertThat "An event for update of channel was not received",
                    eventSubscriberMock.isEventReceived,
                    is(true)
        })
    }

    private class EventSubscriberMock extends AbstractItemEventSubscriber{
        public boolean isEventReceived = false

        @Override
        public void receive(Event event) {
            String expectedEventSource = "$AstroBindingConstants.BINDING_ID:$AstroBindingConstants.SUN:$TEST_SUN_THING_ID:$DEFAULT_TEST_CHANNEL_ID".toString()
            if(event.getSource().equals(expectedEventSource)){
                isEventReceived = true
            }
        }
    }
}
