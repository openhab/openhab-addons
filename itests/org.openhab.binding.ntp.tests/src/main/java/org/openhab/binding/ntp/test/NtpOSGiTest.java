/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.ntp.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemStateEvent;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.eclipse.smarthome.test.storage.VolatileStorageService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.ntp.internal.NtpBindingConstants;
import org.openhab.binding.ntp.internal.handler.NtpHandler;
import org.openhab.binding.ntp.server.SimpleNTPServer;

/**
 * OSGi tests for the {@link NtpHandler}
 *
 * @author Petar Valchev - Initial Contribution
 * @author Markus Rathgeb - Migrated tests from Groovy to pure Java
 * @author Erdoan Hadzhiyusein - Migrated tests to Java 8 and integrated the new DateTimeType
 */
public class NtpOSGiTest extends JavaOSGiTest {
    private static TimeZone systemTimeZone;
    private static Locale locale;

    private NtpHandler ntpHandler;
    private Thing ntpThing;
    private GenericItem testItem;

    private ManagedThingProvider managedThingProvider;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;
    private ChannelTypeProvider channelTypeProvider;

    private static final ZoneId DEFAULT_TIME_ZONE_ID = ZoneId.of("Europe/Bucharest");
    private static final String TEST_TIME_ZONE_ID = "America/Los_Angeles";

    private static final String TEST_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    private static final String TEST_ITEM_NAME = "testItem";
    private static final String TEST_THING_ID = "testThingId";

    // No bundle in ESH is exporting a package from which we can use item types
    // as constants, so we will use String.
    private static final String ACCEPTED_ITEM_TYPE_STRING = "String";
    private static final String ACCEPTED_ITEM_TYPE_DATE_TIME = "DateTime";
    private static final String TEST_HOSTNAME = "127.0.0.1";
    private static final int TEST_PORT = 9002;
    static SimpleNTPServer timeServer;
    private ChannelTypeUID channelTypeUID;

    enum UpdateEventType {
        HANDLE_COMMAND("handleCommand"),
        CHANNEL_LINKED("channelLinked");

        private final String updateEventType;

        private UpdateEventType(String updateEventType) {
            this.updateEventType = updateEventType;
        }

        public String getUpdateEventType() {
            return updateEventType;
        }
    }

