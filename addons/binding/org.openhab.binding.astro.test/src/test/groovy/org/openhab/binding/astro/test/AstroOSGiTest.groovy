/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.Item
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.DateTimeItem
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeMigrationService
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.openhab.binding.astro.AstroBindingConstants
import org.openhab.binding.astro.handler.AstroThingHandler
import org.openhab.binding.astro.internal.AstroHandlerFactory

/**
 * The base class for the OSGi tests for the Astro Binding.
 *
 * This class is extended by the classes that test {@link AstroThingHandler} and {@link AstroDiscoveryService}. 
 *
 * @author Petar Valchev
 *
 */
class AstroOSGiTest extends OSGiTest {
    protected ManagedThingProvider managedThingProvider
    protected ThingRegistry thingRegistry
    protected ItemRegistry itemRegistry

    protected static final String TEST_SUN_THING_ID = "testSunThingId"
    protected static final String TEST_MOON_THING_ID = "testMoonThingId"
    protected final String TEST_ITEM_NAME = "testItem"

    protected final String DEFAULT_TEST_CHANNEL_ID = "rise#start"
    protected final String GEOLOCATION_PROPERTY = "geolocation"
    protected final String GEOLOCATION_VALUE = "51.2,25.4"

    protected AstroThingHandler astroHandler
    protected Thing astroThing
    protected GenericItem testItem

    private static int refreshJobsDelay

    public enum AcceptedItemType{
        DATE_TIME("DateTime"), STRING("String"), NUMBER("Number");

        private String acceptedItemType

        public AcceptedItemType(String acceptedItemType){
            this.acceptedItemType = acceptedItemType
        }

        public String getAcceptedItemType(){
            return acceptedItemType
        }
    }

    @BeforeClass
    public static void setUpClass(){
        refreshJobsDelay = AstroThingHandler.refreshJobsDelay
        AstroThingHandler.refreshJobsDelay = 0
    }

    @Before
    public void setUp(){
        VolatileStorageService volatileStorageService = new VolatileStorageService()
        registerService(volatileStorageService)

        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat "Could not get ManagedThingProvider",
                managedThingProvider,
                is(notNullValue())

        thingRegistry = getService(ThingRegistry)
        assertThat "Could not get ThingRegistry",
                thingRegistry,
                is(notNullValue())

        itemRegistry = getService(ItemRegistry)
        assertThat "Could not get ItemRegistry",
                itemRegistry,
                is(notNullValue())
    }

    @After
    public void tearDown(){
        if(astroThing != null){
            Thing removedThing = thingRegistry.forceRemove(astroThing.getUID())
            assertThat("The sun thing was not deleted",
                    removedThing,
                    is(notNullValue()))
        }

        if(testItem != null) {
            Item removedItem = itemRegistry.remove(TEST_ITEM_NAME)
            assertThat("The thing cannot be deleted", 
                removedItem, 
                is(notNullValue()))
        }
    }

    @AfterClass
    public static void tearDownClass(){
        AstroThingHandler.refreshJobsDelay = refreshJobsDelay
    }

    protected ChannelUID getChannelUID(String channelId){
        Channel channel = astroThing.getChannel(channelId)
        assertThat "Could not get channel $channelId",
                channel,
                is(notNullValue())

        ChannelUID channelUID = channel.getUID()
        assertThat "Could not get the UID of the channel $channelId",
                channelUID,
                is(notNullValue())

        return channelUID
    }

    protected State getItemState(){
        Item item
        waitForAssert({
            item = itemRegistry.getItem(TEST_ITEM_NAME)
            assertThat "Could not get item $TEST_ITEM_NAME from the item registry",
                    item,
                    is(notNullValue())
        })

        State itemState
        waitForAssert({
            itemState = item.getState()
            assertThat "The state of the item $TEST_ITEM_NAME was not as expected",
                    itemState,
                    is(not(UnDefType.NULL))
        })

        return itemState
    }

