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
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.types.State
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.openhab.binding.astro.internal.model.Moon
import org.openhab.binding.astro.internal.model.Planet
import org.openhab.binding.astro.internal.model.Sun
import org.openhab.binding.astro.internal.util.PropertyUtils
import org.openhab.binding.astro.test.AstroOSGiTest
import org.openhab.binding.astro.test.AstroOSGiTest.AcceptedItemType
import org.openhab.binding.astro.test.cases.AstroParametrizedTestCases

/**
 * OSGi tests for the {@link AstroThingHandler}
 *
 * This class tests if the state of the astro channels is updated.
 *
 * @author Petar Valchev
 *
 */
@RunWith(Parameterized.class)
class AstroStateUpdateTest extends AstroOSGiTest {

    private String thingID
    private String channelId
    private AcceptedItemType acceptedItemType

    public AstroStateUpdateTest(String thingID, String channelId, AcceptedItemType acceptedItemType){
        this.thingID = thingID
        this.channelId = channelId
        this.acceptedItemType = acceptedItemType
    }

    @Parameters
    public static Collection<Object[]> data() {
        AstroParametrizedTestCases cases = new AstroParametrizedTestCases()
        cases.getCases()
    }

    @Test
    public void testParametrized(){
        assertStateUpdate(thingID, channelId, acceptedItemType)
    }
    
    private void assertStateUpdate(String thingID, String channelId, AcceptedItemType acceptedItemType){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)

        initialize(thingID, channelId, acceptedItemType, thingConfiguration)

        Planet planet
        waitForAssert({
            planet = astroHandler.getPlanet()
            switch(thingID) {
                case (TEST_SUN_THING_ID) :
                    assertThat "The planet was not an instance of the expected class",
                    planet,
                    is(instanceOf(Sun))
                    break
                case(TEST_MOON_THING_ID) :
                    assertThat "The planet was not an instance of the expected class",
                    planet,
                    is(instanceOf(Moon))
                    break
            }
        })

        ChannelUID testItemChannelUID = getChannelUID(channelId)

        State expectedState = PropertyUtils.getState(testItemChannelUID, planet)

        State stateFromItemRegistry

        waitForAssert({
            stateFromItemRegistry = getItemState()
            assertThat "The state from the item registry was not as expected",
                    stateFromItemRegistry,
                    is(equalTo(expectedState))
        })
    }
}
