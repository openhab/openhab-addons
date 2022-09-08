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
package org.openhab.binding.systeminfo.test;

import static java.lang.Thread.sleep;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants;
import org.openhab.binding.systeminfo.internal.SysteminfoHandlerFactory;
import org.openhab.binding.systeminfo.internal.discovery.SysteminfoDiscoveryService;
import org.openhab.binding.systeminfo.internal.handler.SysteminfoHandler;
import org.openhab.binding.systeminfo.internal.model.DeviceNotFoundException;
import org.openhab.binding.systeminfo.internal.model.SysteminfoInterface;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.config.discovery.inbox.InboxPredicates;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ManagedItemChannelLinkProvider;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * OSGi tests for the {@link SysteminfoHandler}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Lyubomir Papazov - Created a mock systeminfo object. This way, access to the user's OS will not be required,
 *         but mock data will be used instead, avoiding potential errors from the OS queries.
 * @author Wouter Born - Migrate Groovy to Java tests
 */
public class SysteminfoOSGiTest extends JavaOSGiTest {
    private static final String DEFAULT_TEST_THING_NAME = "work";
    private static final String DEFAULT_TEST_ITEM_NAME = "test";
    private static final String DEFAULT_CHANNEL_TEST_PRIORITY = "High";
    private static final int DEFAULT_CHANNEL_PID = -1;
    private static final String DEFAULT_TEST_CHANNEL_ID = SysteminfoBindingConstants.CHANNEL_CPU_LOAD;
    private static final int DEFAULT_DEVICE_INDEX = 0;

    /**
     * Refresh time in seconds for tasks with priority High.
     * Default value for the parameter interval_high in the thing configuration
     */
    private static final int DEFAULT_TEST_INTERVAL_HIGH = 1;

    /**
     * Refresh time in seconds for tasks with priority Medium.
     */
    private static final int DEFAULT_TEST_INTERVAL_MEDIUM = 3;

    private Thing systemInfoThing;
    private SysteminfoHandler systemInfoHandler;
    private GenericItem testItem;

    private SysteminfoInterface mockedSystemInfo;
    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;
    private SysteminfoHandlerFactory systeminfoHandlerFactory;

