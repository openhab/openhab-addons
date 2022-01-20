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
package org.openhab.binding.fsinternetradio.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.openhab.binding.fsinternetradio.internal.FSInternetRadioBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.fsinternetradio.internal.FSInternetRadioBindingConstants;
import org.openhab.binding.fsinternetradio.internal.handler.FSInternetRadioHandler;
import org.openhab.binding.fsinternetradio.internal.handler.HandlerUtils;
import org.openhab.binding.fsinternetradio.internal.radio.FrontierSiliconRadio;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.test.TestPortUtil;
import org.openhab.core.test.TestServer;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.UnDefType;

/**
 * OSGi tests for the {@link FSInternetRadioHandler}.
 *
 * @author Mihaela Memova - Initial contribution
 * @author Markus Rathgeb - Migrated from Groovy to pure Java test, made more robust
 * @author Velin Yordanov - Migrated to mockito
 *
 */
public class FSInternetRadioHandlerJavaTest extends JavaTest {
    private static final String DEFAULT_TEST_THING_NAME = "testRadioThing";
    private static final String DEFAULT_TEST_ITEM_NAME = "testItem";
    private final String VOLUME = "volume";

    // The request send for preset is "SET/netRemote.nav.action.selectPreset";
    private static final String PRESET = "Preset";
    private static final int TIMEOUT = 10 * 1000;
    private static final ThingTypeUID DEFAULT_THING_TYPE_UID = FSInternetRadioBindingConstants.THING_TYPE_RADIO;
    private static final ThingUID DEFAULT_THING_UID = new ThingUID(DEFAULT_THING_TYPE_UID, DEFAULT_TEST_THING_NAME);
    private static final RadioServiceDummy radioServiceDummy = new RadioServiceDummy();

    /**
     * In order to test a specific channel, it is necessary to create a Thing with two channels - CHANNEL_POWER
     * and the tested channel. So before each test, the power channel is created and added
     * to an ArrayList of channels. Then in the tests an additional channel is created and added to the ArrayList
     * when it's needed.
     */
    private Channel powerChannel;

    private ThingHandlerCallback callback;

    private static TestServer server;

    /**
     * A HashMap which saves all the 'channel-acceppted_item_type' pairs.
     * It is set before all the tests.
     */
    private static Map<String, String> acceptedItemTypes;

    /**
     * ArrayList of channels which is used to initialize a radioThing in the test cases.
     */
    private final List<Channel> channels = new ArrayList<>();

    private FSInternetRadioHandler radioHandler;
    private Thing radioThing;

    private static HttpClient httpClient;

    // default configuration properties
    private static final String DEFAULT_CONFIG_PROPERTY_IP = "127.0.0.1";
    private static final String DEFAULT_CONFIG_PROPERTY_PIN = "1234";
    private static final int DEFAULT_CONFIG_PROPERTY_PORT = TestPortUtil.findFreePort();

    /** The default refresh interval is 60 seconds. For the purposes of the tests it is set to 1 second */
    private static final String DEFAULT_CONFIG_PROPERTY_REFRESH = "1";
    private static final Configuration DEFAULT_COMPLETE_CONFIGURATION = createDefaultConfiguration();

    @BeforeAll
    public static void setUpClass() throws Exception {
        ServletHolder holder = new ServletHolder(radioServiceDummy);
        server = new TestServer(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PORT, TIMEOUT, holder);
        setTheChannelsMap();
        server.startServer();
        httpClient = new HttpClient();
        httpClient.start();
    }

    @BeforeEach
    public void setUp() {
        createThePowerChannel();
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        server.stopServer();
        httpClient.stop();
    }

    private static @NonNull Channel getChannel(final @NonNull Thing thing, final @NonNull String channelId) {
        final Channel channel = thing.getChannel(channelId);
        assertNotNull(channel);
        Objects.requireNonNull(channel);
        return channel;
    }

    private static @NonNull ChannelUID getChannelUID(final @NonNull Thing thing, final @NonNull String channelId) {
        final ChannelUID channelUID = getChannel(thing, channelId).getUID();
        assertNotNull(channelUID);
        return channelUID;
    }

