/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.systeminfo.internal.SystemInfoBindingConstants;
import org.openhab.binding.systeminfo.internal.SystemInfoHandlerFactory;
import org.openhab.binding.systeminfo.internal.discovery.SystemInfoDiscoveryService;
import org.openhab.binding.systeminfo.internal.handler.SystemInfoHandler;
import org.openhab.binding.systeminfo.internal.model.DeviceNotFoundException;
import org.openhab.binding.systeminfo.internal.model.OSHISystemInfo;
import org.openhab.binding.systeminfo.internal.model.SystemInfoInterface;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.config.discovery.inbox.InboxPredicates;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.dimension.DataAmount;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
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
 * OSGi tests for the {@link SystemInfoHandler}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Lyubomir Papazov - Created a mock systeminfo object. This way, access to the user's OS will not be required,
 *         but mock data will be used instead, avoiding potential errors from the OS queries.
 * @author Wouter Born - Migrate Groovy to Java tests
 * @author Mark Herwege - Processor frequency channels
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SystemInfoOSGiTest extends JavaOSGiTest {

    private static final String DEFAULT_TEST_THING_NAME = "work";
    private static final String DEFAULT_TEST_ITEM_NAME = "test";
    private static final String DEFAULT_CHANNEL_TEST_PRIORITY = "High";
    private static final int DEFAULT_CHANNEL_PID = -1;
    private static final String DEFAULT_TEST_CHANNEL_ID = SystemInfoBindingConstants.CHANNEL_CPU_LOAD;
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

    private @Nullable Thing systeminfoThing;
    private @Nullable GenericItem testItem;

    private @Mock @NonNullByDefault({}) OSHISystemInfo mockedSystemInfo;
    private @NonNullByDefault({}) SystemInfoHandlerFactory systeminfoHandlerFactory;
    private @NonNullByDefault({}) ThingRegistry thingRegistry;
    private @NonNullByDefault({}) ItemRegistry itemRegistry;
    private @NonNullByDefault({}) ManagedThingProvider managedThingProvider;
    private @NonNullByDefault({}) ManagedItemChannelLinkProvider itemChannelLinkProvider;
    private @NonNullByDefault({}) UnitProvider unitProvider;
    private @NonNullByDefault({}) VolatileStorageService volatileStorageService;

    @BeforeEach
    public void setUp() {
        volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        // Preparing the mock with OS properties, that are used in the initialize method of SystemInfoHandler
        // Make this lenient because the assertInvalidThingConfigurationValuesAreHandled test does not require them
        lenient().when(mockedSystemInfo.getCpuLogicalCores()).thenReturn(new DecimalType(1));
        lenient().when(mockedSystemInfo.getCpuPhysicalCores()).thenReturn(new DecimalType(1));
        lenient().when(mockedSystemInfo.getOsFamily()).thenReturn(new StringType("Mock OS"));
        lenient().when(mockedSystemInfo.getOsManufacturer()).thenReturn(new StringType("Mock OS Manufacturer"));
        lenient().when(mockedSystemInfo.getOsVersion()).thenReturn(new StringType("Mock Os Version"));
        // Following mock method returns will make sure the thing does not get recreated with extra channels
        lenient().when(mockedSystemInfo.getNetworkIFCount()).thenReturn(1);
        lenient().when(mockedSystemInfo.getDisplayCount()).thenReturn(1);
        lenient().when(mockedSystemInfo.getFileOSStoreCount()).thenReturn(1);
        lenient().when(mockedSystemInfo.getPowerSourceCount()).thenReturn(1);
        lenient().when(mockedSystemInfo.getDriveCount()).thenReturn(1);
        lenient().when(mockedSystemInfo.getFanCount()).thenReturn(1);

        registerService(mockedSystemInfo);

        waitForAssert(() -> {
            systeminfoHandlerFactory = getService(ThingHandlerFactory.class, SystemInfoHandlerFactory.class);
            assertThat(systeminfoHandlerFactory, is(notNullValue()));
        });

        if (systeminfoHandlerFactory != null) {
            // Unbind oshiSystemInfo service and bind the mock service to make the systeminfo binding tests independent
            // of the external OSHI library
            SystemInfoInterface oshiSystemInfo = getService(SystemInfoInterface.class);
            if (oshiSystemInfo != null) {
                systeminfoHandlerFactory.unbindSystemInfo(oshiSystemInfo);
            }
            systeminfoHandlerFactory.bindSystemInfo(mockedSystemInfo);
        }

        waitForAssert(() -> {
            thingRegistry = getService(ThingRegistry.class);
            assertThat(thingRegistry, is(notNullValue()));
        });

        waitForAssert(() -> {
            itemRegistry = getService(ItemRegistry.class);
            assertThat(itemRegistry, is(notNullValue()));
        });

        waitForAssert(() -> {
            managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
            assertThat(managedThingProvider, is(notNullValue()));
        });

        waitForAssert(() -> {
            itemChannelLinkProvider = getService(ManagedItemChannelLinkProvider.class);
            assertThat(itemChannelLinkProvider, is(notNullValue()));
        });

        waitForAssert(() -> {
            unitProvider = getService(UnitProvider.class);
            assertThat(unitProvider, is(notNullValue()));
        });
    }

    @AfterEach
    public void tearDown() {
        Thing thing = systeminfoThing;
        if (thing != null) {
            // Remove the systeminfo thing. The handler will also be disposed automatically
            Thing removedThing = thingRegistry.forceRemove(thing.getUID());
            assertThat("The systeminfo thing cannot be deleted", removedThing, is(notNullValue()));
            waitForAssert(() -> {
                ThingHandler systemInfoHandler = thing.getHandler();
                assertThat(systemInfoHandler, is(nullValue()));
            });
            managedThingProvider.remove(thing.getUID());
        }

        if (testItem != null) {
            itemRegistry.remove(DEFAULT_TEST_ITEM_NAME);
        }

        unregisterService(mockedSystemInfo);
        unregisterService(volatileStorageService);
    }

    private void initializeThingWithChannelAndPID(String channelID, String acceptedItemType, int pid) {
        Configuration thingConfig = new Configuration();
        thingConfig.put(SystemInfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH));
        thingConfig.put(SystemInfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_MEDIUM));
        String priority = DEFAULT_CHANNEL_TEST_PRIORITY;

        initializeThing(thingConfig, channelID, acceptedItemType, priority, pid);
    }

    private void initializeThingWithChannelAndPriority(String channelID, String acceptedItemType, String priority) {
        Configuration thingConfig = new Configuration();
        thingConfig.put(SystemInfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH));
        thingConfig.put(SystemInfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
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
        thingConfig.put(SystemInfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_HIGH));
        thingConfig.put(SystemInfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(DEFAULT_TEST_INTERVAL_MEDIUM));

        String priority = DEFAULT_CHANNEL_TEST_PRIORITY;
        int pid = DEFAULT_CHANNEL_PID;
        initializeThing(thingConfig, channelID, acceptedItemType, priority, pid);
    }

    private void initializeThing(Configuration thingConfiguration, String channelID, String acceptedItemType,
            String priority, int pid) {
        ThingTypeUID thingTypeUID = SystemInfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID thingUID = new ThingUID(thingTypeUID, DEFAULT_TEST_THING_NAME);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        String channelTypeId = channelUID.getIdWithoutGroup();
        if ("load1".equals(channelTypeId) || "load5".equals(channelTypeId) || "load15".equals(channelTypeId)) {
            channelTypeId = "loadAverage";
        }
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(SystemInfoBindingConstants.BINDING_ID, channelTypeId);
        Configuration channelConfig = new Configuration();
        channelConfig.put("priority", priority);
        channelConfig.put("pid", new BigDecimal(pid));
        Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withType(channelTypeUID)
                .withKind(ChannelKind.STATE).withConfiguration(channelConfig).build();

        ThingBuilder thingBuilder = ThingBuilder.create(thingTypeUID, thingUID).withConfiguration(thingConfiguration)
                .withChannel(channel);
        // Make sure the thingTypeVersion matches the highest version in the update instructions of the binding to avoid
        // new channels being added and the thing not initializing
        thingBuilder = thingBuilder.withProperties(Map.of("thingTypeVersion", "1"));
        Thing thing = thingBuilder.build();
        systeminfoThing = thing;

        managedThingProvider.add(thing);

        waitForAssert(() -> {
            SystemInfoHandler handler = (SystemInfoHandler) thing.getHandler();
            assertThat(handler, is(notNullValue()));
        });

        waitForAssert(() -> {
            assertThat("Thing is not initialized, before an Item is created", thing.getStatus(),
                    anyOf(equalTo(ThingStatus.OFFLINE), equalTo(ThingStatus.ONLINE)));
        });

        intializeItem(channelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);
    }

    private void assertItemState(String acceptedItemType, String itemName, String priority, State expectedState) {
        Thing thing = systeminfoThing;
        if (thing == null) {
            throw new AssertionError("Thing is null");
        }
        waitForAssert(() -> {
            ThingStatusDetail thingStatusDetail = thing.getStatusInfo().getStatusDetail();
            String description = thing.getStatusInfo().getDescription();
            assertThat("Thing status detail is " + thingStatusDetail + " with description " + description,
                    thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        });
        // The binding starts all refresh tasks in SystemInfoHandler.scheduleUpdates() after this delay !
        try {
            sleep(SystemInfoHandler.WAIT_TIME_CHANNEL_ITEM_LINK_INIT * 1000);
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
        if ("High".equals(priority)) {
            waitTime = DEFAULT_TEST_INTERVAL_HIGH * 1000;
        } else if ("Medium".equals(priority)) {
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
        GenericItem item = null;
        if (acceptedItemType.startsWith("Number")) {
            item = new NumberItem(acceptedItemType, itemName, unitProvider);
        } else if ("String".equals(acceptedItemType)) {
            item = new StringItem(itemName);
        }
        if (item == null) {
            throw new AssertionError("Item is null");
        }
        itemRegistry.add(item);
        testItem = item;

        itemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));
    }

    @Test
    public void assertInvalidThingConfigurationValuesAreHandled() {
        Configuration configuration = new Configuration();

        // invalid value - must be positive
        int refreshIntervalHigh = -5;
        configuration.put(SystemInfoBindingConstants.HIGH_PRIORITY_REFRESH_TIME, new BigDecimal(refreshIntervalHigh));

        int refreshIntervalMedium = 3;
        configuration.put(SystemInfoBindingConstants.MEDIUM_PRIORITY_REFRESH_TIME,
                new BigDecimal(refreshIntervalMedium));
        initializeThingWithConfiguration(configuration);

        testInvalidConfiguration();
    }

    private void testInvalidConfiguration() {
        waitForAssert(() -> {
            Thing thing = systeminfoThing;
            if (thing != null) {
                assertThat("Invalid configuration is used !", thing.getStatus(), is(equalTo(ThingStatus.OFFLINE)));
                assertThat(thing.getStatusInfo().getStatusDetail(),
                        is(equalTo(ThingStatusDetail.HANDLER_INITIALIZING_ERROR)));
                assertThat(thing.getStatusInfo().getDescription(), is(equalTo("@text/offline.cannot-initialize")));
            }
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
    public void assertChannelCpuMaxFreq() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_MAXFREQ;
        String acceptedItemType = "Number:Frequency";

        QuantityType<Frequency> mockedCpuMaxFreqValue = new QuantityType<>(2500, Units.HERTZ);
        when(mockedSystemInfo.getCpuMaxFreq()).thenReturn(mockedCpuMaxFreqValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuMaxFreqValue);
    }

    @Test
    public void assertChannelCpuFreq() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_FREQ;
        String acceptedItemType = "Number:Frequency";

        QuantityType<Frequency> mockedCpuFreqValue = new QuantityType<>(2500, Units.HERTZ);
        when(mockedSystemInfo.getCpuFreq(0)).thenReturn(mockedCpuFreqValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuFreqValue);
    }

    @Test
    public void assertChannelCpuLoadIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_LOAD;
        String acceptedItemType = "Number";

        PercentType mockedCpuLoadValue = new PercentType(9);
        when(mockedSystemInfo.getSystemCpuLoad()).thenReturn(mockedCpuLoadValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoadValue);
    }

    @Test
    public void assertChannelCpuLoad1IsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_LOAD_1;
        String acceptedItemType = "Number";

        DecimalType mockedCpuLoad1Value = new DecimalType(1.1);
        when(mockedSystemInfo.getCpuLoad1()).thenReturn(mockedCpuLoad1Value);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoad1Value);
    }

    @Test
    public void assertChannelCpuLoad5IsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_LOAD_5;
        String acceptedItemType = "Number";

        DecimalType mockedCpuLoad5Value = new DecimalType(5.5);
        when(mockedSystemInfo.getCpuLoad5()).thenReturn(mockedCpuLoad5Value);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoad5Value);
    }

    @Test
    public void assertChannelCpuLoad15IsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_LOAD_15;
        String acceptedItemType = "Number";

        DecimalType mockedCpuLoad15Value = new DecimalType(15.15);
        when(mockedSystemInfo.getCpuLoad15()).thenReturn(mockedCpuLoad15Value);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuLoad15Value);
    }

    @Test
    public void assertChannelCpuThreadsIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_THREADS;
        String acceptedItemType = "Number";

        DecimalType mockedCpuThreadsValue = new DecimalType(16);
        when(mockedSystemInfo.getCpuThreads()).thenReturn(mockedCpuThreadsValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuThreadsValue);
    }

    @Test
    public void assertChannelCpuUptimeIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_UPTIME;
        String acceptedItemType = "Number:Time";

        QuantityType<Time> mockedCpuUptimeValue = new QuantityType<>(100, Units.MINUTE);
        when(mockedSystemInfo.getCpuUptime()).thenReturn(mockedCpuUptimeValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuUptimeValue);
    }

    @Test
    public void assertChannelCpuDescriptionIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_DESCRIPTION;
        String acceptedItemType = "String";

        StringType mockedCpuDescriptionValue = new StringType("Mocked Cpu Descr");
        when(mockedSystemInfo.getCpuDescription()).thenReturn(mockedCpuDescriptionValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedCpuDescriptionValue);
    }

    @Test
    public void assertChannelCpuNameIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_CPU_NAME;
        String acceptedItemType = "String";

        StringType mockedCpuNameValue = new StringType("Mocked Cpu Name");
        when(mockedSystemInfo.getCpuName()).thenReturn(mockedCpuNameValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedCpuNameValue);
    }

    @Test
    public void assertChannelMemoryAvailableIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_MEMORY_AVAILABLE;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedMemoryAvailableValue = new QuantityType<>(1000, Units.MEBIBYTE);
        when(mockedSystemInfo.getMemoryAvailable()).thenReturn(mockedMemoryAvailableValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedMemoryAvailableValue);
    }

    @Test
    public void assertChannelMemoryUsedIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_MEMORY_USED;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedMemoryUsedValue = new QuantityType<>(24, Units.MEBIBYTE);
        when(mockedSystemInfo.getMemoryUsed()).thenReturn(mockedMemoryUsedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedMemoryUsedValue);
    }

    @Test
    public void assertChannelMemoryTotalIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_MEMORY_TOTAL;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedMemoryTotalValue = new QuantityType<>(1024, Units.MEBIBYTE);
        when(mockedSystemInfo.getMemoryTotal()).thenReturn(mockedMemoryTotalValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedMemoryTotalValue);
    }

    @Test
    public void assertChannelMemoryAvailablePercentIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_MEMORY_AVAILABLE_PERCENT;
        String acceptedItemType = "Number";

        PercentType mockedMemoryAvailablePercentValue = new PercentType(97);
        when(mockedSystemInfo.getMemoryAvailablePercent()).thenReturn(mockedMemoryAvailablePercentValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedMemoryAvailablePercentValue);
    }

    @Test
    public void assertChannelSwapAvailableIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SWAP_AVAILABLE;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedSwapAvailableValue = new QuantityType<>(482, Units.MEBIBYTE);
        when(mockedSystemInfo.getSwapAvailable()).thenReturn(mockedSwapAvailableValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSwapAvailableValue);
    }

    @Test
    public void assertChannelSwapUsedIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SWAP_USED;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedSwapUsedValue = new QuantityType<>(30, Units.MEBIBYTE);
        when(mockedSystemInfo.getSwapUsed()).thenReturn(mockedSwapUsedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedSwapUsedValue);
    }

    @Test
    public void assertChannelSwapTotalIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SWAP_TOTAL;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedSwapTotalValue = new QuantityType<>(512, Units.MEBIBYTE);
        when(mockedSystemInfo.getSwapTotal()).thenReturn(mockedSwapTotalValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedSwapTotalValue);
    }

    @Test
    public void assertChannelSwapAvailablePercentIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SWAP_AVAILABLE_PERCENT;
        String acceptedItemType = "Number";

        PercentType mockedSwapAvailablePercentValue = new PercentType(94);
        when(mockedSystemInfo.getSwapAvailablePercent()).thenReturn(mockedSwapAvailablePercentValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSwapAvailablePercentValue);
    }

    @Test
    public void assertChannelStorageNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_NAME;
        String acceptedItemType = "String";

        StringType mockedStorageName = new StringType("Mocked Storage Name");
        when(mockedSystemInfo.getStorageName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedStorageName);
    }

    @Test
    public void assertChannelStorageTypeIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_TYPE;
        String acceptedItemType = "String";

        StringType mockedStorageType = new StringType("Mocked Storage Type");
        when(mockedSystemInfo.getStorageType(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageType);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedStorageType);
    }

    @Test
    public void assertChannelStorageDescriptionIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_DESCRIPTION;
        String acceptedItemType = "String";

        StringType mockedStorageDescription = new StringType("Mocked Storage Description");
        when(mockedSystemInfo.getStorageDescription(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageDescription);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageDescription);
    }

    @Test
    public void assertChannelStorageAvailableIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_AVAILABLE;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedStorageAvailableValue = new QuantityType<>(2000, Units.MEBIBYTE);
        when(mockedSystemInfo.getStorageAvailable(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageAvailableValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageAvailableValue);
    }

    @Test
    public void assertChannelStorageUsedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_USED;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedStorageUsedValue = new QuantityType<>(500, Units.MEBIBYTE);
        when(mockedSystemInfo.getStorageUsed(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageUsedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageUsedValue);
    }

    @Test
    public void assertChannelStorageTotalIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_TOTAL;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedStorageTotalValue = new QuantityType<>(2500, Units.MEBIBYTE);
        when(mockedSystemInfo.getStorageTotal(DEFAULT_DEVICE_INDEX)).thenReturn(mockedStorageTotalValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageTotalValue);
    }

    @Test
    public void assertChannelStorageAvailablePercentIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_STORAGE_AVAILABLE_PERCENT;
        String acceptedItemType = "Number";

        PercentType mockedStorageAvailablePercent = new PercentType(20);
        when(mockedSystemInfo.getStorageAvailablePercent(DEFAULT_DEVICE_INDEX))
                .thenReturn(mockedStorageAvailablePercent);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedStorageAvailablePercent);
    }

    @Test
    public void assertChannelDriveNameIsUpdated() throws DeviceNotFoundException {
        String channelID = SystemInfoBindingConstants.CHANNEL_DRIVE_NAME;
        String acceptedItemType = "String";

        StringType mockedDriveNameValue = new StringType("Mocked Drive Name");
        when(mockedSystemInfo.getDriveName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDriveNameValue);

        initializeThingWithChannel(channelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedDriveNameValue);
    }

    @Test
    public void assertChannelDriveModelIsUpdated() throws DeviceNotFoundException {
        String channelID = SystemInfoBindingConstants.CHANNEL_DRIVE_MODEL;
        String acceptedItemType = "String";

        StringType mockedDriveModelValue = new StringType("Mocked Drive Model");
        when(mockedSystemInfo.getDriveModel(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDriveModelValue);

        initializeThingWithChannel(channelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedDriveModelValue);
    }

    @Test
    public void assertChannelDriveSerialIsUpdated() throws DeviceNotFoundException {
        String channelID = SystemInfoBindingConstants.CHANNEL_DRIVE_SERIAL;
        String acceptedItemType = "String";

        StringType mockedDriveSerialNumber = new StringType("Mocked Drive Serial Number");
        when(mockedSystemInfo.getDriveSerialNumber(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDriveSerialNumber);

        initializeThingWithChannel(channelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedDriveSerialNumber);
    }

    // Re-enable this previously disabled test, as it is not relying on hardware anymore, but a mocked object
    // There is a bug opened for this issue - https://github.com/dblock/oshi/issues/185
    @Test
    public void assertChannelSensorsCpuTempIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SENSORS_CPU_TEMPERATURE;
        String acceptedItemType = "Number:Temperature";

        QuantityType<Temperature> mockedSensorsCpuTemperatureValue = new QuantityType<>(60, SIUnits.CELSIUS);
        when(mockedSystemInfo.getSensorsCpuTemperature()).thenReturn(mockedSensorsCpuTemperatureValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSensorsCpuTemperatureValue);
    }

    @Test
    public void assertChannelSensorsCpuVoltageIsUpdated() {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SENOSRS_CPU_VOLTAGE;
        String acceptedItemType = "Number:ElectricPotential";

        QuantityType<ElectricPotential> mockedSensorsCpuVoltageValue = new QuantityType<>(1000, Units.VOLT);
        when(mockedSystemInfo.getSensorsCpuVoltage()).thenReturn(mockedSensorsCpuVoltageValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSensorsCpuVoltageValue);
    }

    @Test
    public void assertChannelSensorsFanSpeedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_SENSORS_FAN_SPEED;
        String acceptedItemType = "Number";

        DecimalType mockedSensorsCpuFanSpeedValue = new DecimalType(180);
        when(mockedSystemInfo.getSensorsFanSpeed(DEFAULT_DEVICE_INDEX)).thenReturn(mockedSensorsCpuFanSpeedValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedSensorsCpuFanSpeedValue);
    }

    @Test
    public void assertChannelBatteryNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_BATTERY_NAME;
        String acceptedItemType = "String";

        StringType mockedBatteryName = new StringType("Mocked Battery Name");
        when(mockedSystemInfo.getBatteryName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedBatteryName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedBatteryName);
    }

    @Test
    public void assertChannelBatteryRemainingCapacityIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_BATTERY_REMAINING_CAPACITY;
        String acceptedItemType = "Number";

        PercentType mockedBatteryRemainingCapacity = new PercentType(20);
        when(mockedSystemInfo.getBatteryRemainingCapacity(DEFAULT_DEVICE_INDEX))
                .thenReturn(mockedBatteryRemainingCapacity);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedBatteryRemainingCapacity);
    }

    @Test
    public void assertChannelBatteryRemainingTimeIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_BATTERY_REMAINING_TIME;
        String acceptedItemType = "Number:Time";

        QuantityType<Time> mockedBatteryRemainingTime = new QuantityType<>(3600, Units.MINUTE);
        when(mockedSystemInfo.getBatteryRemainingTime(DEFAULT_DEVICE_INDEX)).thenReturn(mockedBatteryRemainingTime);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedBatteryRemainingTime);
    }

    @Test
    public void assertChannelDisplayInformationIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_DISPLAY_INFORMATION;
        String acceptedItemType = "String";

        StringType mockedDisplayInfo = new StringType("Mocked Display Information");
        when(mockedSystemInfo.getDisplayInformation(DEFAULT_DEVICE_INDEX)).thenReturn(mockedDisplayInfo);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedDisplayInfo);
    }

    @Test
    public void assertChannelNetworkIpIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_IP;
        String acceptedItemType = "String";

        StringType mockedNetworkIp = new StringType("192.168.1.0");
        when(mockedSystemInfo.getNetworkIp(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkIp);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkIp);
    }

    @Test
    public void assertChannelNetworkMacIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_MAC;
        String acceptedItemType = "String";

        StringType mockedNetworkMacValue = new StringType("AB-10-11-12-13-14");
        when(mockedSystemInfo.getNetworkMac(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkMacValue);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkMacValue);
    }

    @Test
    public void assertChannelNetworkDataSentIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_DATA_SENT;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedNetworkDataSent = new QuantityType<>(1000, Units.MEBIBYTE);
        when(mockedSystemInfo.getNetworkDataSent(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkDataSent);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkDataSent);
    }

    @Test
    public void assertChannelNetworkDataReceivedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_DATA_RECEIVED;
        String acceptedItemType = "Number:DataAmount";

        QuantityType<DataAmount> mockedNetworkDataReceiveed = new QuantityType<>(800, Units.MEBIBYTE);
        when(mockedSystemInfo.getNetworkDataReceived(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkDataReceiveed);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkDataReceiveed);
    }

    @Test
    public void assertChannelNetworkPacketsSentIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_PACKETS_SENT;
        String acceptedItemType = "Number";

        DecimalType mockedNetworkPacketsSent = new DecimalType(50);
        when(mockedSystemInfo.getNetworkPacketsSent(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkPacketsSent);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkPacketsSent);
    }

    @Test
    public void assertChannelNetworkPacketsReceivedIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_PACKETS_RECEIVED;
        String acceptedItemType = "Number";

        DecimalType mockedNetworkPacketsReceived = new DecimalType(48);
        when(mockedSystemInfo.getNetworkPacketsReceived(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkPacketsReceived);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkPacketsReceived);
    }

    @Test
    public void assertChannelNetworkNetworkNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_NAME;
        String acceptedItemType = "String";

        StringType mockedNetworkName = new StringType("MockN-AQ34");
        when(mockedSystemInfo.getNetworkName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedNetworkName);
    }

    @Test
    public void assertChannelNetworkNetworkDisplayNameIsUpdated() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_NETWORK_ADAPTER_NAME;
        String acceptedItemType = "String";

        StringType mockedNetworkAdapterName = new StringType("Mocked Network Adapter Name");
        when(mockedSystemInfo.getNetworkDisplayName(DEFAULT_DEVICE_INDEX)).thenReturn(mockedNetworkAdapterName);

        initializeThingWithChannel(channnelID, acceptedItemType);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY,
                mockedNetworkAdapterName);
    }

    class SystemInfoDiscoveryServiceMock extends SystemInfoDiscoveryService {
        String hostname;

        SystemInfoDiscoveryServiceMock(String hostname) {
            super();
            this.hostname = hostname;
        }

        @Override
        protected String getHostName() throws UnknownHostException {
            if ("unresolved".equals(hostname)) {
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
        String expectedHostname = SystemInfoDiscoveryService.DEFAULT_THING_ID;

        testDiscoveryService(expectedHostname, hostname);
    }

    @Test
    public void testDiscoveryWithEmptyHostnameString() {
        String hostname = "";
        String expectedHostname = SystemInfoDiscoveryService.DEFAULT_THING_ID;

        testDiscoveryService(expectedHostname, hostname);
    }

    private void testDiscoveryService(String expectedHostname, String hostname) {
        SystemInfoDiscoveryService discoveryService = getService(DiscoveryService.class,
                SystemInfoDiscoveryService.class);
        waitForAssert(() -> {
            assertThat(discoveryService, is(notNullValue()));
        });
        SystemInfoDiscoveryServiceMock discoveryServiceMock = new SystemInfoDiscoveryServiceMock(hostname);
        if (discoveryService != null) {
            unregisterService(DiscoveryService.class);
        }
        registerService(discoveryServiceMock, DiscoveryService.class.getName(), new Hashtable<>());

        ThingTypeUID computerType = SystemInfoBindingConstants.THING_TYPE_COMPUTER;
        ThingUID computerUID = new ThingUID(computerType, expectedHostname);

        discoveryServiceMock.startScan();

        Inbox inbox = getService(Inbox.class);
        waitForAssert(() -> {
            assertThat(inbox, is(notNullValue()));
        });

        if (inbox == null) {
            return;
        }

        waitForAssert(() -> {
            List<DiscoveryResult> results = inbox.stream().filter(InboxPredicates.forThingUID(computerUID)).toList();
            assertFalse(results.isEmpty(), "No Thing with UID " + computerUID.getAsString() + " in inbox");
        });

        inbox.approve(computerUID, SystemInfoDiscoveryService.DEFAULT_THING_LABEL, null);

        waitForAssert(() -> {
            systeminfoThing = thingRegistry.get(computerUID);
            assertThat(systeminfoThing, is(notNullValue()));
        });

        Thing thing = systeminfoThing;
        if (thing == null) {
            return;
        }

        waitForAssert(() -> {
            assertThat("Thing is not initialized.", thing.getStatus(), is(equalTo(ThingStatus.ONLINE)));
        });
    }

    @Test
    public void assertChannelProcessThreadsIsUpdatedWithPIDse() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_PROCESS_THREADS;
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
        String channnelID = SystemInfoBindingConstants.CHANNEL_PROCESS_PATH;
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
        String channnelID = SystemInfoBindingConstants.CHANNEL_PROCESS_NAME;
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
        String channnelID = SystemInfoBindingConstants.CHANNEL_PROCESS_MEMORY;
        String acceptedItemType = "Number:DataAmount";
        // The pid of the System idle process in Windows
        int pid = 0;

        QuantityType<DataAmount> mockedProcessMemory = new QuantityType<>(450, Units.MEBIBYTE);
        when(mockedSystemInfo.getProcessMemoryUsage(pid)).thenReturn(mockedProcessMemory);

        initializeThingWithChannelAndPID(channnelID, acceptedItemType, pid);
        assertItemState(acceptedItemType, DEFAULT_TEST_ITEM_NAME, DEFAULT_CHANNEL_TEST_PRIORITY, mockedProcessMemory);
    }

    @Test
    public void assertChannelProcessLoadIsUpdatedWithPIDset() throws DeviceNotFoundException {
        String channnelID = SystemInfoBindingConstants.CHANNEL_PROCESS_LOAD;
        String acceptedItemType = "Number";
        // The pid of the System idle process in Windows
        int pid = 0;

        DecimalType mockedProcessLoad = new DecimalType(3);
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

        Thing thing = systeminfoThing;
        if (thing == null) {
            throw new AssertionError("Thing is null");
        }
        Channel channel = thing.getChannel(DEFAULT_TEST_CHANNEL_ID);
        if (channel == null) {
            throw new AssertionError("Channel '" + DEFAULT_TEST_CHANNEL_ID + "' is null");
        }

        ThingHandler thingHandler = thing.getHandler();
        if (thingHandler == null) {
            throw new AssertionError("Thing handler is null");
        }
        if (!(thingHandler.getClass().equals(SystemInfoHandler.class))) {
            throw new AssertionError("Thing handler not of class SystemInfoHandler");
        }
        SystemInfoHandler handler = (SystemInfoHandler) thingHandler;
        waitForAssert(() -> {
            assertThat("The initial priority of channel " + channel.getUID() + " is not as expected.",
                    channel.getConfiguration().get(priorityKey), is(equalTo(initialPriority)));
            assertThat(handler.getHighPriorityChannels().contains(channel.getUID()), is(true));
        });

        // Change the priority of a channel, keep the pid
        Configuration updatedConfig = new Configuration();
        updatedConfig.put(priorityKey, newPriority);
        updatedConfig.put(pidKey, channel.getConfiguration().get(pidKey));
        Channel updatedChannel = ChannelBuilder.create(channel.getUID(), channel.getAcceptedItemType())
                .withType(channel.getChannelTypeUID()).withKind(channel.getKind()).withConfiguration(updatedConfig)
                .build();

        Thing updatedThing = ThingBuilder.create(thing.getThingTypeUID(), thing.getUID())
                .withConfiguration(thing.getConfiguration()).withChannel(updatedChannel).build();

        handler.thingUpdated(updatedThing);

        waitForAssert(() -> {
            assertThat("The prority of the channel was not updated: ", channel.getConfiguration().get(priorityKey),
                    is(equalTo(newPriority)));
            assertThat(handler.getLowPriorityChannels().contains(channel.getUID()), is(true));
        });
    }
}
