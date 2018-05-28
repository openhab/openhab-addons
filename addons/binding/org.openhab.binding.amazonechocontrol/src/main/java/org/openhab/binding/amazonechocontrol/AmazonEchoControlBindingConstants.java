/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link AmazonEchoControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class AmazonEchoControlBindingConstants {

    public static final String BINDING_ID = "amazonechocontrol";
    public static final String BINDING_NAME = "Amazon Echo Control";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_ECHO = new ThingTypeUID(BINDING_ID, "echo");
    public static final ThingTypeUID THING_TYPE_ECHO_SPOT = new ThingTypeUID(BINDING_ID, "echospot");
    public static final ThingTypeUID THING_TYPE_ECHO_SHOW = new ThingTypeUID(BINDING_ID, "echoshow");
    public static final ThingTypeUID THING_TYPE_ECHO_WHA = new ThingTypeUID(BINDING_ID, "wha");

    public static final ThingTypeUID THING_TYPE_FLASH_BRIEFING_PROFILE = new ThingTypeUID(BINDING_ID,
            "flashbriefingprofile");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_ACCOUNT, THING_TYPE_ECHO, THING_TYPE_ECHO_SPOT, THING_TYPE_ECHO_SHOW,
                    THING_TYPE_ECHO_WHA, THING_TYPE_FLASH_BRIEFING_PROFILE));

    // List of all Channel ids
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_ERROR = "error";
    public static final String CHANNEL_SHUFFLE = "shuffle";
    public static final String CHANNEL_LOOP = "loop";
    public static final String CHANNEL_IMAGE_URL = "imageUrl";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_SUBTITLE1 = "subtitle1";
    public static final String CHANNEL_SUBTITLE2 = "subtitle2";
    public static final String CHANNEL_PROVIDER_DISPLAY_NAME = "providerDisplayName";
    public static final String CHANNEL_BLUETOOTH_MAC = "bluetoothMAC";
    public static final String CHANNEL_BLUETOOTH = "bluetooth";
    public static final String CHANNEL_BLUETOOTH_DEVICE_NAME = "bluetoothDeviceName";
    public static final String CHANNEL_RADIO_STATION_ID = "radioStationId";
    public static final String CHANNEL_RADIO = "radio";
    public static final String CHANNEL_AMAZON_MUSIC_TRACK_ID = "amazonMusicTrackId";
    public static final String CHANNEL_AMAZON_MUSIC = "amazonMusic";
    public static final String CHANNEL_AMAZON_MUSIC_PLAY_LIST_ID = "amazonMusicPlayListId";
    public static final String CHANNEL_TEXT_TO_SPEECH = "textToSpeech";
    public static final String CHANNEL_REMIND = "remind";
    public static final String CHANNEL_PLAY_ALARM_SOUND = "playAlarmSound";
    public static final String CHANNEL_START_ROUTINE = "startRoutine";
    public static final String CHANNEL_MUSIC_PROVIDER_ID = "musicProviderId";
    public static final String CHANNEL_PLAY_MUSIC_VOICE_COMMAND = "playMusicVoiceCommand";
    public static final String CHANNEL_START_COMMAND = "startCommand";

    public static final String CHANNEL_SAVE = "save";
    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_PLAY_ON_DEVICE = "playOnDevice";

    // List of channel Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_BLUETHOOTH_MAC = new ChannelTypeUID(BINDING_ID, "bluetoothMAC");
    public static final ChannelTypeUID CHANNEL_TYPE_AMAZON_MUSIC_PLAY_LIST_ID = new ChannelTypeUID(BINDING_ID,
            "amazonMusicPlayListId");
    public static final ChannelTypeUID CHANNEL_TYPE_PLAY_ALARM_SOUND = new ChannelTypeUID(BINDING_ID, "playAlarmSound");
    public static final ChannelTypeUID CHANNEL_TYPE_CHANNEL_PLAY_ON_DEVICE = new ChannelTypeUID(BINDING_ID,
            "playOnDevice");
    public static final ChannelTypeUID CHANNEL_TYPE_MUSIC_PROVIDER_ID = new ChannelTypeUID(BINDING_ID,
            "musicProviderId");
    public static final ChannelTypeUID CHANNEL_TYPE_START_COMMAND = new ChannelTypeUID(BINDING_ID, "startCommand");

    // List of all Properties
    public static final String DEVICE_PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String DEVICE_PROPERTY_FAMILY = "deviceFamily";
    public static final String DEVICE_PROPERTY_FLASH_BRIEFING_PROFILE = "configurationJson";

    // Other
    public static final String FLASH_BRIEFING_COMMAND_PREFIX = "FlashBriefing.";
}