    @BeforeEach
    public void setUp() {
        VolatileStorageService volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        // Preparing the mock with OS properties, that are used in the initialize method of SysteminfoHandler
        mockedSystemInfo = mock(SysteminfoInterface.class);
        when(mockedSystemInfo.getCpuLogicalCores()).thenReturn(new DecimalType(2));
        when(mockedSystemInfo.getCpuPhysicalCores()).thenReturn(new DecimalType(2));
        when(mockedSystemInfo.getOsFamily()).thenReturn(new StringType("Mock OS"));
        when(mockedSystemInfo.getOsManufacturer()).thenReturn(new StringType("Mock OS Manufacturer"));
        when(mockedSystemInfo.getOsVersion()).thenReturn(new StringType("Mock Os Version"));

        systeminfoHandlerFactory = getService(ThingHandlerFactory.class, SysteminfoHandlerFactory.class);
        SysteminfoInterface oshiSystemInfo = getService(SysteminfoInterface.class);

        // Unbind oshiSystemInfo service and bind the mock service to make the systeminfobinding tests independent of
        // the external OSHI library
        if (oshiSystemInfo != null) {
            systeminfoHandlerFactory.unbindSystemInfo(oshiSystemInfo);
        }
        systeminfoHandlerFactory.bindSystemInfo(mockedSystemInfo);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));

        thingRegistry = getService(ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        itemRegistry = getService(ItemRegistry.class);
        assertThat(itemRegistry, is(notNullValue()));
    }

    @AfterEach
    public void tearDown() {
        if (systemInfoThing != null) {
            // Remove the systeminfo thing. The handler will be also disposed automatically
            Thing removedThing = thingRegistry.forceRemove(systemInfoThing.getUID());
            assertThat("The systeminfo thing cannot be deleted", removedThing, is(notNullValue()));
        }
        waitForAssert(() -> {
            ThingHandler systemInfoHandler = systemInfoThing.getHandler();
            assertThat(systemInfoHandler, is(nullValue()));
        });

        if (testItem != null) {
            itemRegistry.remove(DEFAULT_TEST_ITEM_NAME);
        }
    }

    private void initializeThingWithChannelAndPID(String channelID, String acceptedItemType, int pid) {
        Configuration thingConfig = new Configuration();
        thingConfig.put(SysteminfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH));
        thingConfig.put(SysteminfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_MEDIUM));
        String priority = DEFAULT_CHANNEL_TEST_PRIORITY;

        initializeThing(thingConfig, channelID, acceptedItemType, priority, pid);
    }

    private void initializeThingWithChannelAndPriority(String channelID, String acceptedItemType, String priority) {
        Configuration thingConfig = new Configuration();
        thingConfig.put(SysteminfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH));
        thingConfig.put(SysteminfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_MEDIUM));
        int pid = DEFAULT_CHANNEL_PID;

        initializeThing(thingConfig, channelID, acceptedItemType, priority, pid);
    }

    private void initializeThingWithConfiguration(Configuration config) {
        String priority = DEFAULT_CHANNEL_TEST_PRIORITY;
        String channelID = DEFAULT_TEST_CHANNEL_ID;
        String acceptedItemType = "String";
        int pid = DEFAULT_CHANNEL_PID;

        initializeThing(config, channelID, acceptedItemType, priority, pid);
    }

    private void initializeThingWithChannel(String channelID, String acceptedItemType) {
        Configuration thingConfig = new Configuration();
        thingConfig.put(SysteminfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH));
        thingConfig.put(SysteminfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_MEDIUM));

        String priority = DEFAULT_CHANNEL_TEST_PRIORITY;
        int pid = DEFAULT_CHANNEL_PID;
        initializeThing(thingConfig, channelID, acceptedItemType, priority, pid);
    }

    private void initializeThing(Configuration thingConfiguration, String channelID, String acceptedItemType,
            String priority, int pid) {
        ThingTypeUID thingTypeUID = SysteminfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID thingUID = new ThingUID(thingTypeUID, DEFAULT_TEST_THING_NAME);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(SysteminfoBindingConstants.BINDING_ID,
                channelUID.getIdWithoutGroup());
        Configuration channelConfig = new Configuration();
        channelConfig.put("priority", priority);
        channelConfig.put("pid", new BigDecimal(pid));
        Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withType(channelTypeUID)
                .withKind(ChannelKind.STATE).withConfiguration(channelConfig).build();

        systemInfoThing = ThingBuilder.create(thingTypeUID, thingUID).withConfiguration(thingConfiguration)
                .withChannel(channel).build();

        managedThingProvider.add(systemInfoThing);

        waitForAssert(() -> {
            systemInfoHandler = (SysteminfoHandler) systemInfoThing.getHandler();
            assertThat(systemInfoHandler, is(notNullValue()));
        });

        waitForAssert(() -> {
            assertThat("Thing is not initilized, before an Item is created", systemInfoThing.getStatus(),
                    anyOf(equalTo(ThingStatus.OFFLINE), equalTo(ThingStatus.ONLINE)));
        });

        intializeItem(channelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);
    }

    private void assertItemState(String acceptedItemType, String itemName, String priority, State expectedState) {
        waitForAssert(() -> {
            ThingStatusDetail thingStatusDetail = systemInfoThing.getStatusInfo().getStatusDetail();
            String description = systemInfoThing.getStatusInfo().getDescription();
            assertThat("Thing status detail is " + thingStatusDetail + " with description " + description,
                    systemInfoThing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        });
        // The binding starts all refresh tasks in SysteminfoHandler.scheduleUpdates() after this delay !
        try {
            sleep(SysteminfoHandler.WAIT_TIME_CHANNEL_ITEM_LINK_INIT * 1000);
        } catch (InterruptedException e) {
            throw new AssertionError("Interrupted while sleeping");
        }

        GenericItem item;
        try {
            item = (GenericItem) itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            throw new AssertionError("Item not found in registry");
        }

        int waitTime;
        if (priority.equals("High")) {
            waitTime = DEFAULT_TEST_INTERVAL_HIGH * 1000;
        } else if (priority.equals("Medium")) {
            waitTime = DEFAULT_TEST_INTERVAL_MEDIUM * 1000;
        } else {
            waitTime = 100;
        }

        waitForAssert(() -> {
            State itemState = item.getState();
            assertThat(itemState, is(equalTo(expectedState)));
        }, waitTime, DFL_SLEEP_TIME);
    }

    private void intializeItem(ChannelUID channelUID, String itemName, String acceptedItemType) {
        if (acceptedItemType.equals("Number")) {
            testItem = new NumberItem(itemName);
        } else if (acceptedItemType.equals("String")) {
            testItem = new StringItem(itemName);
        }
        itemRegistry.add(testItem);

        ManagedItemChannelLinkProvider itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
        assertThat(itemChannelLinkProvider, is(notNullValue()));

        itemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));
    }

    @Test
    public void assertInvalidThingConfigurationValuesAreHandled() {
        Configuration configuration = new Configuration();

        // invalid value - must be positive
        int refreshIntervalHigh = -5;
        configuration.put(SysteminfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME, new BigDecimal(refreshIntervalHigh));

        int refreshIntervalMedium = 3;
        configuration.put(SysteminfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(refreshIntervalMedium));
        initializeThingWithConfiguration(configuration);

        testInvalidConfiguration();
    }

    private void testInvalidConfiguration() {
        waitForAssert(() -> {
            assertThat("Invalid configuratuin is used !", systemInfoThing.getStatus(),
                    is(equalTo(ThingStatus.OFFLINE)));
            assertThat(systemInfoThing.getStatusInfo().getStatusDetail(),
                    is(equalTo(ThingStatusDetail.HANDLER_INITIALIZING_ERROR)));
            assertThat(systemInfoThing.getStatusInfo().getDescription(), is(equalTo("Thing cannot be initialized!")));
        });
    }

    @Test
    public void assertThingStatusIsUninitializedWhenThereIsNoSysteminfoServiceProvided() {
        // Unbind the mock service to verify the systeminfo thing will not be initialized when no systeminfo service is
        // provided
        systeminfoHandlerFactory.unbindSystemInfo(mockedSystemInfo);

        ThingTypeUID thingTypeUID = SysteminfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID thingUID = new ThingUID(thingTypeUID, DEFAULT_TEST_THING_NAME);

        systemInfoThing = ThingBuilder.create(thingTypeUID, thingUID).build();
        managedThingProvider.add(systemInfoThing);

        waitForAssert(() -> {
            assertThat("The thing status is uninitialized when systeminfo service is missing",
                    systemInfoThing.getStatus(), equalTo(ThingStatus.UNINITIALIZED));
        });
    }

    @Test
    public void assertMediumPriorityChannelIsUpdated() {
        String channnelID = DEFAULT_TEST_CHANNEL_ID;
        String acceptedItemType = "Number";
        String priority = "Medium";

        initializeThingWithChannelAndPriority(channnelID, acceptedItemType, priority);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, priority, UnDefType.UNDEF);
    }

    @Test
    public void assertStateOfSecondDeviceIsUpdated() {
        // This test assumes that at least 2 network interfaces are present on the test platform
        int deviceIndex = 1;
        String channnelID = "network" + deviceIndex + "#mac";
        String acceptedItemType = "String";

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, UnDefType.UNDEF);
    }

    @Test
    public void assertChannelCpuLoadIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_LOAD;
        String acceptedItemType = "Number";

        PercentType mockedCpuLoadValue = new PercentType(9);
        when(mockedSystemInfo.getSystemCpuLoad()).thenReturn(mockedCpuLoadValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoadValue);
    }

    @Test
    public void assertChannelCpuLoad1IsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_LOAD_1;
        String acceptedItemType = "Number";

        DecimalType mockedCpuLoad1Value = new DecimalType(1.1);
        when(mockedSystemInfo.getCpuLoad1()).thenReturn(mockedCpuLoad1Value);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoad1Value);
    }

    @Test
    public void assertChannelCpuLoad5IsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_LOAD_5;
        String acceptedItemType = "Number";

        DecimalType mockedCpuLoad5Value = new DecimalType(5.5);
        when(mockedSystemInfo.getCpuLoad5()).thenReturn(mockedCpuLoad5Value);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoad5Value);
    }

    @Test
    public void assertChannelCpuLoad15IsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_LOAD_15;
        String acceptedItemType = "Number";

        DecimalType mockedCpuLoad15Value = new DecimalType(15.15);
        when(mockedSystemInfo.getCpuLoad15()).thenReturn(mockedCpuLoad15Value);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoad15Value);
    }

    @Test
    public void assertChannelCpuThreadsIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_THREADS;
        String acceptedItemType = "Number";

        DecimalType mockedCpuThreadsValue = new DecimalType(16);
        when(mockedSystemInfo.getCpuThreads()).thenReturn(mockedCpuThreadsValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuThreadsValue);
    }

    @Test
    public void assertChannelCpuUptimeIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_UPTIME;
        String acceptedItemType = "Number";

        DecimalType mockedCpuUptimeValue = new DecimalType(100);
        when(mockedSystemInfo.getCpuUptime()).thenReturn(mockedCpuUptimeValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuUptimeValue);
    }

    @Test
    public void assertChannelCpuDescriptionIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_DESCRIPTION;
        String acceptedItemType = "String";

        StringType mockedCpuDescriptionValue = new StringType("Mocked Cpu Descr");
        when(mockedSystemInfo.getCpuDescription()).thenReturn(mockedCpuDescriptionValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedCpuDescriptionValue);
    }

    @Test
    public void assertChannelCpuNameIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_CPU_NAME;
        String acceptedItemType = "String";

        StringType mockedCpuNameValue = new StringType("Mocked Cpu Name");
        when(mockedSystemInfo.getCpuName()).thenReturn(mockedCpuNameValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuNameValue);
    }

    @Test
    public void assertChannelMemoryAvailableIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_AVAILABLE;
        String acceptedItemType = "Number";

        DecimalType mockedMemoryAvailableValue = new DecimalType(1000);
        when(mockedSystemInfo.getMemoryAvailable()).thenReturn(mockedMemoryAvailableValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedMemoryAvailableValue);
    }

    @Test
    public void assertChannelMemoryUsedIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_USED;
        String acceptedItemType = "Number";

        DecimalType mockedMemoryUsedValue = new DecimalType(24);
        when(mockedSystemInfo.getMemoryUsed()).thenReturn(mockedMemoryUsedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedMemoryUsedValue);
    }

    @Test
    public void assertChannelMemoryTotalIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_TOTAL;
        String acceptedItemType = "Number";

        DecimalType mockedMemoryTotalValue = new DecimalType(1024);
        when(mockedSystemInfo.getMemoryTotal()).thenReturn(mockedMemoryTotalValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedMemoryTotalValue);
    }

    @Test
    public void assertChannelMemoryAvailablePercentIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_MEMORY_AVAILABLE_PERCENT;
        String acceptedItemType = "Number";

        DecimalType mockedMemoryAvailablePercentValue = new DecimalType(97);
        when(mockedSystemInfo.getMemoryAvailablePercent()).thenReturn(mockedMemoryAvailablePercentValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedMemoryAvailablePercentValue);
    }

    @Test
    public void assertChannelSwapAvailableIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SWAP_AVAILABLE;
        String acceptedItemType = "Number";

        DecimalType mockedSwapAvailableValue = new DecimalType(482);
        when(mockedSystemInfo.getSwapAvailable()).thenReturn(mockedSwapAvailableValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSwapAvailableValue);
    }

    @Test
    public void assertChannelSwapUsedIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SWAP_USED;
        String acceptedItemType = "Number";

        DecimalType mockedSwapUsedValue = new DecimalType(30);
        when(mockedSystemInfo.getSwapUsed()).thenReturn(mockedSwapUsedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedSwapUsedValue);
    }

    @Test
    public void assertChannelSwapTotalIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SWAP_TOTAL;
        String acceptedItemType = "Number";

        DecimalType mockedSwapTotalValue = new DecimalType(512);
        when(mockedSystemInfo.getSwapTotal()).thenReturn(mockedSwapTotalValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedSwapTotalValue);
    }

    @Test
    public void assertChannelSwapAvailablePercentIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SWAP_AVAILABLE_PERCENT;
        String acceptedItemType = "Number";

        DecimalType mockedSwapAvailablePercentValue = new DecimalType(94);
        when(mockedSystemInfo.getSwapAvailablePercent()).thenReturn(mockedSwapAvailablePercentValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSwapAvailablePercentValue);
    }

    @Test
    public void assertChannelStorageNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_NAME;
        String acceptedItemType = "String";

        StringType mockedStorageName = new StringType("Mocked Storage Name");
        when(mockedSystemInfo.getStorageName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedStorageName);
    }

    @Test
    public void assertChannelStorageTypeIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_TYPE;
        String acceptedItemType = "String";

        StringType mockedStorageType = new StringType("Mocked Storage Type");
        when(mockedSystemInfo.getStorageType(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageType);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedStorageType);
    }

    @Test
    public void assertChannelStorageDescriptionIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_DESCRIPTION;
        String acceptedItemType = "String";

        StringType mockedStorageDescription = new StringType("Mocked Storage Description");
        when(mockedSystemInfo.getStorageDescription(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageDescription);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageDescription);
    }

    @Test
    public void assertChannelStorageAvailableIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_AVAILABLE;
        String acceptedItemType = "Number";

        DecimalType mockedStorageAvailableValue = new DecimalType(2000);
        when(mockedSystemInfo.getStorageAvailable(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageAvailableValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageAvailableValue);
    }

    @Test
    public void assertChannelStorageUsedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_USED;
        String acceptedItemType = "Number";

        DecimalType mockedStorageUsedValue = new DecimalType(500);
        when(mockedSystemInfo.getStorageUsed(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageUsedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageUsedValue);
    }

    @Test
    public void assertChannelStorageTotalIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_TOTAL;
        String acceptedItemType = "Number";

        DecimalType mockedStorageTotalValue = new DecimalType(2500);
        when(mockedSystemInfo.getStorageTotal(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageTotalValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageTotalValue);
    }

    @Test
    public void assertChannelStorageAvailablePercentIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_STORAGE_AVAILABLE_PERCENT;
        String acceptedItemType = "Number";

        DecimalType mockedStorageAvailablePercent = new DecimalType(20);
        when(mockedSystemInfo.getStorageAvailablePercent(DEFAULT_DEVICE_INDEX))
                .thenReturn(mockedStorageAvailablePercent);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageAvailablePercent);
    }

    @Test
    public void assertChannelDriveNameIsUpdated() throws DeviceNotFoundException {
        String channelID = SysteminfoBindingConstants.CHANNEL_DRIVE_NAME;
        String acceptedItemType = "String";

        StringType mockedDriveNameValue = new StringType("Mocked Drive Name");
        when(mockedSystemInfo.getDriveName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDriveNameValue);

        initializeThingWithChannel(channelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedDriveNameValue);
    }

    @Test
    public void assertChannelDriveModelIsUpdated() throws DeviceNotFoundException {
        String channelID = SysteminfoBindingConstants.CHANNEL_DRIVE_MODEL;
        String acceptedItemType = "String";

        StringType mockedDriveModelValue = new StringType("Mocked Drive Model");
        when(mockedSystemInfo.getDriveModel(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDriveModelValue);

        initializeThingWithChannel(channelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedDriveModelValue);
    }

    @Test
    public void assertChannelDriveSerialIsUpdated() throws DeviceNotFoundException {
        String channelID = SysteminfoBindingConstants.CHANNEL_DRIVE_SERIAL;
        String acceptedItemType = "String";

        StringType mockedDriveSerialNumber = new StringType("Mocked Drive Serial Number");
        when(mockedSystemInfo.getDriveSerialNumber(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDriveSerialNumber);

        initializeThingWithChannel(channelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedDriveSerialNumber);
    }

    @Disabled
    // There is a bug opened for this issue - https://github.com/dblock/oshi/issues/185
    @Test
    public void assertChannelSensorsCpuTempIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SENSORS_CPU_TEMPERATURE;
        String acceptedItemType = "Number";

        DecimalType mockedSensorsCpuTemperatureValue = new DecimalType(60);
        when(mockedSystemInfo.getSensorsCpuTemperature()).thenReturn(mockedSensorsCpuTemperatureValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSensorsCpuTemperatureValue);
    }

    @Test
    public void assertChannelSensorsCpuVoltageIsUpdated() {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SENOSRS_CPU_VOLTAGE;
        String acceptedItemType = "Number";

        DecimalType mockedSensorsCpuVoltageValue = new DecimalType(1000);
        when(mockedSystemInfo.getSensorsCpuVoltage()).thenReturn(mockedSensorsCpuVoltageValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSensorsCpuVoltageValue);
    }

    @Test
    public void assertChannelSensorsFanSpeedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_SENSORS_FAN_SPEED;
        String acceptedItemType = "Number";

        DecimalType mockedSensorsCpuFanSpeedValue = new DecimalType(180);
        when(mockedSystemInfo.getSensorsFanSpeed(DEFAULT_DEVICE_INDEX)).thenReturn(mockedSensorsCpuFanSpeedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSensorsCpuFanSpeedValue);
    }

    @Test
    public void assertChannelBatteryNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_BATTERY_NAME;
        String acceptedItemType = "String";

        StringType mockedBatteryName = new StringType("Mocked Battery Name");
        when(mockedSystemInfo.getBatteryName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedBatteryName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedBatteryName);
    }

    @Test
    public void assertChannelBatteryRemainingCapacityIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_BATTERY_REMAINING_CAPACITY;
        String acceptedItemType = "Number";

        DecimalType mockedBatteryRemainingCapacity = new DecimalType(200);
        when(mockedSystemInfo.getBatteryRemainingCapacity(DEFAULT_DEVICE_INDEX))
                .thenReturn(mockedBatteryRemainingCapacity);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedBatteryRemainingCapacity);
    }

    @Test
    public void assertChannelBatteryRemainingTimeIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_BATTERY_REMAINING_TIME;
        String acceptedItemType = "Number";

        DecimalType mockedBatteryRemainingTime = new DecimalType(3600);
        when(mockedSystemInfo.getBatteryRemainingTime(DEFAULT_DEVICE_INDEX)).thenReturn(mockedBatteryRemainingTime);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedBatteryRemainingTime);
    }

    @Test
    public void assertChannelDisplayInformationIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_DISPLAY_INFORMATION;
        String acceptedItemType = "String";

        StringType mockedDisplayInfo = new StringType("Mocked Display Information");
        when(mockedSystemInfo.getDisplayInformation(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDisplayInfo);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedDisplayInfo);
    }

    @Test
    public void assertChannelNetworkIpIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_IP;
        String acceptedItemType = "String";

        StringType mockedNetworkIp = new StringType("192.168.1.0");
        when(mockedSystemInfo.getNetworkIp(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkIp);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkIp);
    }

    @Test
    public void assertChannelNetworkMacIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_MAC;
        String acceptedItemType = "String";

        StringType mockedNetworkMacValue = new StringType("AB-10-11-12-13-14");
        when(mockedSystemInfo.getNetworkMac(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkMacValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkMacValue);
    }

    @Test
    public void assertChannelNetworkDataSentIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_DATA_SENT;
        String acceptedItemType = "Number";

        DecimalType mockedNetworkDataSent = new DecimalType(1000);
        when(mockedSystemInfo.getNetworkDataSent(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkDataSent);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkDataSent);
    }

    @Test
    public void assertChannelNetworkDataReceivedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_DATA_RECEIVED;
        String acceptedItemType = "Number";

        DecimalType mockedNetworkDataReceiveed = new DecimalType(800);
        when(mockedSystemInfo.getNetworkDataReceived(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkDataReceiveed);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkDataReceiveed);
    }

    @Test
    public void assertChannelNetworkPacketsSentIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_PACKETS_SENT;
        String acceptedItemType = "Number";

        DecimalType mockedNetworkPacketsSent = new DecimalType(50);
        when(mockedSystemInfo.getNetworkPacketsSent(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkPacketsSent);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkPacketsSent);
    }

    @Test
    public void assertChannelNetworkPacketsReceivedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_PACKETS_RECEIVED;
        String acceptedItemType = "Number";

        DecimalType mockedNetworkPacketsReceived = new DecimalType(48);
        when(mockedSystemInfo.getNetworkPacketsReceived(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkPacketsReceived);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkPacketsReceived);
    }

    @Test
    public void assertChannelNetworkNetworkNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_NAME;
        String acceptedItemType = "String";

        StringType mockedNetworkName = new StringType("MockN-AQ34");
        when(mockedSystemInfo.getNetworkName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkName);
    }

    @Test
    public void assertChannelNetworkNetworkDisplayNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_NETWORK_ADAPTER_NAME;
        String acceptedItemType = "String";

        StringType mockedNetworkAdapterName = new StringType("Mocked Network Adapter Name");
        when(mockedSystemInfo.getNetworkDisplayName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkAdapterName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkAdapterName);
    }

    class SysteminfoDiscoveryServiceMock extends SysteminfoDiscoveryService {
        String hostname;

        SysteminfoDiscoveryServiceMock(String hostname) {
            super();
            this.hostname = hostname;
        }

        @Override
        protected String getHostName() throws UnknownHostException {
            if (hostname.equals("unresolved")) {
                throw new UnknownHostException();
            }
            return hostname;
        }

        @Override
        public void startScan() {
            super.startScan();
        }
    }

    @Test
    public void testDiscoveryWithInvalidHostname() {
        String hostname = "Hilo.fritz.box";
        String expectedHostname = "Hilo_fritz_box";

        testDiscoveryService(expectedHostname, hostname);
    }

    @Test
    public void testDiscoveryWithValidHostname() {
        String hostname = "MyComputer";
        String expectedHostname = "MyComputer";

        testDiscoveryService(expectedHostname, hostname);
    }

    @Test
    public void testDiscoveryWithUnresolvedHostname() {
        String hostname = "unresolved";
        String expectedHostname = SysteminfoDiscoveryService.DEFAULT_THING_ID;

        testDiscoveryService(expectedHostname, hostname);
    }

    @Test
    public void testDiscoveryWithEmptyHostnameString() {
        String hostname = "";
        String expectedHostname = SysteminfoDiscoveryService.DEFAULT_THING_ID;

        testDiscoveryService(expectedHostname, hostname);
    }

    private void testDiscoveryService(String expectedHostname, String hostname) {
        SysteminfoDiscoveryService discoveryService = getService(DiscoveryService.class,
                SysteminfoDiscoveryService.class);
        waitForAssert(() -> {
            assertThat(discoveryService, is(notNullValue()));
        });
        SysteminfoDiscoveryServiceMock discoveryServiceMock = new SysteminfoDiscoveryServiceMock(hostname);
        if (discoveryService != null) {
            unregisterService(DiscoveryService.class);
        }
        registerService(discoveryServiceMock, DiscoveryService.class.getName(), new Hashtable<>());

        ThingTypeUID computerType = SysteminfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID computerUID = new ThingUID(computerType, expectedHostname);

        discoveryServiceMock.startScan();

        Inbox inbox = getService(Inbox.class);
        assertThat(inbox, is(notNullValue()));

        waitForAssert(() -> {
            List<DiscoveryResult> results = inbox.stream().filter(InboxPredicates.forThingUID(computerUID))
                    .collect(toList());
            assertFalse(results.isEmpty(), "No Thing with UID " + computerUID.getAsString() + " in inbox");
        });

        inbox.approve(computerUID, SysteminfoDiscoveryService.DEFAULT_THING_LABEL, null);

        waitForAssert(() -> {
            systemInfoThing = thingRegistry.get(computerUID);
            assertThat(systemInfoThing, is(notNullValue()));
        });

        waitForAssert(() -> {
            assertThat("Thing is not initialized.", systemInfoThing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        });
    }

    @Test
    public void assertChannelProcessThreadsIsUpdatedWithPIDse() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_PROCESS_THREADS;
        String acceptedItemType = "Number";
        // The pid of the System idle process in Windows
        int pid = 0;

        DecimalType mockedProcessThreadsCount = new DecimalType(4);
        when(mockedSystemInfo.getProcessThreads(pid)).thenReturn(mockedProcessThreadsCount);

        initializeThingWithChannelAndPID(channnelID, acceptedItemType, pid);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedProcessThreadsCount);
    }

    @Test
    public void assertChannelProcessPathIsUpdatedWithPIDset() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_PROCESS_PATH;
        String acceptedItemType = "String";
        // The pid of the System idle process in Windows
        int pid = 0;

        StringType mockedProcessPath = new StringType("C:\\Users\\MockedUser\\Process");
        when(mockedSystemInfo.getProcessPath(pid)).thenReturn(mockedProcessPath);

        initializeThingWithChannelAndPID(channnelID, acceptedItemType, pid);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedProcessPath);
    }

    @Test
    public void assertChannelProcessNameIsUpdatedWithPIDset() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_PROCESS_NAME;
        String acceptedItemType = "String";
        // The pid of the System idle process in Windows
        int pid = 0;

        StringType mockedProcessName = new StringType("MockedProcess.exe");
        when(mockedSystemInfo.getProcessName(pid)).thenReturn(mockedProcessName);

        initializeThingWithChannelAndPID(channnelID, acceptedItemType, pid);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedProcessName);
    }

    @Test
    public void assertChannelProcessMemoryIsUpdatedWithPIDset() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_PROCESS_MEMORY;
        String acceptedItemType = "Number";
        // The pid of the System idle process in Windows
        int pid = 0;

        DecimalType mockedProcessMemory = new DecimalType(450);
        when(mockedSystemInfo.getProcessMemoryUsage(pid)).thenReturn(mockedProcessMemory);

        initializeThingWithChannelAndPID(channnelID, acceptedItemType, pid);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedProcessMemory);
    }

    @Test
    public void assertChannelProcessLoadIsUpdatedWithPIDset() throws DeviceNotFoundException {
        String channnelID = SysteminfoBindingConstants.CHANNEL_PROCESS_LOAD;
        String acceptedItemType = "Number";
        // The pid of the System idle process in Windows
        int pid = 0;

        PercentType mockedProcessLoad = new PercentType(3);
        when(mockedSystemInfo.getProcessCpuUsage(pid)).thenReturn(mockedProcessLoad);

        initializeThingWithChannelAndPID(channnelID, acceptedItemType, pid);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedProcessLoad);
    }

    @Test
    public void testThingHandlesChannelPriorityChange() {
        String priorityKey = "priority";
        String pidKey = "pid";
        String initialPriority = DEFAULT_CHANNEL_TEST_PRIORITY; // Evaluates to High
        String newPriority = "Low";

        String acceptedItemType = "Number";
        initializeThingWithChannel(DEFAULT_TEST_CHANNEL_ID, acceptedItemType);

        Channel channel = systemInfoThing.getChannel(DEFAULT_TEST_CHANNEL_ID);
        if (channel == null) {
            throw new AssertionError("Channel '" + DEFAULT_TEST_CHANNEL_ID + "' is null");
        }

        waitForAssert(() -> {
            assertThat("The initial priority of channel " + channel.getUID() + " is not as expected.",
                    channel.getConfiguration().get(priorityKey), is(equalTo(initialPriority)));
            assertThat(systemInfoHandler.getHighPriorityChannels().contains(channel.getUID()), is(true));
        });

        // Change the priority of a channel, keep the pid
        Configuration updatedConfig = new Configuration();
        updatedConfig.put(priorityKey, newPriority);
        updatedConfig.put(pidKey, channel.getConfiguration().get(pidKey));
        Channel updatedChannel = ChannelBuilder.create(channel.getUID(), channel.getAcceptedItemType())
                .withType(channel.getChannelTypeUID()).withKind(channel.getKind()).withConfiguration(updatedConfig)
                .build();

        Thing updatedThing = ThingBuilder.create(systemInfoThing.getThingTypeUID(), systemInfoThing.getUID())
                .withConfiguration(systemInfoThing.getConfiguration()).withChannel(updatedChannel).build();

        systemInfoHandler.thingUpdated(updatedThing);

        waitForAssert(() -> {
            assertThat("The prority of the channel was not updated: ", channel.getConfiguration().get(priorityKey),
                    is(equalTo(newPriority)));
            assertThat(systemInfoHandler.getLowPriorityChannels().contains(channel.getUID()), is(true));
        });
    }
}
