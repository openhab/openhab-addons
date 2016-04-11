/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.items.GenericItem
import org.eclipse.smarthome.core.items.ItemRegistry
import org.eclipse.smarthome.core.library.items.NumberItem
import org.eclipse.smarthome.core.library.items.StringItem
import org.eclipse.smarthome.core.library.types.DecimalType
import org.eclipse.smarthome.core.library.types.StringType
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.link.ItemChannelLink
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider
import org.eclipse.smarthome.core.types.State
import org.eclipse.smarthome.core.types.UnDefType
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openhab.binding.systeminfo.SysteminfoBindingConstants
import org.openhab.binding.systeminfo.handler.SysteminfoHandler
/**
 * OSGi tests for the {@link SysteminfoHandler}
 *
 * @author Svilen Valkanov
 *
 */
class SysteminfoOSGiTest extends OSGiTest{
    def thingName = "work";
    def ITEM_NAME = "test"
    Thing systemInfoThing;

    ManagedThingProvider managedThingProvider;
    ThingRegistry thingRegistry;
    ItemRegistry itemRegistry;

    /**
     * Refresh time in seconds for tasks with priority High.
     * Default value for the parameter interval_high in the thing configuration
     */
    int DEFAULT_TEST_INTERVAL_HIGH = 1
    /**
     * Refresh time in seconds for tasks with priority Medium.
     * Default value for the parameter interval_medium in the thing configuration
     */
    int DEFAULT_TEST_INTERVAL_MEDIUM = 60

    @Before
    public void setUp ()  {
        VolatileStorageService volatileStorageService = new VolatileStorageService()
        registerService(volatileStorageService)
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())

        thingRegistry = getService(ThingRegistry)
        assertThat thingRegistry, is(notNullValue())

        itemRegistry = getService(ItemRegistry)
        assertThat itemRegistry, is(notNullValue())
    }

    public void testChannelStateIsUpdated(ChannelUID channelUID,State stateType) {
    }

    private initilizeDefaultTestThingWithChannel(String channelID,String acceptedItemType) {
        Configuration thingConfig = new Configuration()
        thingConfig.put(SysteminfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME, new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH))
        thingConfig.put(SysteminfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME, new BigDecimal(DEFAULT_TEST_INTERVAL_MEDIUM))
        ThingTypeUID thingTypeUID = SysteminfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID thingUID = new ThingUID(thingTypeUID,thingName);

        ChannelUID channelUID = new ChannelUID(thingUID,channelID)
        Configuration channelConfig  = new Configuration()
        channelConfig.put("priority", "High")
        Channel channel = new Channel(channelUID,acceptedItemType,channelConfig)
        systemInfoThing = ThingBuilder.create(thingTypeUID,thingUID).withConfiguration(thingConfig).withChannel(channel).build();

        managedThingProvider.add(systemInfoThing)

        waitForAssert({
            SysteminfoHandler thingHandler = getService(ThingHandler,SysteminfoHandler)
            assertThat thingHandler, is(notNullValue())
        },2000)

        intializeItem(channelUID,acceptedItemType)
    }

    private void testItemStateIsUpdated(String acceptedItemType) {
        waitForAssert({
            def thingStatusDetail = systemInfoThing.getStatusInfo().getStatusDetail()
            def description = systemInfoThing.getStatusInfo().getDescription();
            assertThat  "Thing status detail is {$thingStatusDetail} with description {$description}",systemInfoThing.getStatus(), is(equalTo(ThingStatus.ONLINE))
        },2 * DEFAULT_TEST_INTERVAL_HIGH * 1000)

        def Items = itemRegistry.getItems();
        def GenericItem item = itemRegistry.getItem(ITEM_NAME) as GenericItem

        waitForAssert({
            State itemState = item.getState()
            assertThat itemState, not (equalTo(UnDefType.NULL))

            if(acceptedItemType.equals("Number")) {
                assertThat itemState, isA (DecimalType)
            } else if(acceptedItemType.equals("String")){
                assertThat itemState, isA (StringType)
            }
        },2 * DEFAULT_TEST_INTERVAL_HIGH * 1000)
    }

    private void intializeItem (ChannelUID channelUID,String acceptedItemType) {
        //add Item
        GenericItem itemType;
        if(acceptedItemType.equals("Number")) {
            itemType = new NumberItem(ITEM_NAME)
        } else if(acceptedItemType.equals("String")){
            itemType = new StringItem(ITEM_NAME)
        }
        itemRegistry.add(itemType)

        def ManagedItemChannelLinkProvider itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider)
        assertThat itemChannelLinkProvider, is(notNullValue())

        ThingUID thingUID = systemInfoThing.getUID()
        itemChannelLinkProvider.add(new ItemChannelLink(ITEM_NAME, channelUID))
    }



    @After
    public void tearDown () {
        if(systemInfoThing != null){
            // Remove the systeminfo thing. The handler will be also disposed automatically
            Thing removedThing = thingRegistry.remove(systemInfoThing.getUID())
            assertThat("The systeminfo thing cannot be deleted",removedThing,is(notNullValue()))
        }
    }

    @Test
    public void 'assert channel cpu_load is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_LOAD;
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel cpu_description is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_DESCRIPTION;
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel cpu_name is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_NAME;
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel cpu_logical_cores is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_LOGICAL_CORES
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel cpu_physical_cores is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_PHYSICAL_CORES
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel os_version is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_OS_VERSION;
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel os_family is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_OS_FAMILY;
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel os_manufacturer is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_OS_MANUFACTURER
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel memory_available is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_AVAILABLE
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel memory_used is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_USED
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel memory_total is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_TOTAL
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel memory_available_percent is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_AVAILABLE_PERCENT
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel sorage_name is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_NAME
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel storage_description is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_DESCRIPTION
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel storage_available is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_AVAILABLE
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel storage_used is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_USED
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel sorage_total is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_TOTAL
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel sorage_available_percent is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_AVAILABLE_PERCENT
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel sensors_cpu_temperature is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SENSORS_CPU_TEMPERATURE
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel sensors_cpu_voltage is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SENOSRS_CPU_VOLTAGE
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel sensors_fan_speed is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SENSORS_FAN_SPEED
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel battery_name is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_BATTERY_NAME
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel battery_remaining_capacity is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_BATTERY_REMAINING_CAPACITY
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel battery_remaining_time is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_BATTERY_REMAINING_TIME
        String acceptedItemType = "Number";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel display_information is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_DISPLAY_INFORMATION
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel network_ip is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_IP
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel network_name is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_NAME
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }

    @Test
    public void 'assert channel network_adapter_name is updated' () {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_ADAPTER_NAME
        String acceptedItemType = "String";

        initilizeDefaultTestThingWithChannel(channnelID,acceptedItemType);
        testItemStateIsUpdated(acceptedItemType);
    }
}