    @BeforeClass
    public static void setUpClass() {
        // Initializing a new local server on this port
        timeServer = new SimpleNTPServer(TEST_PORT);
        // Starting the local server
        timeServer.startServer();

        /*
         * Store the initial system time zone and locale value, so that we can
         * restore them at the test end.
         */
        systemTimeZone = TimeZone.getDefault();
        locale = Locale.getDefault();

        /*
         * Set new default time zone and locale, which will be used during the
         * tests execution.
         */
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_TIME_ZONE_ID));
        Locale.setDefault(Locale.US);
    }

    @Before
    public void setUp() {
        VolatileStorageService volatileStorageService = new VolatileStorageService();
        registerService(volatileStorageService);

        managedThingProvider = getService(ThingProvider.class, ManagedThingProvider.class);
        assertNotNull(managedThingProvider);

        thingRegistry = getService(ThingRegistry.class);
        assertNotNull(thingRegistry);

        itemRegistry = getService(ItemRegistry.class);
        assertNotNull(itemRegistry);

        channelTypeUID = new ChannelTypeUID(NtpBindingConstants.BINDING_ID + ":channelType");
        channelTypeProvider = mock(ChannelTypeProvider.class);
        when(channelTypeProvider.getChannelType(any(ChannelTypeUID.class), any(Locale.class)))
                .thenReturn(new ChannelType(channelTypeUID, false, "Switch", ChannelKind.STATE, "label", null, null,
                        null, null, null, null));
        registerService(channelTypeProvider);
    }

    @After
    public void tearDown() {
        if (ntpThing != null) {
            Thing removedThing = thingRegistry.forceRemove(ntpThing.getUID());
            assertNotNull(removedThing);
        }

        if (testItem != null) {
            itemRegistry.remove(TEST_ITEM_NAME);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        // Stopping the local time server
        timeServer.stopServer();
        // Set the default time zone and locale to their initial value.
        TimeZone.setDefault(systemTimeZone);
        Locale.setDefault(locale);
    }

    @Test
    public void testStringChannelTimeZoneUpdate() {
        final String expectedTimeZonePDT = "PDT";
        final String expectedTimeZonePST = "PST";

        Configuration configuration = new Configuration();
        configuration.put(NtpBindingConstants.PROPERTY_TIMEZONE, TEST_TIME_ZONE_ID);
        Configuration channelConfig = new Configuration();
        /*
         * Set the format of the date, so it is updated in the item registry in
         * a format from which we can easily get the time zone.
         */
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, TEST_DATE_TIME_FORMAT);

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig, null);

        String timeZoneFromItemRegistry = getStringChannelTimeZoneFromItemRegistry();

        assertThat(timeZoneFromItemRegistry, is(anyOf(equalTo(expectedTimeZonePDT), equalTo(expectedTimeZonePST))));
    }

    @Test
    public void testDateTimeChannelTimeZoneUpdate() {

        Configuration configuration = new Configuration();
        configuration.put(NtpBindingConstants.PROPERTY_TIMEZONE, TEST_TIME_ZONE_ID);
        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null, null);

        String testItemState = getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME).toString();
        assertFormat(testItemState, DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS);
        ZonedDateTime timeZoneFromItemRegistry = ((DateTimeType) getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME))
                .getZonedDateTime();
        
        ZoneOffset expectedOffset = ZoneId.of(TEST_TIME_ZONE_ID).getRules().getOffset(timeZoneFromItemRegistry.toInstant());
        assertEquals(expectedOffset, timeZoneFromItemRegistry.getOffset());
    }

    @Test
    public void testDateTimeChannelCalendarTimeZoneUpdate() {
        Configuration configuration = new Configuration();
        configuration.put(NtpBindingConstants.PROPERTY_TIMEZONE, TEST_TIME_ZONE_ID);
        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null, null);
        ZonedDateTime timeZoneIdFromItemRegistry = ((DateTimeType) getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME))
                .getZonedDateTime();

        ZoneOffset expectedOffset = ZoneId.of(TEST_TIME_ZONE_ID).getRules().getOffset(timeZoneIdFromItemRegistry.toInstant());
        assertEquals(expectedOffset, timeZoneIdFromItemRegistry.getOffset());
    }

    @Test
    public void testStringChannelDefaultTimeZoneUpdate() {
        final String expectedTimeZoneEEST = "EEST";
        final String expectedTimeZoneEET = "EET";

        Configuration configuration = new Configuration();
        Configuration channelConfig = new Configuration();
        /*
         * Set the format of the date, so it is updated in the item registry in
         * a format from which we can easily get the time zone.
         */
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, TEST_DATE_TIME_FORMAT);

        // Initialize with configuration with no time zone property set.
        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, null, null);

        String timeZoneFromItemRegistry = getStringChannelTimeZoneFromItemRegistry();

        assertThat(timeZoneFromItemRegistry, is(anyOf(equalTo(expectedTimeZoneEEST), equalTo(expectedTimeZoneEET))));
    }

    @Test
    public void testDateTimeChannelDefaultTimeZoneUpdate() {
        ZonedDateTime zoned = ZonedDateTime.now();

        ZoneOffset expectedTimeZone = zoned.getOffset();
        Configuration configuration = new Configuration();
        // Initialize with configuration with no time zone property set.
        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null, null);

        String testItemState = getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME).toString();
        assertFormat(testItemState, DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS);
        ZoneOffset timeZoneFromItemRegistry = new DateTimeType(testItemState).getZonedDateTime().getOffset();

        assertEquals(expectedTimeZone, timeZoneFromItemRegistry);
    }

    @Test
    @Ignore("https://github.com/eclipse/smarthome/issues/5224")
    public void testDateTimeChannelCalendarDefaultTimeZoneUpdate() {
        Configuration configuration = new Configuration();
        // Initialize with configuration with no time zone property set.
        initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null, null);

        ZonedDateTime timeZoneIdFromItemRegistry = ((DateTimeType) getItemState(ACCEPTED_ITEM_TYPE_DATE_TIME))
                .getZonedDateTime();

        ZoneOffset expectedOffset = ZoneId.systemDefault().getRules().getOffset(timeZoneIdFromItemRegistry.toInstant());
        assertEquals(expectedOffset, timeZoneIdFromItemRegistry.getOffset());
    }

    @Test
    public void testStringChannelFormatting() {
        final String formatPattern = "EEE, d MMM yyyy HH:mm:ss z";

        Configuration configuration = new Configuration();
        Configuration channelConfig = new Configuration();
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, formatPattern);
        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig, null);

        String dateFromItemRegistry = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString();

        assertFormat(dateFromItemRegistry, formatPattern);
    }

    @Test
    public void testStringChannelDefaultFormatting() {
        Configuration configuration = new Configuration();
        // Initialize with configuration with no property for formatting set.
        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, null, null);

        String dateFromItemRegistryString = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString();

        assertFormat(dateFromItemRegistryString, NtpHandler.DATE_PATTERN_WITH_TZ);
    }

    @Test
    public void testEmptyStringPropertyFormatting() {
        Configuration configuration = new Configuration();
        Configuration channelConfig = new Configuration();
        // Empty string
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, "");

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig, null);

        String dateFromItemRegistry = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString();

        assertFormat(dateFromItemRegistry, NtpHandler.DATE_PATTERN_WITH_TZ);
    }

    @Test
    public void testNullPropertyFormatting() {
        Configuration configuration = new Configuration();
        Configuration channelConfig = new Configuration();
        channelConfig.put(NtpBindingConstants.PROPERTY_DATE_TIME_FORMAT, null);

        initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, channelConfig, null);

        String dateFromItemRegistry = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString();

        assertFormat(dateFromItemRegistry, NtpHandler.DATE_PATTERN_WITH_TZ);
    }

    @Test
    public void testDateTimeChannelWithUnknownHost() {
        assertCommunicationError(ACCEPTED_ITEM_TYPE_DATE_TIME);
    }

    @Test
    public void testStringChannelWithUnknownHost() {
        assertCommunicationError(ACCEPTED_ITEM_TYPE_STRING);
    }

    @Test
    public void testStringChannelHandleCommand() {
        assertEventIsReceived(UpdateEventType.HANDLE_COMMAND, NtpBindingConstants.CHANNEL_STRING,
                ACCEPTED_ITEM_TYPE_STRING);
    }

    @Test
    public void testDateTimeChannelHandleCommand() {
        assertEventIsReceived(UpdateEventType.HANDLE_COMMAND, NtpBindingConstants.CHANNEL_DATE_TIME,
                ACCEPTED_ITEM_TYPE_DATE_TIME);
    }

    @Test
    public void testStringChannelLinking() {
        assertEventIsReceived(UpdateEventType.CHANNEL_LINKED, NtpBindingConstants.CHANNEL_STRING,
                ACCEPTED_ITEM_TYPE_STRING);
    }

    @Test
    public void testDateTimeChannelLinking() {
        assertEventIsReceived(UpdateEventType.CHANNEL_LINKED, NtpBindingConstants.CHANNEL_DATE_TIME,
                ACCEPTED_ITEM_TYPE_DATE_TIME);
    }

    private void initialize(Configuration configuration, String channelID, String acceptedItemType,
            Configuration channelConfiguration, String wrongHostname) {
        // There are 2 tests which require wrong hostnames.
        boolean isWrongHostNameTest = wrongHostname != null;
        if (isWrongHostNameTest) {
            configuration.put(NtpBindingConstants.PROPERTY_NTP_SERVER_HOST, wrongHostname);
        } else {
            configuration.put(NtpBindingConstants.PROPERTY_NTP_SERVER_HOST, TEST_HOSTNAME);
        }
        initialize(configuration, channelID, acceptedItemType, channelConfiguration);
    }

    private void initialize(Configuration configuration, String channelID, String acceptedItemType,
            Configuration channelConfiguration) {

        configuration.put(NtpBindingConstants.PROPERTY_NTP_SERVER_PORT, TEST_PORT);
        ThingUID ntpUid = new ThingUID(NtpBindingConstants.THING_TYPE_NTP, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(ntpUid, channelID);
        Channel channel = ChannelBuilder.create(channelUID, acceptedItemType).withType(channelTypeUID)
                .withConfiguration(channelConfiguration).withLabel("label").withKind(ChannelKind.STATE).build();

        ntpThing = ThingBuilder.create(NtpBindingConstants.THING_TYPE_NTP, ntpUid).withConfiguration(configuration)
                .withChannel(channel).build();

        managedThingProvider.add(ntpThing);

        // Wait for the NTP thing to be added to the ManagedThingProvider.
        ntpHandler = waitForAssert(() -> {
            final ThingHandler thingHandler = ntpThing.getHandler();
            assertThat(thingHandler, is(instanceOf(NtpHandler.class)));
            return (NtpHandler) thingHandler;
        }, DFL_TIMEOUT * 3, DFL_SLEEP_TIME);

        if (acceptedItemType.equals(ACCEPTED_ITEM_TYPE_STRING)) {
            testItem = new StringItem(TEST_ITEM_NAME);
        } else if (acceptedItemType.equals(ACCEPTED_ITEM_TYPE_DATE_TIME)) {
            testItem = new DateTimeItem(TEST_ITEM_NAME);
        }

        itemRegistry.add(testItem);

        // Wait for the item , linked to the NTP thing to be added to the
        // ManagedThingProvider.
        final ManagedItemChannelLinkProvider itemChannelLinkProvider = waitForAssert(() -> {
            final ManagedItemChannelLinkProvider tmp = getService(ManagedItemChannelLinkProvider.class);
            assertNotNull(tmp);
            return tmp;
        });
        itemChannelLinkProvider.add(new ItemChannelLink(TEST_ITEM_NAME, channelUID));
    }

    private State getItemState(String acceptedItemType) {
        final Item testItem = waitForAssert(() -> {
            Item tmp;
            try {
                tmp = itemRegistry.getItem(TEST_ITEM_NAME);
            } catch (ItemNotFoundException e) {
                tmp = null;
            }
            assertNotNull(tmp);
            return tmp;
        });

        return waitForAssert(() -> {
            final State testItemState = testItem.getState();
            if (acceptedItemType.equals(ACCEPTED_ITEM_TYPE_STRING)) {
                assertThat(testItemState, is(instanceOf(StringType.class)));
            } else if (acceptedItemType.equals(ACCEPTED_ITEM_TYPE_DATE_TIME)) {
                assertThat(testItemState, is(instanceOf(DateTimeType.class)));
            }
            return testItemState;
        }, 3 * DFL_TIMEOUT, 2 * DFL_SLEEP_TIME);
    }

    private String getStringChannelTimeZoneFromItemRegistry() {
        String itemState = getItemState(ACCEPTED_ITEM_TYPE_STRING).toString();
        String timeZoneFromItemRegistry = StringUtils.substringAfterLast(itemState, " ");
        return timeZoneFromItemRegistry;
    }

    private void assertFormat(String initialDate, String formatPattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);

        final ZonedDateTime date;
        date = ZonedDateTime.parse(initialDate, formatter);

        String formattedDate = formatter.format(date);

        assertEquals(initialDate, formattedDate);
    }

    private void assertCommunicationError(String acceptedItemType) {
        Configuration configuration = new Configuration();
        final String WRONG_HOSTNAME = "wrong.hostname";
        if (acceptedItemType.equals(ACCEPTED_ITEM_TYPE_DATE_TIME)) {
            initialize(configuration, NtpBindingConstants.CHANNEL_DATE_TIME, ACCEPTED_ITEM_TYPE_DATE_TIME, null,
                    WRONG_HOSTNAME);
        } else if (acceptedItemType.equals(ACCEPTED_ITEM_TYPE_STRING)) {
            initialize(configuration, NtpBindingConstants.CHANNEL_STRING, ACCEPTED_ITEM_TYPE_STRING, null,
                    WRONG_HOSTNAME);
        }
        waitForAssert(() -> {
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, ntpThing.getStatusInfo().getStatusDetail());
        });
    }

    private void assertEventIsReceived(UpdateEventType updateEventType, String channelID, String acceptedItemType) {
        Configuration configuration = new Configuration();
        initialize(configuration, channelID, acceptedItemType, null, null);

        EventSubscriber eventSubscriberMock = mock(EventSubscriber.class);
        when(eventSubscriberMock.getSubscribedEventTypes()).thenReturn(Collections.singleton(ItemStateEvent.TYPE));
        registerService(eventSubscriberMock);

        if (updateEventType.equals(UpdateEventType.HANDLE_COMMAND)) {
            ntpHandler.handleCommand(new ChannelUID("ntp:test:chan:1"), new StringType("test"));
        } else if (updateEventType.equals(UpdateEventType.CHANNEL_LINKED)) {
            ntpHandler.channelLinked(new ChannelUID("ntp:test:chan:1"));
        }
        waitForAssert(() -> {
            verify(eventSubscriberMock, atLeastOnce()).receive(ArgumentMatchers.any(Event.class));
        });
    }

}
