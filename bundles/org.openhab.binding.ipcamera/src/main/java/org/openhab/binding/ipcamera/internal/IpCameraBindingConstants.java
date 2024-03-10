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
package org.openhab.binding.ipcamera.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IpCameraBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpCameraBindingConstants {
    private static final String BINDING_ID = "ipcamera";
    public static final String AUTH_HANDLER = "authorizationHandler";
    public static final String AMCREST_HANDLER = "amcrestHandler";
    public static final String COMMON_HANDLER = "commonHandler";
    public static final String INSTAR_HANDLER = "instarHandler";
    public static final String REOLINK_HANDLER = "reolinkHandler";
    public static final String HIKVISION_HANDLER = "hikvisionHandler";

    public enum FFmpegFormat {
        HLS,
        GIF,
        RECORD,
        RTSP_ALARMS,
        MJPEG,
        SNAPSHOT
    }

    public static final BigDecimal BIG_DECIMAL_SCALE_MOTION = new BigDecimal(5000);
    public static final long HLS_STARTUP_DELAY_MS = 4500;
    @SuppressWarnings("null")
    public static final int SERVLET_PORT = Integer.getInteger("org.osgi.service.http.port", 8080);

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");
    public static final String GENERIC_THING = "generic";
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, GENERIC_THING);
    public static final String ONVIF_THING = "onvif";
    public static final ThingTypeUID THING_TYPE_ONVIF = new ThingTypeUID(BINDING_ID, ONVIF_THING);
    public static final String AMCREST_THING = "amcrest";
    public static final ThingTypeUID THING_TYPE_AMCREST = new ThingTypeUID(BINDING_ID, AMCREST_THING);
    public static final String FOSCAM_THING = "foscam";
    public static final ThingTypeUID THING_TYPE_FOSCAM = new ThingTypeUID(BINDING_ID, FOSCAM_THING);
    public static final String HIKVISION_THING = "hikvision";
    public static final ThingTypeUID THING_TYPE_HIKVISION = new ThingTypeUID(BINDING_ID, HIKVISION_THING);
    public static final String INSTAR_THING = "instar";
    public static final ThingTypeUID THING_TYPE_INSTAR = new ThingTypeUID(BINDING_ID, INSTAR_THING);
    public static final String DAHUA_THING = "dahua";
    public static final ThingTypeUID THING_TYPE_DAHUA = new ThingTypeUID(BINDING_ID, DAHUA_THING);
    public static final String DOORBIRD_THING = "doorbird";
    public static final ThingTypeUID THING_TYPE_DOORBIRD = new ThingTypeUID(BINDING_ID, DOORBIRD_THING);
    public static final String REOLINK_THING = "reolink";
    public static final ThingTypeUID THING_TYPE_REOLINK = new ThingTypeUID(BINDING_ID, REOLINK_THING);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_ONVIF, THING_TYPE_GENERIC, THING_TYPE_AMCREST, THING_TYPE_DAHUA, THING_TYPE_INSTAR,
                    THING_TYPE_FOSCAM, THING_TYPE_DOORBIRD, THING_TYPE_HIKVISION, THING_TYPE_REOLINK));

    public static final Set<ThingTypeUID> GROUP_SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_GROUP));

    // List of all Thing Config items
    public static final String CONFIG_IPADDRESS = "ipAddress";
    public static final String CONFIG_ONVIF_PORT = "onvifPort";

    // List of all Channel ids
    public static final String CHANNEL_POLL_IMAGE = "pollImage";
    public static final String CHANNEL_RECORDING_GIF = "recordingGif";
    public static final String CHANNEL_GIF_HISTORY = "gifHistory";
    public static final String CHANNEL_GIF_HISTORY_LENGTH = "gifHistoryLength";
    public static final String CHANNEL_RECORDING_MP4 = "recordingMp4";
    public static final String CHANNEL_MP4_PREROLL = "mp4Preroll";
    public static final String CHANNEL_MP4_HISTORY = "mp4History";
    public static final String CHANNEL_MP4_HISTORY_LENGTH = "mp4HistoryLength";
    public static final String CHANNEL_IMAGE = "image";
    public static final String CHANNEL_RTSP_URL = "rtspUrl";
    public static final String CHANNEL_IMAGE_URL = "imageUrl";
    public static final String CHANNEL_MJPEG_URL = "mjpegUrl";
    public static final String CHANNEL_HLS_URL = "hlsUrl";
    public static final String CHANNEL_PAN = "pan";
    public static final String CHANNEL_TILT = "tilt";
    public static final String CHANNEL_ZOOM = "zoom";
    public static final String CHANNEL_EXTERNAL_MOTION = "externalMotion";
    public static final String CHANNEL_MOTION_ALARM = "motionAlarm";
    public static final String CHANNEL_LINE_CROSSING_ALARM = "lineCrossingAlarm";
    public static final String CHANNEL_FACE_DETECTED = "faceDetected";
    public static final String CHANNEL_ITEM_LEFT = "itemLeft";
    public static final String CHANNEL_ITEM_TAKEN = "itemTaken";
    public static final String CHANNEL_AUDIO_ALARM = "audioAlarm";
    public static final String CHANNEL_ENABLE_MOTION_ALARM = "enableMotionAlarm";
    public static final String CHANNEL_FFMPEG_MOTION_CONTROL = "ffmpegMotionControl";
    public static final String CHANNEL_FFMPEG_MOTION_ALARM = "ffmpegMotionAlarm";
    public static final String CHANNEL_ENABLE_LINE_CROSSING_ALARM = "enableLineCrossingAlarm";
    public static final String CHANNEL_ENABLE_AUDIO_ALARM = "enableAudioAlarm";
    public static final String CHANNEL_THRESHOLD_AUDIO_ALARM = "thresholdAudioAlarm";
    public static final String CHANNEL_ACTIVATE_ALARM_OUTPUT = "activateAlarmOutput";
    public static final String CHANNEL_ACTIVATE_ALARM_OUTPUT2 = "activateAlarmOutput2";
    public static final String CHANNEL_ENABLE_EXTERNAL_ALARM_INPUT = "enableExternalAlarmInput";
    public static final String CHANNEL_TRIGGER_EXTERNAL_ALARM_INPUT = "triggerExternalAlarmInput";
    public static final String CHANNEL_EXTERNAL_ALARM_INPUT = "externalAlarmInput";
    public static final String CHANNEL_EXTERNAL_ALARM_INPUT2 = "externalAlarmInput2";
    public static final String CHANNEL_AUTO_WHITE_LED = "autoWhiteLED";
    public static final String CHANNEL_AUTO_LED = "autoLED";
    public static final String CHANNEL_ENABLE_LED = "enableLED";
    public static final String CHANNEL_WHITE_LED = "whiteLED";
    public static final String CHANNEL_ENABLE_PIR_ALARM = "enablePirAlarm";
    public static final String CHANNEL_PIR_ALARM = "pirAlarm";
    public static final String CHANNEL_CELL_MOTION_ALARM = "cellMotionAlarm";
    public static final String CHANNEL_ENABLE_FIELD_DETECTION_ALARM = "enableFieldDetectionAlarm";
    public static final String CHANNEL_FIELD_DETECTION_ALARM = "fieldDetectionAlarm";
    public static final String CHANNEL_PARKING_ALARM = "parkingAlarm";
    public static final String CHANNEL_TAMPER_ALARM = "tamperAlarm";
    public static final String CHANNEL_TOO_DARK_ALARM = "tooDarkAlarm";
    public static final String CHANNEL_STORAGE_ALARM = "storageAlarm";
    public static final String CHANNEL_SCENE_CHANGE_ALARM = "sceneChangeAlarm";
    public static final String CHANNEL_TOO_BRIGHT_ALARM = "tooBrightAlarm";
    public static final String CHANNEL_TOO_BLURRY_ALARM = "tooBlurryAlarm";
    public static final String CHANNEL_TEXT_OVERLAY = "textOverlay";
    public static final String CHANNEL_EXTERNAL_LIGHT = "externalLight";
    public static final String CHANNEL_DOORBELL = "doorBell";
    public static final String CHANNEL_LAST_MOTION_TYPE = "lastMotionType";
    public static final String CHANNEL_LAST_EVENT_DATA = "lastEventData";
    public static final String CHANNEL_GOTO_PRESET = "gotoPreset";
    public static final String CHANNEL_START_STREAM = "startStream";
    public static final String CHANNEL_ENABLE_PRIVACY_MODE = "enablePrivacyMode";
    public static final String CHANNEL_CAR_ALARM = "carAlarm";
    public static final String CHANNEL_HUMAN_ALARM = "humanAlarm";
    public static final String CHANNEL_ANIMAL_ALARM = "animalAlarm";
    public static final String CHANNEL_ENABLE_FTP = "enableFTP";
    public static final String CHANNEL_ENABLE_EMAIL = "enableEmail";
    public static final String CHANNEL_ENABLE_PUSH = "enablePush";
    public static final String CHANNEL_ENABLE_RECORDINGS = "enableRecordings";
}
