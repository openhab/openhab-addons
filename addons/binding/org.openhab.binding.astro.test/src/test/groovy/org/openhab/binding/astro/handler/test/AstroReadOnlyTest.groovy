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
import org.eclipse.smarthome.core.library.types.DateTimeType
import org.eclipse.smarthome.core.types.State
import org.junit.Test
import org.openhab.binding.astro.test.AstroOSGiTest
import org.openhab.binding.astro.test.AstroOSGiTest.AcceptedItemType

/**
 * OSGi tests for the {@link AstroThingHandler}
 *
 * This class tests the handleUpdate method in the {@link AstroThingHandler}.
 *
 * @author Petar Valchev
 *
 */
class AstroReadOnlyTest extends AstroOSGiTest {
    @Test
    public void 'handleUpdate method does not update the state of sun thing'(){
        assertNoUpdate(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME)
    }
    
    @Test
    public void 'handleUpdate method does not update the state of moon thing'(){
        assertNoUpdate(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME)
    }
    
    private void assertNoUpdate(String thingID, String channelId, AcceptedItemType acceptedItemType){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)

        initialize(thingID, channelId, acceptedItemType, thingConfiguration)

        State stateBeforeUpdate = getItemState()

        State stateToUpdate = new DateTimeType()
        astroHandler.handleUpdate(astroThing.getChannel(channelId).getUID(), stateToUpdate)

        State stateAfterUpdate = getItemState()

        assertThat "The state of the item was not as expected",
                stateAfterUpdate,
                is(equalTo(stateBeforeUpdate))
    }
}
