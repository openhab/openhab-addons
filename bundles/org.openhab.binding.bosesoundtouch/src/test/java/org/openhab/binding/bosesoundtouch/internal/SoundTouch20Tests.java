/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bosesoundtouch.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.bosesoundtouch.internal.handler.BoseSoundTouchHandler;
import org.openhab.binding.bosesoundtouch.internal.handler.InMemmoryContentStorage;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 *
 * @author Leo Siepel - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class SoundTouch20Tests {

    private @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;
    private @NonNullByDefault({}) Thing soundTouchThing;
    private @NonNullByDefault({}) BoseSoundTouchHandler thingHandler;
    private @NonNullByDefault({}) XMLResponseProcessor processor;
    private ThingUID thingUID = new ThingUID(BoseSoundTouchBindingConstants.BINDING_ID, "soundtouch20");
    private ChannelUID volumeChannelUID = new ChannelUID(thingUID, BoseSoundTouchBindingConstants.CHANNEL_VOLUME);
    private ChannelUID presetChannelUID = new ChannelUID(thingUID, BoseSoundTouchBindingConstants.CHANNEL_PRESET);
    private Storage<@NonNull ContentItem> storage = new InMemmoryContentStorage();
    private @Mock @NonNullByDefault({}) BoseStateDescriptionOptionProvider stateDescriptionProvider;

    @BeforeEach
    public void initialize() {
        // arrange
        Configuration config = new Configuration();
        config.put(BoseSoundTouchConfiguration.MAC_ADDRESS, "B0D5CC1AAAA1");

        soundTouchThing = ThingBuilder.create(BoseSoundTouchBindingConstants.BST_20_THING_TYPE_UID, thingUID)
                .withConfiguration(config).withChannel(ChannelBuilder.create(volumeChannelUID, "Number").build())
                .withChannel(ChannelBuilder.create(presetChannelUID, "Number").build()).build();

        PresetContainer container = new PresetContainer(storage);
        thingHandler = new BoseSoundTouchHandler(soundTouchThing, container, stateDescriptionProvider);
        processor = new XMLResponseProcessor(thingHandler);
    }

    private void processIncomingMessage(String mesage) {
        try {
            processor.handleMessage(mesage);
        } catch (Exception e) {
            assert false : MessageFormat.format("handleMessage throws an exception: {0} Stacktrace: {1}",
                    e.getMessage(), e.getStackTrace());
        }
    }

    @Test
    public void configurationPropertyUpdated() {
        // arange
        CommandExecutor executor = new CommandExecutor(thingHandler);
        thingHandler.setCommandExecutor(executor);
        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><msg><header deviceID=\"B0D5CC1AAAA1\" url=\"info\" method=\"GET\"><request requestID=\"0\" msgType=\"RESPONSE\"><info type=\"new\" /></request></header><body><info deviceID=\"B0D5CC1AAAA1\"><name>livingroom</name><type>SoundTouch 20</type><margeAccountUUID>3504027</margeAccountUUID><components><component><componentCategory>SCM</componentCategory><softwareVersion>27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29</softwareVersion><serialNumber>U6148010803720048000100</serialNumber></component><component><componentCategory>PackagedProduct</componentCategory><serialNumber>069430P5227013812</serialNumber></component></components><margeURL>https://streaming.bose.com</margeURL><networkInfo type=\"SCM\"><macAddress>B0D5CC1AAAA1</macAddress><ipAddress>192.168.1.1</ipAddress></networkInfo><networkInfo type=\"SMSC\"><macAddress>5CF821E2FD76</macAddress><ipAddress>192.168.1.1</ipAddress></networkInfo><moduleType>sm2</moduleType><variant>spotty</variant><variantMode>normal</variantMode><countryCode>GB</countryCode><regionCode>GB</regionCode></info></body></msg>";

        // act
        processIncomingMessage(message);

        // assert
        assertEquals("27.0.6.46330.5043500",
                soundTouchThing.getProperties().get(org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION));
    }

    @Test
    public void channelVolumeUpdated() {
        // arrange
        CommandExecutor executor = new CommandExecutor(thingHandler);

        thingHandler.setCommandExecutor(executor);
        Mockito.when(thingHandlerCallback.isChannelLinked((ChannelUID) notNull())).thenReturn(true);
        thingHandler.setCallback(thingHandlerCallback);
        String message = "<updates deviceID=\"B0D5CC1AAAA1\"><volumeUpdated><volume><actualvolume>27</actualvolume></volume></volumeUpdated></updates>";

        // act
        processIncomingMessage(message);

        // assert
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(volumeChannelUID), eq(new PercentType("27")));
    }

    @Test
    @Disabled
    public void channelPresetUpdated() {
        // arrange
        CommandExecutor executor = new CommandExecutor(thingHandler);

        thingHandler.setCommandExecutor(executor);
        Mockito.when(thingHandlerCallback.isChannelLinked((ChannelUID) notNull())).thenReturn(true);
        thingHandler.setCallback(thingHandlerCallback);
        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><presets><preset id=\"1\" createdOn=\"1502124154\" updatedOn=\"1644607971\"><ContentItem source=\"TUNEIN\" type=\"stationurl\" location=\"/v1/playback/station/s25077\" sourceAccount=\"\" isPresetable=\"true\"><itemName>Radio FM1</itemName><containerArt>http://cdn-profiles.tunein.com/s25077/images/logoq.jpg</containerArt></ContentItem></preset><preset id=\"2\" createdOn=\"1485893875\" updatedOn=\"1612895566\"><ContentItem source=\"STORED_MUSIC\" location=\"22$2955\" sourceAccount=\"00113254-f4eb-0011-ebf4-ebf454321100/0\" isPresetable=\"true\"><itemName>Medicine At Midnight</itemName><containerArt /></ContentItem></preset><preset id=\"3\" createdOn=\"1506167722\" updatedOn=\"1506167722\"><ContentItem source=\"STORED_MUSIC\" location=\"22$1421\" sourceAccount=\"00113254-f4eb-0011-ebf4-ebf454321100/0\" isPresetable=\"true\"><itemName>Concrete &amp; Gold</itemName><containerArt /></ContentItem></preset><preset id=\"4\" createdOn=\"1444146657\" updatedOn=\"1542740566\"><ContentItem source=\"TUNEIN\" type=\"stationurl\" location=\"/v1/playback/station/s24896\" sourceAccount=\"\" isPresetable=\"true\"><itemName>SWR3</itemName><containerArt>http://radiotime-logos.s3.amazonaws.com/s24896q.png</containerArt></ContentItem></preset><preset id=\"5\" createdOn=\"1468517184\" updatedOn=\"1542740566\"><ContentItem source=\"TUNEIN\" type=\"stationurl\" location=\"/v1/playback/station/s103302\" sourceAccount=\"\" isPresetable=\"true\"><itemName>SRF 3</itemName><containerArt>http://radiotime-logos.s3.amazonaws.com/s24862q.png</containerArt></ContentItem></preset><preset id=\"6\" createdOn=\"1481548081\" updatedOn=\"1524211387\"><ContentItem source=\"STORED_MUSIC\" location=\"22$882\" sourceAccount=\"00113254-f4eb-0011-ebf4-ebf454321100/0\" isPresetable=\"true\"><itemName>Sonic Highways</itemName><containerArt /></ContentItem></preset></presets>";

        // act
        processIncomingMessage(message);

        // assert
        // TODO: check if preset channels have changed
        Mockito.verify(thingHandlerCallback).stateUpdated(eq(presetChannelUID), eq(new StringListType("27")));
    }
}