    protected void assertThingStatus(String thingID, String channelID, AcceptedItemType acceptedItemType, Configuration thingConfiguration, ThingStatus expectedThingStatus){
        initialize(thingID, channelID, acceptedItemType, thingConfiguration)

        waitForAssert({
            assertThat "The status of the thing $astroThing was not as expected",
                    astroThing.getStatus(),
                    is(equalTo(expectedThingStatus))
        })
    }

    protected void initialize(String thingID, String channelId, AcceptedItemType acceptedItemType, Configuration thingConfiguration){
        ThingUID astroThingUid
        switch(thingID) {
            case (TEST_SUN_THING_ID) :
                astroThingUid = new ThingUID(AstroBindingConstants.THING_TYPE_SUN, TEST_SUN_THING_ID)
                break
            case(TEST_MOON_THING_ID) :
                astroThingUid = new ThingUID(AstroBindingConstants.THING_TYPE_MOON, TEST_MOON_THING_ID)
                break
        }

        ChannelUID channelUID = new ChannelUID(astroThingUid, channelId)
        Channel channel = new Channel(channelUID, acceptedItemType.getAcceptedItemType(), null)

        if(thingID.equals(TEST_SUN_THING_ID)){
            astroThing =  ThingBuilder.create(AstroBindingConstants.THING_TYPE_SUN, astroThingUid)
                    .withConfiguration(thingConfiguration)
                    .withChannel(channel)
                    .build()
        } else if(thingID.equals(TEST_MOON_THING_ID)){
            astroThing =  ThingBuilder.create(AstroBindingConstants.THING_TYPE_MOON, astroThingUid)
                    .withConfiguration(thingConfiguration)
                    .withChannel(channel)
                    .build()
        }

        managedThingProvider.add(astroThing)

        waitForAssert({
            if(thingID.equals(TEST_SUN_THING_ID)){
                astroHandler = getThingHandler(ThingHandler.class)
            } else if(thingID.equals(TEST_MOON_THING_ID)){
                astroHandler = getThingHandler(ThingHandler.class)
            }
            assertThat "Could not get NtpHandler",
                    astroHandler,
                    is(notNullValue())
        })

        if(acceptedItemType.equals(AcceptedItemType.DATE_TIME)){
            testItem = new DateTimeItem(TEST_ITEM_NAME)
        } else if(acceptedItemType.equals(AcceptedItemType.NUMBER)){
            testItem = new NumberItem(TEST_ITEM_NAME)
        } else if(acceptedItemType.equals(AcceptedItemType.STRING)){
            testItem = new StringItem(TEST_ITEM_NAME)
        }
        itemRegistry.add(testItem)

        ManagedItemChannelLinkProvider itemChannelLinkProvider

        waitForAssert({
            itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
            assertThat "Could not get ManagedItemChannelLinkProvider",
                    itemChannelLinkProvider,
                    is(notNullValue())
        })
        
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_ITEM_NAME, channelUID))
    }
    
    /**
     * Gets a thing handler of a specific type.
     *
     * @param clazz type of thing handler
     *
     * @return the thing handler
     */
    protected <T extends ThingHandler> T getThingHandler(Class<T> clazz){
        AstroHandlerFactory factory
        waitForAssert{
            factory = getService(ThingHandlerFactory, AstroHandlerFactory)
            assertThat factory, is(notNullValue())
        }
        def handlers = getThingHandlers(factory)

        for(ThingHandler handler : handlers) {
            if(clazz.isInstance(handler)) {
                return handler
            }
        }
        return null
    }

    private Set<ThingHandler> getThingHandlers(AstroHandlerFactory factory) {
        def thingManager = getService(ThingTypeMigrationService.class, { "org.eclipse.smarthome.core.thing.internal.ThingManager" } )
        assertThat thingManager, not(null)
        thingManager.thingHandlersByFactory.get(factory)
    }
}
