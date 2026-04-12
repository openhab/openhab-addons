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
package org.openhab.binding.unifi.protect.internal.api.priv.dto.devices;

import java.time.Instant;
import java.util.List;

import org.openhab.binding.unifi.protect.internal.api.priv.dto.base.UniFiProtectAdoptableDevice;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.types.IRLEDMode;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.types.RecordingMode;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.types.SmartDetectObjectType;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.types.VideoMode;

import com.google.gson.annotations.SerializedName;

/**
 * Camera device model for UniFi Protect
 * This is the most comprehensive device model with all camera-specific fields
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Camera extends UniFiProtectAdoptableDevice {

    // Motion and detection
    public Instant lastMotion;
    public Instant lastRing;
    public Instant lastSmart;
    public Boolean isMotionDetected;
    public Boolean isSmartDetected;
    public Boolean isDark;

    // Audio
    public Integer micVolume;
    public Boolean isMicEnabled;

    // Recording
    public Boolean isRecording;
    public Boolean isLiveHeatmapEnabled;
    public Boolean videoReconfigurationInProgress;

    // Network
    public Double phyRate;
    public Boolean isProbingForWifi;
    public Boolean isPoorNetwork;
    public Boolean isWirelessUplinkEnabled;

    // Video settings
    public Boolean hdrMode;
    public VideoMode videoMode;
    public String hdrType;
    public Integer activePatrolSlot;

    // Doorbell specific
    public Integer chimeDuration;

    // Privacy
    public String lastPrivacyZonePositionId;

    // Power
    public Double voltage;
    public Boolean hasBattery;
    public BatteryStatus batteryStatus;

    // Platform
    public Boolean isThirdPartyCamera;
    public String platform;
    public Boolean hasSpeaker;
    public Boolean hasWifi;
    public List<String> audioCodecs;
    public List<String> videoCodecs;

    // Configuration objects
    public EventStats eventStats;
    public List<CameraChannel> channels;
    public ISPSettings ispSettings;
    public TalkbackSettings talkbackSettings;
    public OSDSettings osdSettings;
    public LEDSettings ledSettings;
    public SpeakerSettings speakerSettings;
    public RecordingSettings recordingSettings;
    public SmartDetectSettings smartDetectSettings;

    // Zones
    public List<MotionZone> motionZones;
    public List<PrivacyZone> privacyZones;
    public List<SmartDetectZone> smartDetectZones;
    public List<SmartDetectLine> smartDetectLines;

    // Stats and capabilities
    public CameraStats stats;
    public CameraFeatureFlags featureFlags;
    public LCDMessage lcdMessage;
    public List<CameraLens> lenses;
    public List<String> recordingSchedules;
    public String motionAlgorithm;
    public PirSettings pirSettings;

    public static class EventStats {
        public MotionStats motion;
        public SmartStats smart;
    }

    public static class MotionStats {
        public Integer today;
        public Integer average;
        public List<Integer> lastDays;
        public List<Integer> recentHours;
    }

    public static class SmartStats {
        public Integer today;
        public Integer average;
        public List<Integer> lastDays;
    }

    public static class CameraChannel {
        public Integer id;
        public String videoId;
        public String name;
        public Boolean enabled;
        public Boolean isRtspEnabled;
        public String rtspAlias;
        public Integer width;
        public Integer height;
        public Integer fps;
        public Integer bitrate;
        public Integer minBitrate;
        public Integer maxBitrate;
        public Integer minClientAdaptiveBitRate;
        public Integer minMotionAdaptiveBitRate;
        public List<Integer> fpsValues;
        public Integer idrInterval;
        public Boolean autoBitrate;
        public Boolean autoFps;
    }

    public static class ISPSettings {
        public String aeMode;
        public IRLEDMode irLedMode;
        public Integer irLedLevel;
        public Integer wdr;
        public Integer icrSensitivity;
        public Integer brightness;
        public Integer contrast;
        public Integer hue;
        public Integer saturation;
        public Integer sharpness;
        public Integer denoise;
        public Boolean isFlippedVertical;
        public Boolean isFlippedHorizontal;
        public Boolean isAutoRotateEnabled;
        public Boolean isLdcEnabled;
        public Boolean is3dnrEnabled;
        public Boolean isExternalIrEnabled;
        public Boolean isAggressiveAntiFlickerEnabled;
        public Boolean isPauseMotionEnabled;
        public Integer dZoomCenterX;
        public Integer dZoomCenterY;
        public Integer dZoomScale;
        public Integer dZoomStreamId;
        public String focusMode;
        public Integer focusPosition;
        public Integer touchFocusX;
        public Integer touchFocusY;
        public Integer zoomPosition;
        public String mountPosition;
        public Boolean isColorNightVisionEnabled;
        public String hdrMode;
        public Integer icrCustomValue;
        public String icrSwitchMode;
        public Integer spotlightDuration;
    }

    public static class TalkbackSettings {
        public String typeFmt;
        public String typeIn;
        public String bindAddr;
        public Integer bindPort;
        public Integer channels;
        public Integer samplingRate;
        public Integer bitsPerSample;
        public Integer quality;
    }

    public static class OSDSettings {
        public Boolean isNameEnabled;
        public Boolean isDateEnabled;
        public Boolean isLogoEnabled;
        public Boolean isDebugEnabled;
    }

    public static class LEDSettings {
        public Boolean isEnabled;
        public Integer blinkRate;
        public Boolean welcomeLed;
        public Boolean floodLed;
    }

    public static class SpeakerSettings {
        public Boolean isEnabled;
        public Boolean areSystemSoundsEnabled;
        public Integer volume;
        public Integer ringVolume;
        public String ringtoneId;
        public Integer repeatTimes;
        public Integer speakerVolume;
    }

    public static class RecordingSettings {
        public Integer prePaddingSecs;
        public Integer postPaddingSecs;
        public Integer minMotionEventTrigger;
        public Integer endMotionEventDelay;
        public Boolean suppressIlluminationSurge;
        public RecordingMode mode;
        public String geofencing;
        public String motionAlgorithm;
        public Boolean enableMotionDetection;
        public Boolean useNewMotionAlgorithm;
        public String inScheduleMode;
        public String outScheduleMode;
        public Long retentionDurationMs;
        public Integer smartDetectPrePaddingSecs;
        public Integer smartDetectPostPaddingSecs;
        public Boolean createAccessEvent;
    }

    public static class SmartDetectSettings {
        public List<SmartDetectObjectType> objectTypes;
        public List<String> audioTypes;
        public List<SmartDetectObjectType> autoTrackingObjectTypes;
    }

    public static class MotionZone {
        public Integer id;
        public String name;
        public String color;
        public List<List<Integer>> points;
        public Integer sensitivity;
    }

    public static class PrivacyZone {
        public Integer id;
        public String name;
        public String color;
        public List<List<Integer>> points;
    }

    public static class SmartDetectZone {
        public Integer id;
        public String name;
        public String color;
        public List<List<Integer>> points;
        public List<SmartDetectObjectType> objectTypes;
        public Integer sensitivity;
    }

    public static class SmartDetectLine {
        public Integer id;
        public String name;
        public String color;
        public List<List<Integer>> points;
        public List<SmartDetectObjectType> objectTypes;
        public String crossingType;
    }

    public static class CameraStats {
        public Long rxBytes;
        public Long txBytes;
        public WifiStats wifi;
        public VideoStats video;
        public StorageStats storage;
    }

    public static class WifiStats {
        public Integer channel;
        public Integer frequency;
        public String linkSpeedMbps;
        public Integer signalQuality;
        public Integer signalStrength;
    }

    public static class VideoStats {
        public Instant recordingStart;
        public Instant recordingEnd;

        @SerializedName("recordingStartLQ")
        public Instant recordingStartLq;

        @SerializedName("recordingEndLQ")
        public Instant recordingEndLq;

        public Instant timelapseStart;
        public Instant timelapseEnd;

        @SerializedName("timelapseStartLQ")
        public Instant timelapseStartLq;

        @SerializedName("timelapseEndLQ")
        public Instant timelapseEndLq;
    }

    public static class StorageStats {
        public Long used;
        public Double rate;

        public Double getRatePerSecond() {
            return rate != null ? rate * 1000 : null;
        }
    }

    public static class CameraFeatureFlags {
        public Boolean canAdjustIrLedLevel;
        public Boolean canMagicZoom;
        public Boolean canOpticalZoom;
        public Boolean canTouchFocus;
        public Boolean hasAccelerometer;
        public Boolean hasAec;
        public Boolean hasBattery;
        public Boolean hasBluetooth;
        public Boolean hasChime;
        public Boolean hasExternalIr;
        public Boolean hasHdr;
        public Boolean hasIcrSensitivity;
        public Boolean hasLdc;
        public Boolean hasLedIr;
        public Boolean hasLedStatus;
        public Boolean hasLineIn;
        public Boolean hasMic;
        public Boolean hasMotionZones;
        public Boolean hasNewMotionAlgorithm;
        public Boolean hasPackageCamera;
        public Boolean hasPrivacyMask;
        public Boolean hasRtc;
        public Boolean hasSdCard;
        public Boolean hasSmartDetect;
        public Boolean hasSpeaker;
        public Boolean hasWifi;
        public Boolean isDoorbell;
        public Boolean hasPtz;
        public Boolean isPtz;
        public Boolean hasAutoICROnly;
        public Boolean hasLensDistortionCorrection;
        public List<Integer> videoModeMaxFps;
        public List<VideoMode> videoModes;
        public List<SmartDetectObjectType> smartDetectTypes;
        public List<String> smartDetectAudioTypes;
        public List<String> motionAlgorithms;
    }

    public static class LCDMessage {
        public String type;
        public String text;
        public Instant resetAt;
    }

    public static class CameraLens {
        public Integer id;
        public VideoInfo video;
    }

    public static class VideoInfo {
        public Instant recordingStart;
        public Instant recordingEnd;
    }

    public static class BatteryStatus {
        public Integer percentage;
        public Boolean isCharging;
        public String sleepState;
    }

    public static class PirSettings {
        public Integer pirSensitivity;
        public Integer pirMotionClipLength;
        public Integer timelapseFrameInterval;
        public Integer timelapseTransferInterval;
    }

    public boolean isDoorbell() {
        return featureFlags != null && Boolean.TRUE.equals(featureFlags.isDoorbell);
    }

    public boolean hasSmartDetect() {
        return featureFlags != null && Boolean.TRUE.equals(featureFlags.hasSmartDetect);
    }

    @Override
    public String toString() {
        return "Camera{" +
        // From UniFiProtectModel
                "id='" + id + '\'' + ", model=" + model +
                // From UniFiProtectDevice
                ", name='" + name + '\'' + ", type='" + type + '\'' + ", mac='" + mac + '\'' + ", host='" + host + '\''
                + ", upSince=" + upSince + ", uptime=" + uptime + ", lastSeen=" + lastSeen + ", hardwareRevision='"
                + hardwareRevision + '\'' + ", firmwareVersion='" + firmwareVersion + '\'' + ", isUpdating="
                + isUpdating + ", isSshEnabled=" + isSshEnabled +
                // From UniFiProtectAdoptableDevice
                ", state=" + state + ", connectionHost='" + connectionHost + '\'' + ", connectedSince=" + connectedSince
                + ", latestFirmwareVersion='" + latestFirmwareVersion + '\'' + ", firmwareBuild='" + firmwareBuild
                + '\'' + ", isAdopting=" + isAdopting + ", isAdopted=" + isAdopted + ", isAdoptedByOther="
                + isAdoptedByOther + ", isProvisioned=" + isProvisioned + ", isRebooting=" + isRebooting + ", canAdopt="
                + canAdopt + ", isAttemptingToConnect=" + isAttemptingToConnect + ", isConnected=" + isConnected
                + ", marketName='" + marketName + '\'' + ", nvrMac='" + nvrMac + '\'' + ", guid=" + guid
                + ", isRestoring=" + isRestoring + ", lastDisconnect=" + lastDisconnect + ", anonymousDeviceId="
                + anonymousDeviceId + ", bridgeId='" + bridgeId + '\'' + ", isDownloadingFirmware="
                + isDownloadingFirmware + ", wiredConnectionState=" + wiredConnectionState + ", wifiConnectionState="
                + wifiConnectionState + ", bluetoothConnectionState=" + bluetoothConnectionState +
                // From Camera
                ", lastMotion=" + lastMotion + ", lastRing=" + lastRing + ", lastSmart=" + lastSmart
                + ", isMotionDetected=" + isMotionDetected + ", isSmartDetected=" + isSmartDetected + ", isDark="
                + isDark + ", micVolume=" + micVolume + ", isMicEnabled=" + isMicEnabled + ", isRecording="
                + isRecording + ", isLiveHeatmapEnabled=" + isLiveHeatmapEnabled + ", videoReconfigurationInProgress="
                + videoReconfigurationInProgress + ", phyRate=" + phyRate + ", isProbingForWifi=" + isProbingForWifi
                + ", isPoorNetwork=" + isPoorNetwork + ", isWirelessUplinkEnabled=" + isWirelessUplinkEnabled
                + ", hdrMode=" + hdrMode + ", videoMode=" + videoMode + ", chimeDuration=" + chimeDuration
                + ", lastPrivacyZonePositionId='" + lastPrivacyZonePositionId + '\'' + ", voltage=" + voltage
                + ", hasBattery=" + hasBattery + ", batteryStatus=" + batteryStatus + ", isThirdPartyCamera="
                + isThirdPartyCamera + ", platform='" + platform + '\'' + ", hasSpeaker=" + hasSpeaker + ", hasWifi="
                + hasWifi + ", audioCodecs=" + audioCodecs + ", videoCodecs=" + videoCodecs + ", eventStats="
                + eventStats + ", channels=" + channels + ", ispSettings=" + ispSettings + ", talkbackSettings="
                + talkbackSettings + ", osdSettings=" + osdSettings + ", ledSettings=" + ledSettings
                + ", speakerSettings=" + speakerSettings + ", recordingSettings=" + recordingSettings
                + ", smartDetectSettings=" + smartDetectSettings + ", motionZones=" + motionZones + ", privacyZones="
                + privacyZones + ", smartDetectZones=" + smartDetectZones + ", smartDetectLines=" + smartDetectLines
                + ", stats=" + stats + ", featureFlags=" + featureFlags + ", lcdMessage=" + lcdMessage + ", lenses="
                + lenses + ", recordingSchedules=" + recordingSchedules + ", motionAlgorithm='" + motionAlgorithm + '\''
                + ", pirSettings=" + pirSettings + '}';
    }
}
