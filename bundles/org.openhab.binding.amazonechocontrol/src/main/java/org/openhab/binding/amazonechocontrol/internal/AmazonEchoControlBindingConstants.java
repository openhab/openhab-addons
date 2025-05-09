/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.amazonechocontrol.internal.smarthome.AlexaColor;
import org.openhab.binding.amazonechocontrol.internal.util.ResourceUtil;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
    public static final ThingTypeUID THING_TYPE_SMART_HOME_DEVICE = new ThingTypeUID(BINDING_ID, "smartHomeDevice");
    public static final ThingTypeUID THING_TYPE_SMART_HOME_DEVICE_GROUP = new ThingTypeUID(BINDING_ID,
            "smartHomeDeviceGroup");

    public static final Set<ThingTypeUID> SUPPORTED_ECHO_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_ECHO,
            THING_TYPE_ECHO_SPOT, THING_TYPE_ECHO_SHOW, THING_TYPE_ECHO_WHA, THING_TYPE_FLASH_BRIEFING_PROFILE);

    public static final Set<ThingTypeUID> SUPPORTED_SMART_HOME_THING_TYPES_UIDS = Set.of(THING_TYPE_SMART_HOME_DEVICE,
            THING_TYPE_SMART_HOME_DEVICE_GROUP);

    // List of all Channel ids
    public static final String CHANNEL_ANNOUNCEMENT = "announcement";
    public static final String CHANNEL_SEND_MESSAGE = "sendMessage";
    public static final String CHANNEL_REFRESH_ACTIVITY = "refreshActivity";
    public static final String CHANNEL_PLAYER = "player";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_EQUALIZER_TREBLE = "equalizerTreble";
    public static final String CHANNEL_EQUALIZER_MIDRANGE = "equalizerMidrange";
    public static final String CHANNEL_EQUALIZER_BASS = "equalizerBass";
    public static final String CHANNEL_SHUFFLE = "shuffle";
    public static final String CHANNEL_IMAGE_URL = "imageUrl";
    public static final String CHANNEL_TITLE = "title";
    public static final String CHANNEL_SUBTITLE1 = "subtitle1";
    public static final String CHANNEL_SUBTITLE2 = "subtitle2";
    public static final String CHANNEL_PROVIDER_DISPLAY_NAME = "providerDisplayName";
    public static final String CHANNEL_BLUETOOTH_MAC = "bluetoothMAC";
    public static final String CHANNEL_BLUETOOTH = "bluetooth";
    public static final String CHANNEL_BLUETOOTH_DEVICE_NAME = "bluetoothDeviceName";
    public static final String CHANNEL_TEXT_TO_SPEECH = "textToSpeech";
    public static final String CHANNEL_TEXT_TO_SPEECH_VOLUME = "textToSpeechVolume";
    public static final String CHANNEL_TEXT_COMMAND = "textCommand";
    public static final String CHANNEL_REMIND = "remind";
    public static final String CHANNEL_PLAY_ALARM_SOUND = "playAlarmSound";
    public static final String CHANNEL_START_ROUTINE = "startRoutine";
    public static final String CHANNEL_MUSIC_PROVIDER_ID = "musicProviderId";
    public static final String CHANNEL_PLAY_MUSIC_VOICE_COMMAND = "playMusicVoiceCommand";
    public static final String CHANNEL_START_COMMAND = "startCommand";
    public static final String CHANNEL_LAST_VOICE_COMMAND = "lastVoiceCommand";
    public static final String CHANNEL_LAST_SPOKEN_TEXT = "lastSpokenText";
    public static final String CHANNEL_MEDIA_PROGRESS = "mediaProgress";
    public static final String CHANNEL_MEDIA_LENGTH = "mediaLength";
    public static final String CHANNEL_MEDIA_PROGRESS_TIME = "mediaProgressTime";
    public static final String CHANNEL_ASCENDING_ALARM = "ascendingAlarm";
    public static final String CHANNEL_DO_NOT_DISTURB = "doNotDisturb";
    public static final String CHANNEL_NOTIFICATION_VOLUME = "notificationVolume";
    public static final String CHANNEL_NEXT_REMINDER = "nextReminder";
    public static final String CHANNEL_NEXT_ALARM = "nextAlarm";
    public static final String CHANNEL_NEXT_MUSIC_ALARM = "nextMusicAlarm";
    public static final String CHANNEL_NEXT_TIMER = "nextTimer";
    public static final String CHANNEL_SAVE = "save";
    public static final String CHANNEL_ACTIVE = "active";
    public static final String CHANNEL_PLAY_ON_DEVICE = "playOnDevice";

    // List of all Properties
    public static final String DEVICE_PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String DEVICE_PROPERTY_FAMILY = "deviceFamily";
    public static final String DEVICE_PROPERTY_DEVICE_TYPE_ID = "deviceTypeId";
    public static final String DEVICE_PROPERTY_MANUFACTURER_NAME = "manufacturerName";
    public static final String DEVICE_PROPERTY_DEVICE_IDENTIFIER_LIST = "deviceIdentifierList";
    public static final String DEVICE_PROPERTY_FLASH_BRIEFING_PROFILE = "configurationJson";
    public static final String DEVICE_PROPERTY_ID = "id";

    // Other
    public static final String FLASH_BRIEFING_COMMAND_PREFIX = "FlashBriefing.";

    public static final String API_VERSION = "2.2.556530.0";
    public static final String DI_OS_VERSION = "16.6";
    public static final String DI_SDK_VERSION = "6.12.4";

    public static final Map<String, String> DEVICE_TYPES = ResourceUtil
            .readProperties(AmazonEchoControlBindingConstants.class, "device_type.properties");

    public static final JsonObject CAPABILITY_REGISTRATION = Objects.requireNonNull(
            ResourceUtil.getResourceStream(AmazonEchoControlBindingConstants.class, "registration_capabilities.json")
                    .map(inputStream -> new Gson().fromJson(new InputStreamReader(inputStream), JsonObject.class))
                    .orElseThrow(() -> new IllegalStateException("resource not found")));
    public static final List<AlexaColor> ALEXA_COLORS = ResourceUtil
            .readProperties(AlexaColor.class, "color.properties").entrySet().stream()
            .map(e -> new AlexaColor(e.getKey(), new HSBType(e.getValue()))).toList();
}
