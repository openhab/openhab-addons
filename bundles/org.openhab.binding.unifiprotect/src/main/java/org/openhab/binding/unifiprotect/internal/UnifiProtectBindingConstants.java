/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
    public static final ThingTypeUID THING_TYPE_DOORLOCK = new ThingTypeUID(BINDING_ID, "doorlock");
    public static final ThingTypeUID THING_TYPE_CHIME = new ThingTypeUID(BINDING_ID, "chime");

    // List of all Configuration ids
    public static final String DEVICE_ID = "deviceId";
    public static final String SNAPSHOT_SEQUENCE = "sequence";

    // List of all Property ids
    public static final String PROPERTY_WEBRTC_URL = "webrtc-url"; // also used as base url for all webrtc urls
    public static final String PROPERTY_SNAPSHOT_URL = "snapshot-url";

    // Device Properties (Generic)
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";

    // Camera Properties
    public static final String PROPERTY_PLATFORM = "platform";
    public static final String PROPERTY_HAS_SPEAKER = "hasSpeaker";
    public static final String PROPERTY_HAS_WIFI = "hasWifi";
    public static final String PROPERTY_HAS_BATTERY = "hasBattery";
    public static final String PROPERTY_IS_THIRD_PARTY_CAMERA = "isThirdPartyCamera";

    // NVR Properties
    public static final String PROPERTY_APPLICATION_VERSION = "applicationVersion";
    public static final String PROPERTY_PROTECT_VERSION = "protectVersion";
    public static final String PROPERTY_UCORE_VERSION = "ucoreVersion";
    public static final String PROPERTY_UI_VERSION = "uiVersion";
    public static final String PROPERTY_HARDWARE_PLATFORM = "hardwarePlatform";
    public static final String PROPERTY_MARKET_NAME = "marketName";
    public static final String PROPERTY_IS_HARDWARE = "isHardware";
    public static final String PROPERTY_CAMERA_CAPACITY_MAX = "cameraCapacityMax";
    public static final String PROPERTY_HOSTNAME = "hostname";
    public static final String PROPERTY_HOST = "host";
    public static final String PROPERTY_PUBLIC_IP = "publicIp";
    public static final String PROPERTY_WAN_IP = "wanIp";

    // Sensor Properties
    public static final String PROPERTY_MOUNT_TYPE = "mountType";

    // List of all Channel ids

    // Shared
    public static final String CHANNEL_STATUS = "status";

    // Camera
    public static final String CHANNEL_SNAPSHOT = "snapshot";
    public static final String CHANNEL_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.snapshot.label";
    public static final String CHANNEL_SNAPSHOT_URL = "snapshot-url";
    public static final String CHANNEL_SNAPSHOT_URL_LABEL = "@text/channel-type.unifiprotect.snapshot-url.label";
    public static final String CHANNEL_MIC_VOLUME = "mic-volume";
    public static final String CHANNEL_MIC_ENABLED = "mic-enabled";
    public static final String CHANNEL_RECORDING_MODE = "recording-mode";
    public static final String CHANNEL_IR_MODE = "ir-mode";
    public static final String CHANNEL_MOTION_DETECTION_ENABLED = "motion-detection-enabled";
    public static final String CHANNEL_USE_GLOBAL_SETTINGS = "use-global-settings";
    public static final String CHANNEL_VIDEO_MODE = "video-mode";
    public static final String CHANNEL_HDR_TYPE = "hdr-type";
    public static final String CHANNEL_OSD_NAME = "osd-name";
    public static final String CHANNEL_OSD_DATE = "osd-date";
    public static final String CHANNEL_OSD_LOGO = "osd-logo";
    public static final String CHANNEL_LED_ENABLED = "led-enabled";
    public static final String CHANNEL_ACTIVE_PATROL_SLOT = "active-patrol-slot";
    public static final String CHANNEL_RTSP_URL_HIGH = "rtsp-url-high";
    public static final String CHANNEL_RTSP_URL_HIGH_LABEL = "@text/channel-type.unifiprotect.rtsp-url-high.label";
    public static final String CHANNEL_RTSP_URL_MEDIUM = "rtsp-url-medium";
    public static final String CHANNEL_RTSP_URL_MEDIUM_LABEL = "@text/channel-type.unifiprotect.rtsp-url-medium.label";
    public static final String CHANNEL_RTSP_URL_LOW = "rtsp-url-low";
    public static final String CHANNEL_RTSP_URL_LOW_LABEL = "@text/channel-type.unifiprotect.rtsp-url-low.label";
    public static final String CHANNEL_RTSP_URL_PACKAGE = "rtsp-url-package";
    public static final String CHANNEL_RTSP_URL_PACKAGE_LABEL = "@text/channel-type.unifiprotect.rtsp-url-package.label";
    public static final String CHANNEL_RTSP_URL = "rtsp-url";
    public static final String CHANNEL_WEBRTC_URL = "webrtc-url";
    public static final String CHANNEL_WEBRTC_URL_HIGH = "webrtc-url-high";
    public static final String CHANNEL_WEBRTC_URL_HIGH_LABEL = "@text/channel-type.unifiprotect.webrtc-url-high.label";
    public static final String CHANNEL_WEBRTC_URL_MEDIUM = "webrtc-url-medium";
    public static final String CHANNEL_WEBRTC_URL_MEDIUM_LABEL = "@text/channel-type.unifiprotect.webrtc-url-medium.label";
    public static final String CHANNEL_WEBRTC_URL_LOW = "webrtc-url-low";
    public static final String CHANNEL_WEBRTC_URL_LOW_LABEL = "@text/channel-type.unifiprotect.webrtc-url-low.label";
    public static final String CHANNEL_WEBRTC_URL_PACKAGE = "webrtc-url-package";
    public static final String CHANNEL_WEBRTC_URL_PACKAGE_LABEL = "@text/channel-type.unifiprotect.webrtc-url-package.label";
    // Triggers and Contacts
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_MOTION_START = "motion-start";
    public static final String CHANNEL_MOTION_START_LABEL = "@text/channel-type.unifiprotect.motion-start.label";
    public static final String CHANNEL_MOTION_UPDATE = "motion-update";
    public static final String CHANNEL_MOTION_UPDATE_LABEL = "@text/channel-type.unifiprotect.motion-update.label";
    public static final String CHANNEL_MOTION_CONTACT = "motion-contact";
    public static final String CHANNEL_MOTION_SNAPSHOT = "motion-snapshot";
    public static final String CHANNEL_MOTION_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.motion-snapshot.label";

    public static final String CHANNEL_SMART_DETECT_AUDIO = "smart-detect-audio";
    public static final String CHANNEL_SMART_DETECT_AUDIO_START = "smart-detect-audio-start";
    public static final String CHANNEL_SMART_DETECT_AUDIO_START_LABEL = "@text/channel-type.unifiprotect.smart-detect-audio-start.label";
    public static final String CHANNEL_SMART_DETECT_AUDIO_UPDATE = "smart-detect-audio-update";
    public static final String CHANNEL_SMART_DETECT_AUDIO_UPDATE_LABEL = "@text/channel-type.unifiprotect.smart-detect-audio-update.label";
    public static final String CHANNEL_SMART_DETECT_AUDIO_CONTACT = "smart-detect-audio-contact";
    public static final String CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT = "smart-detect-audio-snapshot";
    public static final String CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.smart-detect-audio-snapshot.label";

    // Smart Detection Control (Private API)
    public static final String CHANNEL_SMART_DETECT_PERSON_ENABLED = "smart-detect-person-enabled";
    public static final String CHANNEL_SMART_DETECT_VEHICLE_ENABLED = "smart-detect-vehicle-enabled";
    public static final String CHANNEL_SMART_DETECT_FACE_ENABLED = "smart-detect-face-enabled";
    public static final String CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED = "smart-detect-license-plate-enabled";
    public static final String CHANNEL_SMART_DETECT_PACKAGE_ENABLED = "smart-detect-package-enabled";
    public static final String CHANNEL_SMART_DETECT_ANIMAL_ENABLED = "smart-detect-animal-enabled";

    public static final String CHANNEL_SMART_DETECT_ZONE = "smart-detect-zone";
    public static final String CHANNEL_SMART_DETECT_ZONE_START = "smart-detect-zone-start";
    public static final String CHANNEL_SMART_DETECT_ZONE_START_LABEL = "@text/channel-type.unifiprotect.smart-detect-zone-start.label";
    public static final String CHANNEL_SMART_DETECT_ZONE_UPDATE = "smart-detect-zone-update";
    public static final String CHANNEL_SMART_DETECT_ZONE_UPDATE_LABEL = "@text/channel-type.unifiprotect.smart-detect-zone-update.label";
    public static final String CHANNEL_SMART_DETECT_ZONE_CONTACT = "smart-detect-zone-contact";
    public static final String CHANNEL_SMART_DETECT_ZONE_SNAPSHOT = "smart-detect-zone-snapshot";
    public static final String CHANNEL_SMART_DETECT_ZONE_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.smart-detect-zone-snapshot.label";

    public static final String CHANNEL_SMART_DETECT_LINE = "smart-detect-line";
    public static final String CHANNEL_SMART_DETECT_LINE_START = "smart-detect-line-start";
    public static final String CHANNEL_SMART_DETECT_LINE_START_LABEL = "@text/channel-type.unifiprotect.smart-detect-line-start.label";
    public static final String CHANNEL_SMART_DETECT_LINE_UPDATE = "smart-detect-line-update";
    public static final String CHANNEL_SMART_DETECT_LINE_UPDATE_LABEL = "@text/channel-type.unifiprotect.smart-detect-line-update.label";
    public static final String CHANNEL_SMART_DETECT_LINE_CONTACT = "smart-detect-line-contact";
    public static final String CHANNEL_SMART_DETECT_LINE_SNAPSHOT = "smart-detect-line-snapshot";
    public static final String CHANNEL_SMART_DETECT_LINE_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.smart-detect-line-snapshot.label";

    public static final String CHANNEL_SMART_DETECT_LOITER = "smart-detect-loiter";
    public static final String CHANNEL_SMART_DETECT_LOITER_START = "smart-detect-loiter-start";
    public static final String CHANNEL_SMART_DETECT_LOITER_START_LABEL = "@text/channel-type.unifiprotect.smart-detect-loiter-start.label";
    public static final String CHANNEL_SMART_DETECT_LOITER_UPDATE = "smart-detect-loiter-update";
    public static final String CHANNEL_SMART_DETECT_LOITER_UPDATE_LABEL = "@text/channel-type.unifiprotect.smart-detect-loiter-update.label";
    public static final String CHANNEL_SMART_DETECT_LOITER_CONTACT = "smart-detect-loiter-contact";
    public static final String CHANNEL_SMART_DETECT_LOITER_SNAPSHOT = "smart-detect-loiter-snapshot";
    public static final String CHANNEL_SMART_DETECT_LOITER_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.smart-detect-loiter-snapshot.label";

    public static final String CHANNEL_RING = "ring";
    public static final String CHANNEL_RING_LABEL = "@text/channel-type.unifiprotect.ring.label";
    public static final String CHANNEL_RING_CONTACT = "ring-contact";
    public static final String CHANNEL_RING_SNAPSHOT = "ring-snapshot";
    public static final String CHANNEL_RING_SNAPSHOT_LABEL = "@text/channel-type.unifiprotect.ring-snapshot.label";

    public static final String CHANNEL_DOORBELL_DEFAULT_MESSAGE = "doorbell-default-message";
    public static final String CHANNEL_DOORBELL_DEFAULT_MESSAGE_LABEL = "@text/channel-type.unifiprotect.doorbell-default-message.label";
    public static final String CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT = "doorbell-default-message-reset-timeout";
    public static final String CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT_LABEL = "@text/channel-type.unifiprotect.doorbell-default-message-reset-timeout.label";

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

    // Doorlock (Private API)
    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_LOCK_STATUS = "lock-status";
    public static final String CHANNEL_CALIBRATE = "calibrate";
    public static final String CHANNEL_AUTO_CLOSE_TIME = "auto-close-time";

    // Chime (Private API)
    public static final String CHANNEL_PLAY_CHIME = "play-chime";
    public static final String CHANNEL_PLAY_BUZZER = "play-buzzer";
    public static final String CHANNEL_CHIME_VOLUME = "volume";
    public static final String CHANNEL_CHIME_REPEAT_TIMES = "repeat-times";

    // Advanced PTZ (Private API)
    public static final String CHANNEL_PTZ_RELATIVE_PAN = "ptz-relative-pan";
    public static final String CHANNEL_PTZ_RELATIVE_TILT = "ptz-relative-tilt";
    public static final String CHANNEL_PTZ_RELATIVE_ZOOM = "ptz-relative-zoom";
    public static final String CHANNEL_PTZ_CENTER = "ptz-center";
    public static final String CHANNEL_PTZ_CREATE_PRESET = "ptz-create-preset";
    public static final String CHANNEL_PTZ_DELETE_PRESET = "ptz-delete-preset";
    public static final String CHANNEL_PTZ_SET_HOME = "ptz-set-home";

    // Advanced Camera Settings (Private API)
    public static final String CHANNEL_CAMERA_SPEAKER_VOLUME = "camera-speaker-volume";
    public static final String CHANNEL_CAMERA_ZOOM_LEVEL = "camera-zoom-level";
    public static final String CHANNEL_CAMERA_WDR_LEVEL = "camera-wdr-level";
    public static final String CHANNEL_DOORBELL_RING_VOLUME = "doorbell-ring-volume";
    public static final String CHANNEL_DOORBELL_CHIME_DURATION = "doorbell-chime-duration";

    // Device Management (Private API)
    public static final String CHANNEL_DEVICE_REBOOT = "device-reboot";

    // Advanced Sensor (Private API)
    public static final String CHANNEL_SENSOR_CLEAR_TAMPER = "sensor-clear-tamper";

    // Advanced Light Settings (Private API)
    public static final String CHANNEL_LIGHT_MODE_ADVANCED = "light-mode-advanced";
    public static final String CHANNEL_LIGHT_ENABLE_SCHEDULE = "light-enable-schedule";

    // Camera Event Image Channels
    public static final String CHANNEL_MOTION_THUMBNAIL = "motion-thumbnail";
    public static final String CHANNEL_MOTION_HEATMAP = "motion-heatmap";
    public static final String CHANNEL_MOTION_SCORE = "motion-score";
    public static final String CHANNEL_SMART_DETECT_THUMBNAIL = "smart-detect-thumbnail";
    public static final String CHANNEL_SMART_DETECT_SCORE = "smart-detect-score";
    public static final String CHANNEL_SMART_DETECT_TYPES = "smart-detect-types";
    public static final String CHANNEL_SMART_DETECT_LAST_TYPE = "smart-detect-last-type";
    public static final String CHANNEL_SMART_DETECT_TIMESTAMP = "smart-detect-timestamp";

    // Camera Status Channels (Read-Only)
    public static final String CHANNEL_IS_MOTION_DETECTED = "is-motion-detected";
    public static final String CHANNEL_IS_SMART_DETECTED = "is-smart-detected";
    public static final String CHANNEL_IS_RECORDING = "is-recording";
    public static final String CHANNEL_IS_MIC_ACTIVE = "is-mic-active";
    public static final String CHANNEL_HIGH_FPS_ENABLED = "high-fps-enabled";
    public static final String CHANNEL_HDR_ENABLED = "hdr-enabled";

    // Camera Timestamp Channels
    public static final String CHANNEL_UPTIME_STARTED = "uptime-started";
    public static final String CHANNEL_CONNECTED_SINCE = "connected-since";
    public static final String CHANNEL_LAST_SEEN = "last-seen";

    // Doorbell Status Channels
    public static final String CHANNEL_RING_THUMBNAIL = "ring-thumbnail";
    public static final String CHANNEL_IS_RINGING = "is-ringing";
    public static final String CHANNEL_LAST_RING = "last-ring";
    public static final String CHANNEL_LCD_MESSAGE = "lcd-message";

    // Additional Camera Status Channels (Private API)
    // Note: CHANNEL_IS_DARK already defined in Light section (line 154)
    public static final String CHANNEL_LAST_SMART = "last-smart";
    public static final String CHANNEL_IS_LIVE_HEATMAP_ENABLED = "is-live-heatmap-enabled";
    public static final String CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS = "video-reconfiguration-in-progress";

    // Network Status
    public static final String CHANNEL_PHY_RATE = "phy-rate";
    public static final String CHANNEL_IS_PROBING_FOR_WIFI = "is-probing-for-wifi";
    public static final String CHANNEL_IS_POOR_NETWORK = "is-poor-network";
    public static final String CHANNEL_IS_WIRELESS_UPLINK_ENABLED = "is-wireless-uplink-enabled";

    // Power & Battery
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_BATTERY_PERCENTAGE = "battery-percentage";
    public static final String CHANNEL_BATTERY_IS_CHARGING = "battery-is-charging";
    public static final String CHANNEL_BATTERY_SLEEP_STATE = "battery-sleep-state";

    // WiFi Stats
    public static final String CHANNEL_WIFI_CHANNEL = "wifi-channel";
    public static final String CHANNEL_WIFI_FREQUENCY = "wifi-frequency";
    public static final String CHANNEL_WIFI_SIGNAL_QUALITY = "wifi-signal-quality";
    public static final String CHANNEL_WIFI_SIGNAL_STRENGTH = "wifi-signal-strength";

    // Storage Stats
    public static final String CHANNEL_STORAGE_USED = "storage-used";
    public static final String CHANNEL_STORAGE_RATE = "storage-rate";

    // NVR Status Channels
    public static final String CHANNEL_NVR_UPTIME = "nvr-uptime";
    public static final String CHANNEL_NVR_LAST_SEEN = "nvr-last-seen";
    public static final String CHANNEL_NVR_LAST_UPDATED = "nvr-last-updated";

    // NVR System Resource Channels
    public static final String CHANNEL_CPU_LOAD = "cpu-load";
    public static final String CHANNEL_CPU_TEMPERATURE = "cpu-temperature";
    public static final String CHANNEL_MEMORY_AVAILABLE = "memory-available";
    public static final String CHANNEL_MEMORY_FREE = "memory-free";

    // NVR Storage Channels
    public static final String CHANNEL_STORAGE_TOTAL = "storage-total";
    // Note: CHANNEL_STORAGE_USED already defined in camera Storage Stats section (line 280)
    public static final String CHANNEL_STORAGE_AVAILABLE = "storage-available";
    public static final String CHANNEL_STORAGE_UTILIZATION = "storage-utilization";
    public static final String CHANNEL_STORAGE_IS_RECYCLING = "storage-is-recycling";
    public static final String CHANNEL_RECORDING_RETENTION = "recording-retention";

    // NVR Network & Info Channels
    public static final String CHANNEL_CLOUD_CONNECTED = "cloud-connected";
    public static final String CHANNEL_ENABLE_AUTOMATIC_BACKUPS = "enable-automatic-backups";
    public static final String CHANNEL_ALERTS_ENABLED = "alerts-enabled";

    // NVR Private API Channels
    public static final String CHANNEL_NVR_STORAGE_DEVICE_HEALTHY = "storage-device-healthy";
    public static final String CHANNEL_NVR_CAMERA_UTILIZATION = "camera-utilization";
    public static final String CHANNEL_NVR_RECORDING_MODE = "recording-mode";
    public static final String CHANNEL_NVR_RECORDING_DISABLED = "recording-disabled";
    public static final String CHANNEL_NVR_RECORDING_MOTION_ONLY = "recording-motion-only";
    public static final String CHANNEL_NVR_IS_AWAY = "is-away";
    public static final String CHANNEL_NVR_GEOFENCING_ENABLED = "geofencing-enabled";
    public static final String CHANNEL_NVR_SMART_DETECTION_AVAILABLE = "smart-detection-available";
    public static final String CHANNEL_NVR_INSIGHTS_ENABLED = "insights-enabled";
    public static final String CHANNEL_NVR_VAULT_REGISTERED = "vault-registered";
    public static final String CHANNEL_NVR_CAN_AUTO_UPDATE = "can-auto-update";
    public static final String CHANNEL_NVR_LAST_UPDATE_AT = "last-update-at";
    public static final String CHANNEL_NVR_PROTECT_UPDATABLE = "protect-updatable";

    // Device Info Channels (Generic)
    public static final String CHANNEL_DEVICE_STATE = "device-state";
    public static final String CHANNEL_DEVICE_UPTIME = "device-uptime";
}
