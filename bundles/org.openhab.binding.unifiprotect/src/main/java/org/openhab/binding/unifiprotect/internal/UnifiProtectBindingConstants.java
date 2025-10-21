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
package org.openhab.binding.unifiprotect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UnifiProtectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectBindingConstants {

    public static final String BINDING_ID = "unifiprotect";
    public static final String SERVICE_ID = "org.openhab." + BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_NVR = new ThingTypeUID(BINDING_ID, "nvr");
    public static final ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public static final ThingTypeUID THING_TYPE_SENSOR = new ThingTypeUID(BINDING_ID, "sensor");

    // List of all Configuration ids
    public static final String DEVICE_ID = "deviceId";
    public static final String SNAPSHOT_SEQUENCE = "sequence";

    // List of all Property ids
    public static final String PROPERTY_WEBRTC_URL = "webrtc-url"; // also used as base url for all webrtc urls
    public static final String PROPERTY_SNAPSHOT_URL = "snapshot-url";

    // List of all Channel ids

    // Shared
    public static final String CHANNEL_STATUS = "status";

    // Camera
    public static final String CHANNEL_SNAPSHOT = "snapshot";
    public static final String CHANNEL_SNAPSHOT_LABEL = "Snapshot";
    public static final String CHANNEL_SNAPSHOT_URL = "snapshot-url";
    public static final String CHANNEL_SNAPSHOT_URL_LABEL = "Snapshot URL";
    public static final String CHANNEL_MIC_VOLUME = "mic-volume";
    public static final String CHANNEL_VIDEO_MODE = "video-mode";
    public static final String CHANNEL_HDR_TYPE = "hdr-type";
    public static final String CHANNEL_OSD_NAME = "osd-name";
    public static final String CHANNEL_OSD_DATE = "osd-date";
    public static final String CHANNEL_OSD_LOGO = "osd-logo";
    public static final String CHANNEL_LED_ENABLED = "led-enabled";
    public static final String CHANNEL_ACTIVE_PATROL_SLOT = "active-patrol-slot";
    public static final String CHANNEL_RTSP_URL_HIGH = "rtsp-url-high";
    public static final String CHANNEL_RTSP_URL_HIGH_LABEL = "RTSP URL High";
    public static final String CHANNEL_RTSP_URL_MEDIUM = "rtsp-url-medium";
    public static final String CHANNEL_RTSP_URL_MEDIUM_LABEL = "RTSP URL Medium";
    public static final String CHANNEL_RTSP_URL_LOW = "rtsp-url-low";
    public static final String CHANNEL_RTSP_URL_LOW_LABEL = "RTSP URL Low";
    public static final String CHANNEL_RTSP_URL_PACKAGE = "rtsp-url-package";
    public static final String CHANNEL_RTSP_URL_PACKAGE_LABEL = "RTSP URL Package";
    public static final String CHANNEL_RTSP_URL = "rtsp-url";
    public static final String CHANNEL_WEBRTC_URL = "webrtc-url";
    public static final String CHANNEL_WEBRTC_URL_HIGH = "webrtc-url-high";
    public static final String CHANNEL_WEBRTC_URL_HIGH_LABEL = "WebRTC URL High Quality";
    public static final String CHANNEL_WEBRTC_URL_MEDIUM = "webrtc-url-medium";
    public static final String CHANNEL_WEBRTC_URL_MEDIUM_LABEL = "WebRTC URL Medium Quality";
    public static final String CHANNEL_WEBRTC_URL_LOW = "webrtc-url-low";
    public static final String CHANNEL_WEBRTC_URL_LOW_LABEL = "WebRTC URL Low Quality";
    public static final String CHANNEL_WEBRTC_URL_PACKAGE = "webrtc-url-package";
    public static final String CHANNEL_WEBRTC_URL_PACKAGE_LABEL = "WebRTC URL Package Quality";
    // Triggers and Contacts
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_MOTION_START = "motion-start";
    public static final String CHANNEL_MOTION_START_LABEL = "Motion Start";
    public static final String CHANNEL_MOTION_UPDATE = "motion-update";
    public static final String CHANNEL_MOTION_UPDATE_LABEL = "Motion Update";
    public static final String CHANNEL_MOTION_CONTACT = "motion-contact";
    public static final String CHANNEL_MOTION_SNAPSHOT = "motion-snapshot";
    public static final String CHANNEL_MOTION_SNAPSHOT_LABEL = "Motion Snapshot";

    public static final String CHANNEL_SMART_DETECT_AUDIO = "smart-detect-audio";
    public static final String CHANNEL_SMART_DETECT_AUDIO_START = "smart-detect-audio-start";
    public static final String CHANNEL_SMART_DETECT_AUDIO_START_LABEL = "Smart Detect Audio Start";
    public static final String CHANNEL_SMART_DETECT_AUDIO_UPDATE = "smart-detect-audio-update";
    public static final String CHANNEL_SMART_DETECT_AUDIO_UPDATE_LABEL = "Smart Detect Audio Update";
    public static final String CHANNEL_SMART_DETECT_AUDIO_CONTACT = "smart-detect-audio-contact";
    public static final String CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT = "smart-detect-audio-snapshot";
    public static final String CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT_LABEL = "Smart Detect Audio Snapshot";

    public static final String CHANNEL_SMART_DETECT_ZONE = "smart-detect-zone";
    public static final String CHANNEL_SMART_DETECT_ZONE_START = "smart-detect-zone-start";
    public static final String CHANNEL_SMART_DETECT_ZONE_START_LABEL = "Smart Detect Zone Start";
    public static final String CHANNEL_SMART_DETECT_ZONE_UPDATE = "smart-detect-zone-update";
    public static final String CHANNEL_SMART_DETECT_ZONE_UPDATE_LABEL = "Smart Detect Zone Update";
    public static final String CHANNEL_SMART_DETECT_ZONE_CONTACT = "smart-detect-zone-contact";
    public static final String CHANNEL_SMART_DETECT_ZONE_SNAPSHOT = "smart-detect-zone-snapshot";
    public static final String CHANNEL_SMART_DETECT_ZONE_SNAPSHOT_LABEL = "Smart Detect Zone Snapshot";

    public static final String CHANNEL_SMART_DETECT_LINE = "smart-detect-line";
    public static final String CHANNEL_SMART_DETECT_LINE_START = "smart-detect-line-start";
    public static final String CHANNEL_SMART_DETECT_LINE_START_LABEL = "Smart Detect Line Start";
    public static final String CHANNEL_SMART_DETECT_LINE_UPDATE = "smart-detect-line-update";
    public static final String CHANNEL_SMART_DETECT_LINE_UPDATE_LABEL = "Smart Detect Line Update";
    public static final String CHANNEL_SMART_DETECT_LINE_CONTACT = "smart-detect-line-contact";
    public static final String CHANNEL_SMART_DETECT_LINE_SNAPSHOT = "smart-detect-line-snapshot";
    public static final String CHANNEL_SMART_DETECT_LINE_SNAPSHOT_LABEL = "Smart Detect Line Snapshot";

    public static final String CHANNEL_SMART_DETECT_LOITER = "smart-detect-loiter";
    public static final String CHANNEL_SMART_DETECT_LOITER_START = "smart-detect-loiter-start";
    public static final String CHANNEL_SMART_DETECT_LOITER_START_LABEL = "Smart Detect Loiter Start";
    public static final String CHANNEL_SMART_DETECT_LOITER_UPDATE = "smart-detect-loiter-update";
    public static final String CHANNEL_SMART_DETECT_LOITER_UPDATE_LABEL = "Smart Detect Loiter Update";
    public static final String CHANNEL_SMART_DETECT_LOITER_CONTACT = "smart-detect-loiter-contact";
    public static final String CHANNEL_SMART_DETECT_LOITER_SNAPSHOT = "smart-detect-loiter-snapshot";
    public static final String CHANNEL_SMART_DETECT_LOITER_SNAPSHOT_LABEL = "Smart Detect Loiter Snapshot";

    public static final String CHANNEL_RING = "ring";
    public static final String CHANNEL_RING_LABEL = "Ring";
    public static final String CHANNEL_RING_CONTACT = "ring-contact";
    public static final String CHANNEL_RING_SNAPSHOT = "ring-snapshot";
    public static final String CHANNEL_RING_SNAPSHOT_LABEL = "Ring Snapshot";

    // Light (floodlight)
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_IS_DARK = "is-dark";
    public static final String CHANNEL_PIR_MOTION = "pir-motion";
    public static final String CHANNEL_LAST_MOTION = "last-motion";
    public static final String CHANNEL_LIGHT_MODE = "light-mode";
    public static final String CHANNEL_ENABLE_AT = "enable-at";
    public static final String CHANNEL_INDICATOR_ENABLED = "indicator-enabled";
    public static final String CHANNEL_PIR_DURATION = "pir-duration";
    public static final String CHANNEL_PIR_SENSITIVITY = "pir-sensitivity";
    public static final String CHANNEL_LED_LEVEL = "led-level";

    // Sensor
    public static final String CHANNEL_BATTERY = "battery";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_ILLUMINANCE = "illuminance";
    public static final String CHANNEL_SENSOR_MOTION = "motion";
    public static final String CHANNEL_ALARM_CONTACT = "alarm-contact";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_WATER_LEAK_CONTACT = "water-leak-contact";
    public static final String CHANNEL_WATER_LEAK = "water-leak";
    public static final String CHANNEL_TAMPER_CONTACT = "tamper-contact";
    public static final String CHANNEL_TAMPER = "tamper";
    public static final String CHANNEL_OPENED = "opened";
    public static final String CHANNEL_CLOSED = "closed";
}