    /**
     * Verify OFFLINE Thing status when the IP is NULL.
     */
    @Test
    public void offlineIfNullIp() {
        Configuration config = createConfiguration(null, DEFAULT_CONFIG_PROPERTY_PIN,
                String.valueOf(DEFAULT_CONFIG_PROPERTY_PORT), DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithNullIP = initializeRadioThing(config);
        testRadioThingConsideringConfiguration(radioThingWithNullIP);
    }

    /**
     * Verify OFFLINE Thing status when the PIN is empty String.
     */
    @Test
    public void offlineIfEmptyPIN() {
        Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, "",
                String.valueOf(DEFAULT_CONFIG_PROPERTY_PORT), DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithEmptyPIN = initializeRadioThing(config);
        testRadioThingConsideringConfiguration(radioThingWithEmptyPIN);
    }

    /**
     * Verify OFFLINE Thing status when the PORT is zero.
     */
    @Test
    public void offlineIfZeroPort() {
        Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PIN, "0",
                DEFAULT_CONFIG_PROPERTY_REFRESH);
        Thing radioThingWithZeroPort = initializeRadioThing(config);
        testRadioThingConsideringConfiguration(radioThingWithZeroPort);
    }

    /**
     * Verify OFFLINE Thing status when the PIN is wrong.
     */
    @Test
    public void offlineIfWrongPIN() {
        final String wrongPin = "5678";
        Configuration config = createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, wrongPin,
                String.valueOf(DEFAULT_CONFIG_PROPERTY_PORT), DEFAULT_CONFIG_PROPERTY_REFRESH);
        initializeRadioThing(config);
        waitForAssert(() -> {
            String exceptionMessage = "Radio does not allow connection, maybe wrong pin?";
            verifyCommunicationError(exceptionMessage);
        });
    }

    /**
     * Verify OFFLINE Thing status when the HTTP response cannot be parsed correctly.
     */
    @Test
    public void offlineIfParseError() {
        // create a thing with two channels - the power channel and any of the others
        String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE;
        String acceptedItemType = acceptedItemTypes.get(modeChannelID);
        createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        ChannelUID modeChannelUID = getChannelUID(radioThing, modeChannelID);

        /*
         * Setting the isInvalidResponseExpected variable to true
         * in order to get the incorrect XML response from the servlet
         */
        radioServiceDummy.setInvalidResponse(true);

        // try to handle a command
        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"));

        waitForAssert(() -> {
            String exceptionMessage = "java.io.IOException: org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 2;";
            verifyCommunicationError(exceptionMessage);
        });
        radioServiceDummy.setInvalidResponse(false);
    }

    /**
     * Verify the HTTP status is handled correctly when it is not OK_200.
     */
    @Test
    public void httpStatusNokHandling() {
        // create a thing with two channels - the power channel and any of the others
        String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE;
        String acceptedItemType = acceptedItemTypes.get(modeChannelID);
        createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        // turn-on the radio
        turnTheRadioOn(radioThing);

        /*
         * Setting the needed boolean variable to false, so we can be sure
         * that the XML response won't have a OK_200 status
         */
        ChannelUID modeChannelUID = getChannelUID(radioThing, modeChannelID);
        Item modeTestItem = initializeItem(modeChannelUID, CHANNEL_MODE, acceptedItemType);

        // try to handle a command
        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"));

        waitForAssert(() -> {
            assertSame(UnDefType.NULL, modeTestItem.getState());
        });
    }

    /**
     * Verify ONLINE status of a Thing with complete configuration.
     */
    @Test
    public void verifyOnlineStatus() {
        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);
    }

    /**
     * Verify the power channel is updated.
     */
    @Test
    public void powerChannelUpdated() {
        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        ChannelUID powerChannelUID = powerChannel.getUID();
        initializeItem(powerChannelUID, DEFAULT_TEST_ITEM_NAME,
                acceptedItemTypes.get(FSInternetRadioBindingConstants.CHANNEL_POWER));

        radioHandler.handleCommand(powerChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(1, CHANNEL_POWER),
                    "We should be able to turn on the radio");
            radioServiceDummy.clearRequestParameters();
        });

        radioHandler.handleCommand(powerChannelUID, OnOffType.OFF);
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(0, CHANNEL_POWER),
                    "We should be able to turn off the radio");
            radioServiceDummy.clearRequestParameters();
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        radioHandler.handleCommand(powerChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(1, CHANNEL_POWER),
                    "We should be able to turn on the radio");
            radioServiceDummy.clearRequestParameters();
        });
    }

    /**
     * Verify the mute channel is updated.
     */
    @Test
    public void muteChhannelUpdated() {
        String muteChannelID = FSInternetRadioBindingConstants.CHANNEL_MUTE;
        String acceptedItemType = acceptedItemTypes.get(muteChannelID);
        createChannel(DEFAULT_THING_UID, muteChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID muteChannelUID = getChannelUID(radioThing, FSInternetRadioBindingConstants.CHANNEL_MUTE);
        initializeItem(muteChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        radioHandler.handleCommand(muteChannelUID, OnOffType.ON);
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(1, CHANNEL_MUTE),
                    "We should be able to mute the radio");
            radioServiceDummy.clearRequestParameters();
        });

        radioHandler.handleCommand(muteChannelUID, OnOffType.OFF);
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(0, CHANNEL_MUTE),
                    "We should be able to unmute the radio");
            radioServiceDummy.clearRequestParameters();
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
    }

    /**
     * Verify the mode channel is updated.
     */
    @Test
    public void modeChannelUdpated() {
        String modeChannelID = FSInternetRadioBindingConstants.CHANNEL_MODE;
        String acceptedItemType = acceptedItemTypes.get(modeChannelID);
        createChannel(DEFAULT_THING_UID, modeChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID modeChannelUID = getChannelUID(radioThing, modeChannelID);
        initializeItem(modeChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("1"));
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(1, CHANNEL_MODE),
                    "We should be able to update the mode channel correctly");
            radioServiceDummy.clearRequestParameters();
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */
        radioHandler.handleCommand(modeChannelUID, DecimalType.valueOf("3"));
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(3, CHANNEL_MODE),
                    "We should be able to update the mode channel correctly");
            radioServiceDummy.clearRequestParameters();
        });
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_ABSOLUTE using INCREASE and DECREASE commands.
     */
    @Test
    public void volumechannelUpdatedAbsIncDec() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = getChannelUID(radioThing, absoluteVolumeChannelID);
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        testChannelWithINCREASEAndDECREASECommands(absoluteVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_ABSOLUTE using UP and DOWN commands.
     */
    @Test
    public void volumeChannelUpdatedAbsUpDown() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = getChannelUID(radioThing, absoluteVolumeChannelID);
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        testChannelWithUPAndDOWNCommands(absoluteVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the invalid values when updating CHANNEL_VOLUME_ABSOLUTE are handled correctly.
     */
    @Test
    public void invalidAbsVolumeValues() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = getChannelUID(radioThing, absoluteVolumeChannelID);
        initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME, absoluteAcceptedItemType);

        // Trying to set a value that is greater than the maximum volume
        radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("36"));

        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(32, VOLUME),
                    "The volume should not exceed the maximum value");
            radioServiceDummy.clearRequestParameters();
        });

        // Trying to increase the volume more than its maximum value using the INCREASE command
        radioHandler.handleCommand(absoluteVolumeChannelUID, IncreaseDecreaseType.INCREASE);

        waitForAssert(() -> {
            assertTrue(radioServiceDummy.areRequestParametersEmpty(),
                    "The volume should not be increased above the maximum value");
            radioServiceDummy.clearRequestParameters();
        });

        // Trying to increase the volume more than its maximum value using the UP command
        radioHandler.handleCommand(absoluteVolumeChannelUID, UpDownType.UP);

        waitForAssert(() -> {
            assertTrue(radioServiceDummy.areRequestParametersEmpty(),
                    "The volume should not be increased above the maximum value");
            radioServiceDummy.clearRequestParameters();
        });

        // Trying to set a value that is lower than the minimum volume value
        radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("-10"));
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(0, VOLUME),
                    "The volume should not be decreased below 0");
            radioServiceDummy.clearRequestParameters();
        });

        /*
         * Setting the needed boolean variable to true, so we can be sure
         * that an invalid value will be returned in the XML response
         */

        // trying to set the volume
        radioHandler.handleCommand(absoluteVolumeChannelUID, DecimalType.valueOf("15"));
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(15, VOLUME),
                    "We should be able to set the volume correctly");
            radioServiceDummy.clearRequestParameters();
        });
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_PERCENT using INCREASE and DECREASE commands.
     */
    @Test
    public void volumeChannelUpdatedPercIncDec() {
        /*
         * The volume is set through the CHANNEL_VOLUME_PERCENT in order to check if
         * the absolute volume will be updated properly.
         */
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT;
        String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID);
        createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = getChannelUID(radioThing, absoluteVolumeChannelID);
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        ChannelUID percentVolumeChannelUID = getChannelUID(radioThing, percentVolumeChannelID);

        testChannelWithINCREASEAndDECREASECommands(percentVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the volume is updated through the CHANNEL_VOLUME_PERCENT using UP and DOWN commands.
     */
    @Test
    public void volumeChannelUpdatedPercUpDown() {
        /*
         * The volume is set through the CHANNEL_VOLUME_PERCENT in order to check if
         * the absolute volume will be updated properly.
         */
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT;
        String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID);
        createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = getChannelUID(radioThing, absoluteVolumeChannelID);
        Item volumeTestItem = initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME,
                absoluteAcceptedItemType);

        ChannelUID percentVolumeChannelUID = getChannelUID(radioThing, percentVolumeChannelID);

        testChannelWithUPAndDOWNCommands(percentVolumeChannelUID, volumeTestItem);
    }

    /**
     * Verify the valid and invalid values when updating CHANNEL_VOLUME_PERCENT are handled correctly.
     */
    @Test
    public void validInvalidPercVolume() {
        String absoluteVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE;
        String absoluteAcceptedItemType = acceptedItemTypes.get(absoluteVolumeChannelID);
        createChannel(DEFAULT_THING_UID, absoluteVolumeChannelID, absoluteAcceptedItemType);

        String percentVolumeChannelID = FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT;
        String percentAcceptedItemType = acceptedItemTypes.get(percentVolumeChannelID);
        createChannel(DEFAULT_THING_UID, percentVolumeChannelID, percentAcceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID absoluteVolumeChannelUID = getChannelUID(radioThing, absoluteVolumeChannelID);
        initializeItem(absoluteVolumeChannelUID, DEFAULT_TEST_ITEM_NAME, absoluteAcceptedItemType);

        ChannelUID percentVolumeChannelUID = getChannelUID(radioThing, percentVolumeChannelID);

        /*
         * Giving the handler a valid percent value. According to the FrontierSiliconRadio's
         * documentation 100 percents correspond to 32 absolute value
         */
        radioHandler.handleCommand(percentVolumeChannelUID, PercentType.valueOf("50"));
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(16, VOLUME),
                    "We should be able to set the volume correctly using percentages.");
            radioServiceDummy.clearRequestParameters();
        });

        radioHandler.handleCommand(percentVolumeChannelUID, PercentType.valueOf("15"));

        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(4, VOLUME),
                    "We should be able to set the volume correctly using percentages.");
            radioServiceDummy.clearRequestParameters();
        });
    }

    private void testChannelWithINCREASEAndDECREASECommands(ChannelUID channelUID, Item item) {
        synchronized (channelUID) {
            // First we have to make sure that the item state is 0
            radioHandler.handleCommand(channelUID, DecimalType.valueOf("0"));
            waitForAssert(() -> {
                assertTrue(radioServiceDummy.containsRequestParameter(1, CHANNEL_POWER),
                        "We should be able to turn on the radio");
                radioServiceDummy.clearRequestParameters();
            });

            radioHandler.handleCommand(channelUID, IncreaseDecreaseType.INCREASE);

            waitForAssert(() -> {
                assertTrue(radioServiceDummy.containsRequestParameter(1, VOLUME),
                        "We should be able to increase the volume correctly");
                radioServiceDummy.clearRequestParameters();
            });

            radioHandler.handleCommand(channelUID, IncreaseDecreaseType.DECREASE);
            waitForAssert(() -> {
                assertTrue(radioServiceDummy.containsRequestParameter(0, VOLUME),
                        "We should be able to increase the volume correctly");
                radioServiceDummy.clearRequestParameters();
            });

            // Trying to decrease one more time
            radioHandler.handleCommand(channelUID, IncreaseDecreaseType.DECREASE);
            waitForAssert(() -> {
                assertFalse(radioServiceDummy.containsRequestParameter(0, VOLUME),
                        "We should be able to decrease the volume correctly");
                radioServiceDummy.clearRequestParameters();
            });
        }
    }

    private void testChannelWithUPAndDOWNCommands(ChannelUID channelUID, Item item) {
        synchronized (channelUID) {
            // First we have to make sure that the item state is 0
            radioHandler.handleCommand(channelUID, DecimalType.valueOf("0"));
            waitForAssert(() -> {
                assertTrue(radioServiceDummy.containsRequestParameter(1, CHANNEL_POWER),
                        "We should be able to turn on the radio");
                radioServiceDummy.clearRequestParameters();
            });

            radioHandler.handleCommand(channelUID, UpDownType.UP);
            waitForAssert(() -> {
                assertTrue(radioServiceDummy.containsRequestParameter(1, VOLUME),
                        "We should be able to increase the volume correctly");
                radioServiceDummy.clearRequestParameters();
            });

            radioHandler.handleCommand(channelUID, UpDownType.DOWN);
            waitForAssert(() -> {
                assertTrue(radioServiceDummy.containsRequestParameter(0, VOLUME),
                        "We should be able to decrease the volume correctly");
                radioServiceDummy.clearRequestParameters();
            });

            // Trying to decrease one more time
            radioHandler.handleCommand(channelUID, UpDownType.DOWN);
            waitForAssert(() -> {
                assertTrue(radioServiceDummy.areRequestParametersEmpty(),
                        "We shouldn't be able to decrease the volume below 0");
                radioServiceDummy.clearRequestParameters();
            });
        }
    }

    /**
     * Verify the preset channel is updated.
     */
    @Test
    public void presetChannelUpdated() {
        String presetChannelID = FSInternetRadioBindingConstants.CHANNEL_PRESET;
        String acceptedItemType = acceptedItemTypes.get(presetChannelID);
        createChannel(DEFAULT_THING_UID, presetChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThing(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);
        turnTheRadioOn(radioThing);

        ChannelUID presetChannelUID = getChannelUID(radioThing, FSInternetRadioBindingConstants.CHANNEL_PRESET);
        initializeItem(presetChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        radioHandler.handleCommand(presetChannelUID, DecimalType.valueOf("100"));
        waitForAssert(() -> {
            assertTrue(radioServiceDummy.containsRequestParameter(100, PRESET),
                    "We should be able to set value to the preset");
            radioServiceDummy.clearRequestParameters();
        });
    }

    /**
     * Verify the playInfoName channel is updated.
     */
    @Test
    public void playInfoNameChannelUpdated() {
        String playInfoNameChannelID = FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME;
        String acceptedItemType = acceptedItemTypes.get(playInfoNameChannelID);
        createChannel(DEFAULT_THING_UID, playInfoNameChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThingWithMockedHandler(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);

        ChannelUID playInfoNameChannelUID = getChannelUID(radioThing,
                FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME);
        initializeItem(playInfoNameChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        waitForAssert(() -> {
            verifyOnlineStatusIsSet();
        });
    }

    /**
     * Verify the playInfoText channel is updated.
     */
    @Test
    public void playInfoTextChannelUpdated() {
        String playInfoTextChannelID = FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT;
        String acceptedItemType = acceptedItemTypes.get(playInfoTextChannelID);
        createChannel(DEFAULT_THING_UID, playInfoTextChannelID, acceptedItemType);

        Thing radioThing = initializeRadioThingWithMockedHandler(DEFAULT_COMPLETE_CONFIGURATION);
        testRadioThingConsideringConfiguration(radioThing);

        turnTheRadioOn(radioThing);
        ChannelUID playInfoTextChannelUID = getChannelUID(radioThing,
                FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT);
        initializeItem(playInfoTextChannelUID, DEFAULT_TEST_ITEM_NAME, acceptedItemType);

        waitForAssert(() -> {
            verifyOnlineStatusIsSet();
        });
    }

    private static Configuration createDefaultConfiguration() {
        return createConfiguration(DEFAULT_CONFIG_PROPERTY_IP, DEFAULT_CONFIG_PROPERTY_PIN,
                String.valueOf(DEFAULT_CONFIG_PROPERTY_PORT), DEFAULT_CONFIG_PROPERTY_REFRESH);
    }

    private static Configuration createConfiguration(String ip, String pin, String port, String refresh) {
        Configuration config = new Configuration();
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP, ip);
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PIN, pin);
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PORT, new BigDecimal(port));
        config.put(FSInternetRadioBindingConstants.CONFIG_PROPERTY_REFRESH, new BigDecimal(refresh));
        return config;
    }

    private static void setTheChannelsMap() {
        acceptedItemTypes = new HashMap<>();
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_POWER, "Switch");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_MODE, "Number");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_MUTE, "Switch");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_NAME, "String");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PLAY_INFO_TEXT, "String");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_PRESET, "Number");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_VOLUME_ABSOLUTE, "Number");
        acceptedItemTypes.put(FSInternetRadioBindingConstants.CHANNEL_VOLUME_PERCENT, "Dimmer");
    }

    private void createThePowerChannel() {
        String powerChannelID = FSInternetRadioBindingConstants.CHANNEL_POWER;
        String acceptedItemType = acceptedItemTypes.get(powerChannelID);
        powerChannel = createChannel(DEFAULT_THING_UID, powerChannelID, acceptedItemType);
    }

    private Item initializeItem(ChannelUID channelUID, String itemName, String acceptedItemType) {
        Item item = null;

        switch (acceptedItemType) {
            case "Number":
                item = new NumberItem(itemName);
                break;

            case "String":
                item = new StringItem(itemName);
                break;

            case "Switch":
                item = new SwitchItem(itemName);
                break;

            case "Dimmer":
                item = new DimmerItem(itemName);
                break;
        }

        return item;
    }

    private Channel createChannel(ThingUID thingUID, String channelID, String acceptedItemType) {
        ChannelUID channelUID = new ChannelUID(thingUID, channelID);

        Channel radioChannel = ChannelBuilder.create(channelUID, acceptedItemType).build();
        channels.add(radioChannel);
        return radioChannel;
    }

    private void testRadioThingConsideringConfiguration(Thing thing) {
        Configuration config = thing.getConfiguration();
        if (isConfigurationComplete(config)) {
            waitForAssert(() -> {
                verifyOnlineStatusIsSet();
            });
        } else {
            waitForAssert(() -> {
                verifyConfigurationError();
            });
        }
    }

    private boolean isConfigurationComplete(Configuration config) {
        String ip = (String) config.get(FSInternetRadioBindingConstants.CONFIG_PROPERTY_IP);
        BigDecimal port = (BigDecimal) config.get(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PORT.toString());
        String pin = (String) config.get(FSInternetRadioBindingConstants.CONFIG_PROPERTY_PIN.toString());

        if (ip == null || port.compareTo(BigDecimal.ZERO) == 0 || pin == null || pin.isEmpty()) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("null")
    private Thing initializeRadioThing(Configuration config) {
        radioThing = ThingBuilder.create(DEFAULT_THING_TYPE_UID, DEFAULT_THING_UID).withConfiguration(config)
                .withChannels(channels).build();

        callback = mock(ThingHandlerCallback.class);

        radioHandler = new FSInternetRadioHandler(radioThing, httpClient);
        radioHandler.setCallback(callback);
        radioThing.setHandler(radioHandler);
        radioThing.getHandler().initialize();

        return radioThing;
    }

    @SuppressWarnings("null")
    private Thing initializeRadioThingWithMockedHandler(Configuration config) {
        radioThing = ThingBuilder.create(DEFAULT_THING_TYPE_UID, DEFAULT_THING_UID).withConfiguration(config)
                .withChannels(channels).build();

        callback = mock(ThingHandlerCallback.class);

        radioHandler = new MockedRadioHandler(radioThing, httpClient);
        radioHandler.setCallback(callback);
        radioThing.setHandler(radioHandler);
        radioThing.getHandler().initialize();

        return radioThing;
    }

    private void turnTheRadioOn(Thing radioThing) {
        radioHandler.handleCommand(getChannelUID(radioThing, FSInternetRadioBindingConstants.CHANNEL_POWER),
                OnOffType.ON);

        final FrontierSiliconRadio radio = HandlerUtils.getRadio(radioHandler);

        waitForAssert(() -> {
            try {
                assertTrue(radio.getPower());
            } catch (IOException ex) {
                throw new AssertionError("I/O error", ex);
            }
        });
    }

    private void verifyOnlineStatusIsSet() {
        ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(ThingStatus.ONLINE,
                ThingStatusDetail.NONE);
        ThingStatusInfo statusInfo = statusBuilder.withDescription(null).build();
        verify(callback, atLeast(1)).statusUpdated(radioThing, statusInfo);
    }

    private void verifyConfigurationError() {
        ThingStatusInfoBuilder statusBuilder = ThingStatusInfoBuilder.create(ThingStatus.OFFLINE,
                ThingStatusDetail.CONFIGURATION_ERROR);
        ThingStatusInfo statusInfo = statusBuilder.withDescription("Configuration incomplete").build();
        verify(callback, atLeast(1)).statusUpdated(radioThing, statusInfo);
    }

    private void verifyCommunicationError(String exceptionMessage) {
        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeast(1)).statusUpdated(isA(Thing.class), captor.capture());
        ThingStatusInfo status = captor.getValue();
        assertThat(status.getStatus(), is(ThingStatus.OFFLINE));
        assertThat(status.getStatusDetail(), is(ThingStatusDetail.COMMUNICATION_ERROR));
        assertThat(status.getDescription().contains(exceptionMessage), is(true));
    }
}
