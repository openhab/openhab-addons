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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectModel;

/**
 * NVR (Network Video Recorder) model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Nvr extends UniFiProtectModel {

    public String name;
    public Boolean canAutoUpdate;
    public Boolean isStatsGatheringEnabled;
    public String timezone;
    public String version;
    public String ucoreVersion;
    public String firmwareVersion;
    public String ulpVersion;
    public Ports ports;
    public Instant lastUpdateAt;
    public Boolean isStation;
    public Boolean enableAutomaticBackups;
    public Boolean enableStatsReporting;
    public String releaseChannel;
    public List<String> hosts;
    public Boolean enableBridgeAutoAdoption;
    public String hardwareId;
    public String hardwareRevision;
    public Integer hostType;
    public String hostShortname;
    public Boolean isWirelessUplinkEnabled;
    public String timeFormat; // "12h" or "24h"
    public String temperatureUnit; // "C" or "F"
    public Long recordingRetentionDurationMs;
    public Boolean enableCrashReporting;
    public Boolean disableAudio;
    public String analyticsData;
    public String anonymousDeviceId;
    public Integer cameraUtilization;
    public Boolean isRecycling;
    public Boolean disableAutoLink;
    public Boolean skipFirmwareUpdate;
    public NVRLocation locationSettings;
    public NVRFeatureFlags featureFlags;
    public SystemInfo systemInfo;
    public DoorbellSettings doorbellSettings;
    public StorageStats storageStats;
    public Boolean isAway;
    public Boolean isSetup;
    public String network;
    public Map<String, Object> maxCameraCapacity; // "4K", "2K", "HD" keys, values can be numbers or strings
    public String marketName;
    public Boolean streamSharingAvailable;
    public Boolean isDbAvailable;
    public Boolean isInsightsEnabled;
    public Boolean isRecordingDisabled;
    public Boolean isRecordingMotionOnly;
    public String uiVersion;
    public String ssoChannel;
    public Boolean isStacked;
    public Boolean isPrimary;
    public Instant lastDriveSlowEvent;
    public Boolean isUCoreSetup;
    public String corruptionState;
    public String countryCode;
    public Boolean hasGateway;
    public String publicIp;
    public String wanIp;
    public String hardDriveState;
    public Boolean isNetworkInstalled;
    public Boolean isProtectUpdatable;
    public Boolean isUcoreUpdatable;
    public Instant lastDeviceFwUpdatesCheckedAt;
    public NVRSmartDetection smartDetection;
    public Boolean isUcoreStacked;
    public GlobalCameraSettings globalCameraSettings;
    public String errorCode;
    public WifiSettings wifiSettings;
    public Object smartDetectAgreement; // Can be string or object
    public Object portStatus; // Port status can be array or object,
    public Map<String, Object> cameraCapacity; // Same format as maxCameraCapacity

    /**
     * Port configuration for NVR services
     */
    public static class Ports {
        public Integer http;
        public Integer https;
        public Integer rtsp;
        public Integer rtsps;
        public Integer tcpStreams;
        public Integer tcpBridge;
    }

    /**
     * NVR location settings for geofencing
     */
    public static class NVRLocation {
        public Boolean isAway;
        public Double latitude;
        public Double longitude;
        public Boolean isGeofencingEnabled;
        public Integer radius;
    }

    /**
     * NVR feature flags
     */
    public static class NVRFeatureFlags {
        public Boolean beta;
        public Boolean dev;
        public Boolean notificationsV2;
    }

    /**
     * System information
     */
    public static class SystemInfo {
        public CpuInfo cpu;
        public MemoryInfo memory;
        public StorageInfo storage;
        public Object ustorage; // Can be string or object
    }

    /**
     * CPU information
     */
    public static class CpuInfo {
        public Double averageLoad;
        public Double temperature;
    }

    /**
     * Memory information
     */
    public static class MemoryInfo {
        public Long available;
        public Long free;
        public Long total;
    }

    /**
     * Storage information
     */
    public static class StorageInfo {
        public Long available;
        public Boolean isRecycling;
        public Long size;
        public String type;
        public Long used;
        public String capability;
        public List<StorageDevice> devices;
    }

    /**
     * Doorbell settings
     */
    public static class DoorbellSettings {
        public String defaultMessageText;
        public String defaultMessageResetAt;
        public Object allMessages; // Can be boolean or array
        public List<Object> customMessages; // Can be strings or DoorbellMessage objects
    }

    /**
     * Doorbell message
     */
    public static class DoorbellMessage {
        public String type;
        public String text;
    }

    /**
     * Storage statistics
     */
    public static class StorageStats {
        public Double utilization;
        public Double recordingRate;
        public Long capacity;
        public Long remainingCapacity;
        public Long averageCapacity;
        public RecordingSpace recordingSpace;
    }

    /**
     * Recording space information
     */
    public static class RecordingSpace {
        public Long total;
        public Long used;
        public Long available;
    }

    /**
     * Storage device
     */
    public static class StorageDevice {
        public String model;
        public Long size;
        public String healthy;
    }

    /**
     * Smart detection settings
     */
    public static class NVRSmartDetection {
        public Boolean enable;
        public Boolean faceRecognition;
        public Boolean licensePlateRecognition;
        public String precision;
    }

    /**
     * Global camera settings
     */
    public static class GlobalCameraSettings {
        public String recordingMode;
        public Boolean enablePirTimelapse;
    }

    /**
     * WiFi settings
     */
    public static class WifiSettings {
        public Boolean enabled;
        public String ssid;
    }
}
