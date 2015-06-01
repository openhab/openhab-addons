/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.events

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.items.ItemProvider;
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test
import org.openhab.core.library.types.StringType
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;


/**
 * Tests for {@link EventDelegate}.
 *
 * @author Kai Kreuzer - Initial version
 * @since 2.0
 */
class EventDelegateTest extends OSGiTest {

    def state

    EventPublisher publisher

    org.eclipse.smarthome.core.events.EventSubscriber eventSubscriber = new org.eclipse.smarthome.core.events.AbstractEventSubscriber() {
        @Override
        public void receiveUpdate(String itemName, org.eclipse.smarthome.core.types.State newState) {
            state = newState
        }
    }
    
    @Before
    void setUp() {
        registerService([
            getAll: {
                [
                    new StringItem("Test")
                ]
            },
            addProviderChangeListener: {},
            removeProviderChangeListener: {},
            allItemsChanged: {}] as ItemProvider)
        registerService(eventSubscriber, org.osgi.service.event.EventHandler.class.name, ["event.topics":"smarthome/*"] as Hashtable)
        publisher = getService(org.openhab.core.events.EventPublisher, EventPublisherDelegate)
        assertThat publisher, is(notNullValue())
    }

    @Test
    void testMapUnDefType() {
        publisher.postUpdate("Test", new StringType("ABC"))
        waitFor ( { state != null }, 2000)
        assertEquals "ABC", state.toString()

        publisher.postUpdate("Test", UnDefType.NULL)
        waitFor ( { state != null }, 2000)
        assertEquals org.eclipse.smarthome.core.types.UnDefType.NULL, state

        publisher.postUpdate("Test", UnDefType.UNDEF)
        waitFor ( { state != null }, 2000)
        assertEquals org.eclipse.smarthome.core.types.UnDefType.UNDEF, state
    }
}
