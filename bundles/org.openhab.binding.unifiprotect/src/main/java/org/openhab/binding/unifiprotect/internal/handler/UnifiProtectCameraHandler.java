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
package org.openhab.binding.unifiprotect.internal.handler;

import static org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants.*;
import static org.openhab.core.library.CoreItemFactory.*;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.ApiValueEnum;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.ChannelQuality;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.RtspsStreams;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.TalkbackSession;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.CameraSmartDetectAudioEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.CameraSmartDetectLineEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.CameraSmartDetectLoiterEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.CameraSmartDetectZoneEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.RingEvent;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectCameraConfiguration;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectSnapshotConfig;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectSnapshotConfig.Sequence;
import org.openhab.binding.unifiprotect.internal.media.UnifiMediaService;
import org.openhab.binding.unifiprotect.internal.util.TranslationService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

import com.google.gson.JsonNull;

/**
 * Child handler for a UniFi Protect Camera.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectCameraHandler extends UnifiProtectAbstractDeviceHandler<Camera> {

    private final UnifiMediaService media;
    private boolean enableWebRTC = true;
    private final String baseSourceId;
    private final TranslationService translationService;

    public UnifiProtectCameraHandler(Thing thing, UnifiMediaService media, TranslationService translationService) {
        super(thing);
        this.media = media;
        this.baseSourceId = thing.getUID().getAsString();
        this.translationService = translationService;
    }

    @Override
    public void initialize() {
        enableWebRTC = getConfigAs(UnifiProtectCameraConfiguration.class).enableWebRTC;
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        media.unregisterHandler(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            switch (id) {
                case CHANNEL_SNAPSHOT:
                    updateSnapshot(id);
                    return;
                default:
                    refreshState(id);
                    return;
            }
        }

        UniFiProtectHybridClient api = getApiClient();
        if (api == null) {
            return;
        }

        switch (id) {
            case CHANNEL_MIC_VOLUME:
                if (command instanceof DecimalType decimal) {
                    int volume = Math.max(0, Math.min(100, decimal.intValue()));
                    logOnFailure(api.getPrivateClient().setCameraMicVolume(deviceId, volume), "set mic volume");
                }
                break;
            case CHANNEL_HDR_TYPE:
                logOnFailure(api.getPrivateClient().updateCamera(deviceId, Map.of("hdrType", command.toString())),
                        "set HDR type");
                break;
            case CHANNEL_VIDEO_MODE:
                logOnFailure(api.getPrivateClient().setCameraVideoMode(deviceId, command.toString()), "set video mode");
                break;
            case CHANNEL_OSD_NAME:
                logOnFailure(
                        api.getPrivateClient().updateCamera(deviceId,
                                Map.of("osdSettings", Map.of("isNameEnabled", OnOffType.ON.equals(command)))),
                        "set OSD name");
                break;
            case CHANNEL_OSD_DATE:
                logOnFailure(
                        api.getPrivateClient().updateCamera(deviceId,
                                Map.of("osdSettings", Map.of("isDateEnabled", OnOffType.ON.equals(command)))),
                        "set OSD date");
                break;
            case CHANNEL_OSD_LOGO:
                logOnFailure(
                        api.getPrivateClient().updateCamera(deviceId,
                                Map.of("osdSettings", Map.of("isLogoEnabled", OnOffType.ON.equals(command)))),
                        "set OSD logo");
                break;
            case CHANNEL_DOORBELL_DEFAULT_MESSAGE:
                logOnFailure(api.getPrivateClient().updateCamera(deviceId,
                        Map.of("lcdMessage", Map.of("text", command.toString()))), "set doorbell message");
                break;
            case CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT: {
                Long value = timeToMilliseconds(command);
                if (value != null && value <= 0) {
                    value = null;
                }
                HashMap<String, Object> resetAtMap = new HashMap<>();
                resetAtMap.put("resetAt", value != null ? value : JsonNull.INSTANCE);
                logOnFailure(api.getPrivateClient().updateCamera(deviceId, Map.of("lcdMessage", resetAtMap)),
                        "set doorbell message reset timeout");
                break;
            }
            case CHANNEL_LED_ENABLED:
                logOnFailure(api.getPrivateClient().setCameraStatusLight(deviceId, OnOffType.ON.equals(command)),
                        "set LED enabled");
                break;
            case CHANNEL_ACTIVE_PATROL_SLOT:
                if (command instanceof DecimalType decimal) {
                    int slot = decimal.intValue();
                    try {
                        if (slot <= 0) {
                            api.getPublicClient().ptzPatrolStop(deviceId);
                            updateIntegerChannel(CHANNEL_ACTIVE_PATROL_SLOT, 0);
                        } else {
                            api.getPublicClient().ptzPatrolStart(deviceId, String.valueOf(slot));
                            updateIntegerChannel(CHANNEL_ACTIVE_PATROL_SLOT, slot);
                        }
                    } catch (IOException e) {
                        logger.debug("Failed to set patrol slot", e);
                    }
                }
                break;
            case CHANNEL_PTZ_RELATIVE_PAN:
                if (command instanceof DecimalType pan) {
                    logOnFailure(api.getPrivateClient().ptzRelativeMove(deviceId, pan.floatValue(), 0, 10, 10, 0),
                            "PTZ pan");
                }
                break;
            case CHANNEL_PTZ_RELATIVE_TILT:
                if (command instanceof DecimalType tilt) {
                    logOnFailure(api.getPrivateClient().ptzRelativeMove(deviceId, 0, tilt.floatValue(), 10, 10, 0),
                            "PTZ tilt");
                }
                break;
            case CHANNEL_PTZ_RELATIVE_ZOOM:
                if (command instanceof DecimalType zoom) {
                    logOnFailure(api.getPrivateClient().ptzZoom(deviceId, zoom.floatValue(), 10), "PTZ zoom");
                }
                break;
            case CHANNEL_PTZ_CENTER:
                if (command instanceof StringType coords) {
                    try {
                        String[] parts = coords.toString().split(",");
                        if (parts.length == 3) {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            int z = Integer.parseInt(parts[2]);
                            logOnFailure(api.getPrivateClient().ptzCenter(deviceId, x, y, z), "PTZ center");
                        }
                    } catch (NumberFormatException e) {
                        logger.debug("Invalid PTZ center coordinates: {}", coords);
                    }
                }
                break;
            case CHANNEL_PTZ_SET_HOME:
                if (command == OnOffType.ON) {
                    logOnFailure(api.getPrivateClient().ptzSetHome(deviceId), "PTZ set home");
                }
                break;
            case CHANNEL_PTZ_CREATE_PRESET:
                if (command instanceof StringType presetCmd) {
                    try {
                        String[] parts = presetCmd.toString().split(",", 2);
                        if (parts.length == 2) {
                            int slot = Integer.parseInt(parts[0].trim());
                            String name = parts[1].trim();
                            logOnFailure(api.getPrivateClient().ptzCreatePreset(deviceId, slot, name),
                                    "create PTZ preset");
                        } else {
                            logger.debug("Invalid PTZ preset format. Expected 'slot,name', got: {}", presetCmd);
                        }
                    } catch (NumberFormatException e) {
                        logger.debug("Invalid PTZ preset slot number", e);
                    }
                }
                break;
            case CHANNEL_PTZ_DELETE_PRESET:
                if (command instanceof DecimalType slotCmd) {
                    logOnFailure(api.getPrivateClient().ptzDeletePreset(deviceId, slotCmd.intValue()),
                            "delete PTZ preset");
                }
                break;
            case CHANNEL_RECORDING_MODE:
                if (command instanceof StringType mode) {
                    logOnFailure(api.getPrivateClient().setCameraRecordingMode(deviceId, mode.toString()),
                            "set recording mode");
                }
                break;
            case CHANNEL_SMART_DETECT_PERSON_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setPersonDetection(deviceId, enabled == OnOffType.ON),
                            "set person detection");
                }
                break;
            case CHANNEL_SMART_DETECT_VEHICLE_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setVehicleDetection(deviceId, enabled == OnOffType.ON),
                            "set vehicle detection");
                }
                break;
            case CHANNEL_SMART_DETECT_FACE_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setFaceDetection(deviceId, enabled == OnOffType.ON),
                            "set face detection");
                }
                break;
            case CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setLicensePlateDetection(deviceId, enabled == OnOffType.ON),
                            "set license plate detection");
                }
                break;
            case CHANNEL_SMART_DETECT_PACKAGE_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setPackageDetection(deviceId, enabled == OnOffType.ON),
                            "set package detection");
                }
                break;
            case CHANNEL_SMART_DETECT_ANIMAL_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setAnimalDetection(deviceId, enabled == OnOffType.ON),
                            "set animal detection");
                }
                break;
            case CHANNEL_DEVICE_REBOOT:
                if (command == OnOffType.ON) {
                    logOnFailure(api.getPrivateClient().rebootDevice("camera", deviceId), "reboot camera");
                    updateState(channelUID, OnOffType.OFF);
                }
                break;
            case CHANNEL_MIC_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setCameraMicEnabled(deviceId, enabled == OnOffType.ON),
                            "set microphone enabled");
                }
                break;
            case CHANNEL_IR_MODE:
                if (command instanceof StringType mode) {
                    logOnFailure(api.getPrivateClient().setCameraIRMode(deviceId, mode.toString()), "set IR mode");
                }
                break;
            case CHANNEL_MOTION_DETECTION_ENABLED:
                if (command instanceof OnOffType enabled) {
                    logOnFailure(api.getPrivateClient().setCameraMotionDetection(deviceId, enabled == OnOffType.ON),
                            "set motion detection");
                }
                break;
            case CHANNEL_USE_GLOBAL_SETTINGS:
                if (command instanceof OnOffType useGlobal) {
                    logOnFailure(api.getPrivateClient().setCameraUseGlobal(deviceId, useGlobal == OnOffType.ON),
                            "set use global settings");
                }
                break;
            case CHANNEL_CAMERA_SPEAKER_VOLUME:
                if (command instanceof DecimalType volume) {
                    logOnFailure(api.getPrivateClient().setCameraSpeakerVolume(deviceId,
                            Math.max(0, Math.min(100, volume.intValue()))), "set speaker volume");
                }
                break;
            case CHANNEL_CAMERA_ZOOM_LEVEL:
                if (command instanceof DecimalType zoom) {
                    logOnFailure(
                            api.getPrivateClient().setCameraZoom(deviceId, Math.max(0, Math.min(100, zoom.intValue()))),
                            "set zoom level");
                }
                break;
            case CHANNEL_CAMERA_WDR_LEVEL:
                if (command instanceof DecimalType wdr) {
                    logOnFailure(
                            api.getPrivateClient().setCameraWDR(deviceId, Math.max(0, Math.min(3, wdr.intValue()))),
                            "set WDR level");
                }
                break;
            case CHANNEL_DOORBELL_RING_VOLUME:
                if (command instanceof DecimalType volume) {
                    logOnFailure(api.getPrivateClient().setDoorbellRingVolume(deviceId,
                            Math.max(0, Math.min(100, volume.intValue()))), "set doorbell ring volume");
                }
                break;
            case CHANNEL_DOORBELL_CHIME_DURATION: {
                Long duration = timeToMilliseconds(command);
                if (duration != null) {
                    logOnFailure(api.getPrivateClient().setDoorbellChimeDuration(deviceId, duration.intValue()),
                            "set chime duration");
                }
                break;
            }
            case CHANNEL_HIGH_FPS_ENABLED:
                if (command instanceof OnOffType highFps) {
                    logOnFailure(api.getPrivateClient().setCameraVideoMode(deviceId,
                            highFps == OnOffType.ON ? "highFps" : "default"), "set high FPS mode");
                }
                break;
            case CHANNEL_HDR_ENABLED:
                if (command instanceof OnOffType hdr) {
                    logOnFailure(api.getPrivateClient().setCameraHDR(deviceId, hdr == OnOffType.ON), "set HDR");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void refreshFromDevice(Camera camera) {
        super.refreshFromDevice(camera);
        addRemoveChannels();
        updateRtspsChannels(camera);
        updateFromPrivateDevice(camera);
    }

    /**
     * Internal method to update all channels from camera data
     */
    protected void updateFromPrivateDevice(Camera privCamera) {
        logger.debug("Updating from private device: {}", privCamera);

        // Settings previously read from public API
        if (privCamera.featureFlags != null && Boolean.TRUE.equals(privCamera.featureFlags.hasMic)) {
            updateDimmerChannel(CHANNEL_MIC_VOLUME, privCamera.micVolume);
        }
        if (privCamera.osdSettings != null) {
            updateBooleanChannel(CHANNEL_OSD_NAME, privCamera.osdSettings.isNameEnabled);
            updateBooleanChannel(CHANNEL_OSD_DATE, privCamera.osdSettings.isDateEnabled);
            updateBooleanChannel(CHANNEL_OSD_LOGO, privCamera.osdSettings.isLogoEnabled);
        }
        if (privCamera.ledSettings != null) {
            updateBooleanChannel(CHANNEL_LED_ENABLED, privCamera.ledSettings.isEnabled);
        }
        if (privCamera.lcdMessage != null && privCamera.lcdMessage.text != null) {
            updateStringChannel(CHANNEL_DOORBELL_DEFAULT_MESSAGE, privCamera.lcdMessage.text);
            updateTimeChannel(CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT,
                    privCamera.lcdMessage.resetAt != null ? privCamera.lcdMessage.resetAt.toEpochMilli() : null);
        }
        if (privCamera.hdrType != null) {
            updateStringChannel(CHANNEL_HDR_TYPE, privCamera.hdrType);
        }
        if (privCamera.videoMode != null) {
            updateStringChannel(CHANNEL_VIDEO_MODE, privCamera.videoMode.toString());
        }
        if (privCamera.activePatrolSlot != null) {
            updateIntegerChannel(CHANNEL_ACTIVE_PATROL_SLOT, privCamera.activePatrolSlot);
        }
        if (privCamera.name != null) {
            updateProperty(PROPERTY_NAME, privCamera.name);
        }
        if (privCamera.state != null) {
            updateStringChannel(CHANNEL_DEVICE_STATE, privCamera.state.toString());
        }

        // Device status flags
        if (privCamera.isMotionDetected != null) {
            updateState(CHANNEL_IS_MOTION_DETECTED, OnOffType.from(privCamera.isMotionDetected));
        }
        if (privCamera.isSmartDetected != null) {
            updateState(CHANNEL_IS_SMART_DETECTED, OnOffType.from(privCamera.isSmartDetected));
        }
        if (privCamera.isRecording != null) {
            updateState(CHANNEL_IS_RECORDING, OnOffType.from(privCamera.isRecording));
        }
        if (privCamera.isMicEnabled != null) {
            updateState(CHANNEL_MIC_ENABLED, OnOffType.from(privCamera.isMicEnabled));
        }
        if (privCamera.upSince != null) {
            updateDateTimeChannel(CHANNEL_UPTIME_STARTED, privCamera.upSince.toEpochMilli());
        }
        if (privCamera.connectedSince != null) {
            updateDateTimeChannel(CHANNEL_CONNECTED_SINCE, privCamera.connectedSince.toEpochMilli());
        }
        if (privCamera.lastSeen != null) {
            updateDateTimeChannel(CHANNEL_LAST_SEEN, privCamera.lastSeen.toEpochMilli());
        }
        if (privCamera.lastRing != null) {
            updateDateTimeChannel(CHANNEL_LAST_RING, privCamera.lastRing.toEpochMilli());
        }
        if (privCamera.marketName != null) {
            updateProperty(PROPERTY_MODEL, privCamera.marketName);
        } else if (privCamera.type != null) {
            updateProperty(PROPERTY_MODEL, privCamera.type);
        }
        if (privCamera.firmwareVersion != null) {
            updateProperty(PROPERTY_FIRMWARE_VERSION, privCamera.firmwareVersion);
        }
        if (privCamera.mac != null) {
            updateProperty(PROPERTY_MAC_ADDRESS, privCamera.mac);
        }
        if (privCamera.host != null) {
            updateProperty(PROPERTY_IP_ADDRESS, privCamera.host);
        }
        if (privCamera.uptime != null) {
            updateState(CHANNEL_DEVICE_UPTIME, new QuantityType<>(privCamera.uptime.toMillis(), Units.SECOND));
        }
        if (privCamera.lcdMessage != null && privCamera.lcdMessage.text != null) {
            updateStringChannel(CHANNEL_LCD_MESSAGE, privCamera.lcdMessage.text);
        }
        if (privCamera.lastRing != null) {
            long secondsSinceRing = Duration.between(privCamera.lastRing, Instant.now()).getSeconds();
            boolean isRinging = secondsSinceRing < 5; // Consider "ringing" if within last 5 seconds
            updateState(CHANNEL_IS_RINGING, OnOffType.from(isRinging));
        }
        if (privCamera.isDark != null) {
            updateState(CHANNEL_IS_DARK, OnOffType.from(privCamera.isDark));
        }
        if (privCamera.lastSmart != null) {
            updateDateTimeChannel(CHANNEL_LAST_SMART, privCamera.lastSmart.toEpochMilli());
        }
        if (privCamera.isLiveHeatmapEnabled != null) {
            updateState(CHANNEL_IS_LIVE_HEATMAP_ENABLED, OnOffType.from(privCamera.isLiveHeatmapEnabled));
        }
        if (privCamera.videoReconfigurationInProgress != null) {
            updateState(CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS,
                    OnOffType.from(privCamera.videoReconfigurationInProgress));
        }
        if (privCamera.phyRate != null) {
            updateState(CHANNEL_PHY_RATE, new DecimalType(privCamera.phyRate));
        }
        if (privCamera.isProbingForWifi != null) {
            updateState(CHANNEL_IS_PROBING_FOR_WIFI, OnOffType.from(privCamera.isProbingForWifi));
        }
        if (privCamera.isPoorNetwork != null) {
            updateState(CHANNEL_IS_POOR_NETWORK, OnOffType.from(privCamera.isPoorNetwork));
        }
        if (privCamera.isWirelessUplinkEnabled != null) {
            updateState(CHANNEL_IS_WIRELESS_UPLINK_ENABLED, OnOffType.from(privCamera.isWirelessUplinkEnabled));
        }
        if (privCamera.voltage != null) {
            updateState(CHANNEL_VOLTAGE, new QuantityType<>(privCamera.voltage, Units.VOLT));
        }
        if (privCamera.batteryStatus != null) {
            if (privCamera.batteryStatus.percentage != null) {
                updateState(CHANNEL_BATTERY_PERCENTAGE,
                        new QuantityType<>(privCamera.batteryStatus.percentage, Units.PERCENT));
            }
            if (privCamera.batteryStatus.isCharging != null) {
                updateState(CHANNEL_BATTERY_IS_CHARGING, OnOffType.from(privCamera.batteryStatus.isCharging));
            }
            if (privCamera.batteryStatus.sleepState != null) {
                updateStringChannel(CHANNEL_BATTERY_SLEEP_STATE, privCamera.batteryStatus.sleepState);
            }
        }
        if (privCamera.isThirdPartyCamera != null) {
            updateProperty(PROPERTY_IS_THIRD_PARTY_CAMERA, String.valueOf(privCamera.isThirdPartyCamera));
        }
        if (privCamera.platform != null) {
            updateProperty(PROPERTY_PLATFORM, privCamera.platform);
        }
        if (privCamera.hasSpeaker != null) {
            updateProperty(PROPERTY_HAS_SPEAKER, String.valueOf(privCamera.hasSpeaker));
        }
        if (privCamera.hasWifi != null) {
            updateProperty(PROPERTY_HAS_WIFI, String.valueOf(privCamera.hasWifi));
        }
        if (privCamera.hasBattery != null) {
            updateProperty(PROPERTY_HAS_BATTERY, String.valueOf(privCamera.hasBattery));
        }
        if (privCamera.stats != null && privCamera.stats.wifi != null) {
            if (privCamera.stats.wifi.channel != null) {
                updateState(CHANNEL_WIFI_CHANNEL, new DecimalType(privCamera.stats.wifi.channel));
            }
            if (privCamera.stats.wifi.frequency != null) {
                updateState(CHANNEL_WIFI_FREQUENCY, new DecimalType(privCamera.stats.wifi.frequency));
            }
            if (privCamera.stats.wifi.signalQuality != null) {
                updateState(CHANNEL_WIFI_SIGNAL_QUALITY,
                        new QuantityType<>(privCamera.stats.wifi.signalQuality, Units.PERCENT));
            }
            if (privCamera.stats.wifi.signalStrength != null) {
                updateState(CHANNEL_WIFI_SIGNAL_STRENGTH, new DecimalType(privCamera.stats.wifi.signalStrength));
            }
        }
        if (privCamera.stats != null && privCamera.stats.storage != null) {
            if (privCamera.stats.storage.used != null) {
                // Storage is in bytes - use DecimalType since channel is Number:DataAmount
                updateState(CHANNEL_STORAGE_USED, new DecimalType(privCamera.stats.storage.used));
            }
            if (privCamera.stats.storage.rate != null) {
                updateState(CHANNEL_STORAGE_RATE, new DecimalType(privCamera.stats.storage.getRatePerSecond()));
            }
        }
        if (privCamera.ispSettings != null && privCamera.ispSettings.wdr != null) {
            updateState(CHANNEL_CAMERA_WDR_LEVEL, new DecimalType(privCamera.ispSettings.wdr));
        }

        // Recording settings
        if (privCamera.recordingSettings != null) {
            if (privCamera.recordingSettings.mode != null) {
                updateStringChannel(CHANNEL_RECORDING_MODE, privCamera.recordingSettings.mode.toString());
            }
            if (privCamera.recordingSettings.enableMotionDetection != null) {
                updateState(CHANNEL_MOTION_DETECTION_ENABLED,
                        OnOffType.from(privCamera.recordingSettings.enableMotionDetection));
            }
        }

        // Smart detection settings
        if (privCamera.smartDetectSettings != null && privCamera.smartDetectSettings.objectTypes != null) {
            List<SmartDetectObjectType> types = privCamera.smartDetectSettings.objectTypes;
            updateState(CHANNEL_SMART_DETECT_PERSON_ENABLED,
                    OnOffType.from(types.contains(SmartDetectObjectType.PERSON)));
            updateState(CHANNEL_SMART_DETECT_VEHICLE_ENABLED,
                    OnOffType.from(types.contains(SmartDetectObjectType.VEHICLE)));
            updateState(CHANNEL_SMART_DETECT_FACE_ENABLED, OnOffType.from(types.contains(SmartDetectObjectType.FACE)));
            updateState(CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED,
                    OnOffType.from(types.contains(SmartDetectObjectType.LICENSE_PLATE)));
            updateState(CHANNEL_SMART_DETECT_PACKAGE_ENABLED,
                    OnOffType.from(types.contains(SmartDetectObjectType.PACKAGE)));
            updateState(CHANNEL_SMART_DETECT_ANIMAL_ENABLED,
                    OnOffType.from(types.contains(SmartDetectObjectType.ANIMAL)));
        }

        // IR Mode
        if (privCamera.ispSettings != null && privCamera.ispSettings.irLedMode != null) {
            updateStringChannel(CHANNEL_IR_MODE, privCamera.ispSettings.irLedMode.toString());
        }

        // HDR enabled
        if (privCamera.hdrMode != null) {
            updateState(CHANNEL_HDR_ENABLED, OnOffType.from(privCamera.hdrMode));
        }

        // High FPS enabled
        if (privCamera.videoMode != null) {
            boolean isHighFps = privCamera.videoMode.toString().equalsIgnoreCase("highFps");
            updateState(CHANNEL_HIGH_FPS_ENABLED, OnOffType.from(isHighFps));
        }

        // Speaker settings
        if (privCamera.speakerSettings != null) {
            if (privCamera.speakerSettings.volume != null) {
                updateDimmerChannel(CHANNEL_CAMERA_SPEAKER_VOLUME, privCamera.speakerSettings.volume);
            } else if (privCamera.speakerSettings.speakerVolume != null) {
                // Fallback to speakerVolume field if volume is null
                updateDimmerChannel(CHANNEL_CAMERA_SPEAKER_VOLUME, privCamera.speakerSettings.speakerVolume);
            }
            if (privCamera.speakerSettings.ringVolume != null) {
                updateDimmerChannel(CHANNEL_DOORBELL_RING_VOLUME, privCamera.speakerSettings.ringVolume);
            }
        }

        // Chime duration
        if (privCamera.chimeDuration != null) {
            updateState(CHANNEL_DOORBELL_CHIME_DURATION, new QuantityType<>(privCamera.chimeDuration, Units.SECOND));
        }

        // Zoom position
        if (privCamera.ispSettings != null && privCamera.ispSettings.zoomPosition != null) {
            updateDimmerChannel(CHANNEL_CAMERA_ZOOM_LEVEL, privCamera.ispSettings.zoomPosition);
        }
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType eventType) {
        if (event.type == null) {
            return;
        }
        switch (event.type) {
            case CAMERA_MOTION:
                maybeUpdateSnapshot(CHANNEL_MOTION_SNAPSHOT, Sequence.BEFORE);
                // Trigger motion on start and end if channel exists
                String motionChannelId = eventType == WSEventType.ADD ? CHANNEL_MOTION_START : CHANNEL_MOTION_UPDATE;
                if (hasChannel(motionChannelId)) {
                    triggerChannel(new ChannelUID(thing.getUID(), motionChannelId));
                    updateContactChannel(CHANNEL_MOTION_CONTACT, OpenClosedType.OPEN);
                }
                maybeUpdateSnapshot(CHANNEL_MOTION_SNAPSHOT, Sequence.AFTER);
                break;

            case SMART_AUDIO_DETECT:
                if (event instanceof CameraSmartDetectAudioEvent e) {
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT, Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD ? CHANNEL_SMART_DETECT_AUDIO_START
                            : CHANNEL_SMART_DETECT_AUDIO_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(CHANNEL_SMART_DETECT_AUDIO_CONTACT, OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT, Sequence.AFTER);
                }
                break;

            case SMART_DETECT_ZONE:
                if (event instanceof CameraSmartDetectZoneEvent e) {
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_ZONE_SNAPSHOT, Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD ? CHANNEL_SMART_DETECT_ZONE_START
                            : CHANNEL_SMART_DETECT_ZONE_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(CHANNEL_SMART_DETECT_ZONE_CONTACT, OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_ZONE_SNAPSHOT, Sequence.AFTER);
                }
                break;

            case SMART_DETECT_LINE:
                if (event instanceof CameraSmartDetectLineEvent e) {
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_LINE_SNAPSHOT, Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD ? CHANNEL_SMART_DETECT_LINE_START
                            : CHANNEL_SMART_DETECT_LINE_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(CHANNEL_SMART_DETECT_LINE_CONTACT, OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_LINE_SNAPSHOT, Sequence.AFTER);
                }
                break;

            case SMART_DETECT_LOITER_ZONE:
                if (event instanceof CameraSmartDetectLoiterEvent e) {
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_LOITER_SNAPSHOT, Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD ? CHANNEL_SMART_DETECT_LOITER_START
                            : CHANNEL_SMART_DETECT_LOITER_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(CHANNEL_SMART_DETECT_LOITER_CONTACT, OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(CHANNEL_SMART_DETECT_LOITER_SNAPSHOT, Sequence.AFTER);
                }
                break;
            case RING:
                if (event instanceof RingEvent && hasChannel(CHANNEL_RING)) {
                    maybeUpdateSnapshot(CHANNEL_RING_SNAPSHOT, Sequence.BEFORE);
                    triggerChannel(new ChannelUID(thing.getUID(), CHANNEL_RING),
                            event.end == null ? "PRESSED" : "RELEASED");
                    updateContactChannel(CHANNEL_RING_CONTACT, OpenClosedType.OPEN);
                    maybeUpdateSnapshot(CHANNEL_RING_SNAPSHOT, Sequence.AFTER);
                }
                break;
            default:
                // ignore other event types in camera handler
                break;
        }
    }

    public TalkbackSession startTalkback() throws IOException {
        UniFiProtectHybridClient api = getApiClient();
        if (api == null) {
            throw new IOException("API client is null");
        }
        return api.getPublicClient().createTalkbackSession(deviceId);
    }

    public byte[] getSnapshot(boolean highQuality) throws IOException {
        UniFiProtectHybridClient api = getApiClient();
        if (api == null) {
            throw new IOException("API client is null");
        }
        return api.getPublicClient().getSnapshot(deviceId, highQuality);
    }

    private void updateRtspsChannels(Camera privCamera) {
        UniFiProtectHybridClient api = getApiClient();
        if (api == null) {
            return;
        }

        // Build static RTSP URLs from camera channels (these don't change)
        String rtspUrlHigh = null;
        String rtspUrlMedium = null;
        String rtspUrlLow = null;

        // Get static rtspAlias from Private API camera channels
        try {
            Thing bridge = getBridge();
            String host = null;
            if (bridge != null && bridge.getHandler() instanceof UnifiProtectNVRHandler nvrHandler) {
                host = nvrHandler.getHostname();
            }

            if (host != null && privCamera.channels != null) {
                int port = 7441; // Default rtsps port
                for (var channel : privCamera.channels) {
                    if (channel.rtspAlias != null && channel.isRtspEnabled != null && channel.isRtspEnabled) {
                        String url = String.format("rtsps://%s:%d/%s", host, port, channel.rtspAlias);
                        if ("High".equalsIgnoreCase(channel.name)) {
                            rtspUrlHigh = url;
                        } else if ("Medium".equalsIgnoreCase(channel.name)) {
                            rtspUrlMedium = url;
                        } else if ("Low".equalsIgnoreCase(channel.name)) {
                            rtspUrlLow = url;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to get static RTSP URLs from private API", e);
        }

        // Update existing channels with static URLs
        updateStringChannel(CHANNEL_RTSP_URL_HIGH, rtspUrlHigh);
        updateStringChannel(CHANNEL_RTSP_URL_MEDIUM, rtspUrlMedium);
        updateStringChannel(CHANNEL_RTSP_URL_LOW, rtspUrlLow);
        updateStringChannel(CHANNEL_RTSP_URL_PACKAGE, null); // Package URL not in channels

        // For WebRTC streaming, create temporary streams only when registering with media service
        RtspsStreams rtsps = null;

        // Query and Create RTSP streams if WebRTC is enabled
        if (enableWebRTC) {
            try {
                rtsps = api.getPublicClient().getRtspsStream(deviceId);
                List<ChannelQuality> qualities = new ArrayList<>();
                if (rtsps.high == null) {
                    qualities.add(ChannelQuality.HIGH);
                }
                if (rtsps.medium == null) {
                    qualities.add(ChannelQuality.MEDIUM);
                }
                if (rtsps.low == null) {
                    qualities.add(ChannelQuality.LOW);
                }
                if (!qualities.isEmpty()) {
                    rtsps = api.getPublicClient().createRtspsStream(deviceId, qualities);
                }
            } catch (IOException e) {
                logger.debug("Failed to manage RTSP streams", e);
            }
        }

        // Register new streams if available
        if (enableWebRTC && rtsps != null) {
            URI bc = null;
            try {
                TalkbackSession talkback = startTalkback();
                bc = URI.create(talkback.url);
            } catch (IOException e) {
                logger.debug("Talkback not supported: {}", e.getMessage());
            }
            final URI backChannel = bc;
            // streamId -> sources (backchannel + main stream)
            Map<String, List<URI>> streams = new LinkedHashMap<>();
            BiConsumer<String, @Nullable String> add = (type, url) -> {
                if (url != null && !url.isBlank()) {
                    List<URI> sources = new ArrayList<>();
                    sources.add(URI.create(url));
                    if (backChannel != null) {
                        sources.add(backChannel);
                    }
                    String fullStreamId = baseSourceId + ":" + type;
                    streams.put(fullStreamId, sources);
                    String webRTCId = CHANNEL_WEBRTC_URL + "-" + type;
                    String webRTCUrl = media.getPlayBasePath() + "/" + fullStreamId;
                    // set a property as well as a channel so users don't need to map to an Item
                    getThing().setProperty(webRTCId, webRTCUrl);
                    updateStringChannel(webRTCId, webRTCUrl);
                }
            };
            add.accept("high", rtsps.high);
            add.accept("medium", rtsps.medium);
            add.accept("low", rtsps.low);
            add.accept("package", rtsps.packageUrl);
            media.registerHandler(this, streams);
        } else {
            media.registerHandler(this, Map.of());
        }
    }

    private void addRemoveChannels() {
        Camera camera = device;
        if (camera == null || camera.featureFlags == null) {
            logger.debug("Camera or feature flags are null, skipping channel addition");
            return;
        }
        List<Channel> channelAdd = new ArrayList<>();

        // active channel ids set accumulates all channels that should exist after this call
        Set<String> activeChannelIds = new HashSet<>();

        addChannel(CHANNEL_RTSP_URL_HIGH, STRING, CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                CHANNEL_RTSP_URL_HIGH_LABEL);
        addChannel(CHANNEL_RTSP_URL_MEDIUM, STRING, CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                CHANNEL_RTSP_URL_MEDIUM_LABEL);
        addChannel(CHANNEL_RTSP_URL_LOW, STRING, CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                CHANNEL_RTSP_URL_LOW_LABEL);
        addChannel(CHANNEL_RTSP_URL_PACKAGE, STRING, CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                CHANNEL_RTSP_URL_PACKAGE_LABEL);

        addChannel(CHANNEL_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds, CHANNEL_SNAPSHOT_LABEL);

        addChannel(CHANNEL_SNAPSHOT_URL, STRING, CHANNEL_SNAPSHOT_URL, channelAdd, activeChannelIds,
                CHANNEL_SNAPSHOT_URL_LABEL);

        String snapshotUrl = media.getImageBasePath() + "/" + baseSourceId;
        updateStringChannel(CHANNEL_SNAPSHOT_URL, snapshotUrl);
        getThing().setProperty("snapshot-url", snapshotUrl);

        if (enableWebRTC) {
            addChannel(CHANNEL_WEBRTC_URL_HIGH, STRING, CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    CHANNEL_WEBRTC_URL_HIGH_LABEL);
            addChannel(CHANNEL_WEBRTC_URL_MEDIUM, STRING, CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    CHANNEL_WEBRTC_URL_MEDIUM_LABEL);
            addChannel(CHANNEL_WEBRTC_URL_LOW, STRING, CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    CHANNEL_WEBRTC_URL_LOW_LABEL);
            addChannel(CHANNEL_WEBRTC_URL_PACKAGE, STRING, CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    CHANNEL_WEBRTC_URL_PACKAGE_LABEL);
        }

        Camera.CameraFeatureFlags flags = Objects.requireNonNull(camera.featureFlags, "Feature flags are required");
        if (Boolean.TRUE.equals(flags.hasMic)) {
            addChannel(CHANNEL_MIC_VOLUME, DIMMER, CHANNEL_MIC_VOLUME, channelAdd, activeChannelIds);
        }
        if (Boolean.TRUE.equals(flags.hasLedStatus)) {
            addChannel(CHANNEL_LED_ENABLED, SWITCH, CHANNEL_LED_ENABLED, channelAdd, activeChannelIds);
        }
        if (Boolean.TRUE.equals(flags.hasHdr)) {
            addChannel(CHANNEL_HDR_TYPE, STRING, CHANNEL_HDR_TYPE, channelAdd, activeChannelIds);
        }
        if (flags.videoModes != null && !flags.videoModes.isEmpty()) {
            addChannel(CHANNEL_VIDEO_MODE, STRING, CHANNEL_VIDEO_MODE, channelAdd, activeChannelIds);
        }
        addTriggerChannel(CHANNEL_MOTION_START, CHANNEL_MOTION, channelAdd, activeChannelIds,
                CHANNEL_MOTION_START_LABEL);
        addTriggerChannel(CHANNEL_MOTION_UPDATE, CHANNEL_MOTION, channelAdd, activeChannelIds,
                CHANNEL_MOTION_UPDATE_LABEL);
        addChannel(CHANNEL_MOTION_CONTACT, CONTACT, CHANNEL_MOTION_CONTACT, channelAdd, activeChannelIds);
        addChannel(CHANNEL_MOTION_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds);
        addChannel(CHANNEL_MOTION_DETECTION_ENABLED, SWITCH, CHANNEL_MOTION_DETECTION_ENABLED, channelAdd,
                activeChannelIds);

        addChannel(CHANNEL_IS_MOTION_DETECTED, SWITCH, CHANNEL_IS_MOTION_DETECTED, channelAdd, activeChannelIds);
        addChannel(CHANNEL_IS_SMART_DETECTED, SWITCH, CHANNEL_IS_SMART_DETECTED, channelAdd, activeChannelIds);
        addChannel(CHANNEL_IS_RECORDING, SWITCH, CHANNEL_IS_RECORDING, channelAdd, activeChannelIds);
        addChannel(CHANNEL_DEVICE_STATE, STRING, CHANNEL_DEVICE_STATE, channelAdd, activeChannelIds);
        addChannel(CHANNEL_DEVICE_UPTIME, "Number:Time", CHANNEL_DEVICE_UPTIME, channelAdd, activeChannelIds);
        addChannel(CHANNEL_UPTIME_STARTED, DATETIME, CHANNEL_UPTIME_STARTED, channelAdd, activeChannelIds);
        addChannel(CHANNEL_CONNECTED_SINCE, DATETIME, CHANNEL_CONNECTED_SINCE, channelAdd, activeChannelIds);
        addChannel(CHANNEL_LAST_SEEN, DATETIME, CHANNEL_LAST_SEEN, channelAdd, activeChannelIds);

        addChannel(CHANNEL_USE_GLOBAL_SETTINGS, SWITCH, CHANNEL_USE_GLOBAL_SETTINGS, channelAdd, activeChannelIds);

        // Recording Mode
        addChannel(CHANNEL_RECORDING_MODE, STRING, CHANNEL_RECORDING_MODE, channelAdd, activeChannelIds);

        if (flags.smartDetectTypes != null && !flags.smartDetectTypes.isEmpty()) {
            addTriggerChannel(CHANNEL_SMART_DETECT_ZONE_START, CHANNEL_SMART_DETECT_ZONE, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_ZONE_START_LABEL);
            addTriggerChannel(CHANNEL_SMART_DETECT_ZONE_UPDATE, CHANNEL_SMART_DETECT_ZONE, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_ZONE_UPDATE_LABEL);
            addChannel(CHANNEL_SMART_DETECT_ZONE_CONTACT, CONTACT, CHANNEL_SMART_DETECT_ZONE_CONTACT, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_ZONE_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_ZONE_SNAPSHOT_LABEL);

            addTriggerChannel(CHANNEL_SMART_DETECT_LINE_START, CHANNEL_SMART_DETECT_LINE, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_LINE_START_LABEL);
            addTriggerChannel(CHANNEL_SMART_DETECT_LINE_UPDATE, CHANNEL_SMART_DETECT_LINE, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_LINE_UPDATE_LABEL);
            addChannel(CHANNEL_SMART_DETECT_LINE_CONTACT, CONTACT, CHANNEL_SMART_DETECT_LINE_CONTACT, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_LINE_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_LINE_SNAPSHOT_LABEL);

            addTriggerChannel(CHANNEL_SMART_DETECT_LOITER_START, CHANNEL_SMART_DETECT_LOITER, channelAdd,
                    activeChannelIds, CHANNEL_SMART_DETECT_LOITER_START_LABEL);
            addTriggerChannel(CHANNEL_SMART_DETECT_LOITER_UPDATE, CHANNEL_SMART_DETECT_LOITER, channelAdd,
                    activeChannelIds, CHANNEL_SMART_DETECT_LOITER_UPDATE_LABEL);
            addChannel(CHANNEL_SMART_DETECT_LOITER_CONTACT, CONTACT, CHANNEL_SMART_DETECT_LOITER_CONTACT, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_LOITER_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_LOITER_SNAPSHOT_LABEL);
        }
        if (flags.smartDetectAudioTypes != null && !flags.smartDetectAudioTypes.isEmpty()) {
            addTriggerChannel(CHANNEL_SMART_DETECT_AUDIO_START, CHANNEL_SMART_DETECT_AUDIO, channelAdd,
                    activeChannelIds, CHANNEL_SMART_DETECT_AUDIO_START_LABEL);
            addTriggerChannel(CHANNEL_SMART_DETECT_AUDIO_UPDATE, CHANNEL_SMART_DETECT_AUDIO, channelAdd,
                    activeChannelIds, CHANNEL_SMART_DETECT_AUDIO_UPDATE_LABEL);
            addChannel(CHANNEL_SMART_DETECT_AUDIO_CONTACT, CONTACT, CHANNEL_SMART_DETECT_AUDIO_CONTACT, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT_LABEL);
        }
        if (camera.osdSettings != null) {
            addChannel(CHANNEL_OSD_NAME, SWITCH, CHANNEL_OSD_NAME, channelAdd, activeChannelIds);
            addChannel(CHANNEL_OSD_DATE, SWITCH, CHANNEL_OSD_DATE, channelAdd, activeChannelIds);
            addChannel(CHANNEL_OSD_LOGO, SWITCH, CHANNEL_OSD_LOGO, channelAdd, activeChannelIds);
        }
        if (camera.activePatrolSlot != null) {
            addChannel(CHANNEL_ACTIVE_PATROL_SLOT, NUMBER, CHANNEL_ACTIVE_PATROL_SLOT, channelAdd, activeChannelIds);
        }

        if (camera.isDoorbell()) {
            addChannel(CHANNEL_DOORBELL_RING_VOLUME, DIMMER, CHANNEL_DOORBELL_RING_VOLUME, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_DOORBELL_CHIME_DURATION, "Number:Time", CHANNEL_DOORBELL_CHIME_DURATION, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_LCD_MESSAGE, STRING, CHANNEL_LCD_MESSAGE, channelAdd, activeChannelIds);
            addChannel(CHANNEL_LAST_RING, DATETIME, CHANNEL_LAST_RING, channelAdd, activeChannelIds);
            addChannel(CHANNEL_IS_RINGING, SWITCH, CHANNEL_IS_RINGING, channelAdd, activeChannelIds);

            addTriggerChannel(CHANNEL_RING, CHANNEL_RING, channelAdd, activeChannelIds, CHANNEL_RING_LABEL);
            addChannel(CHANNEL_RING_CONTACT, CONTACT, CHANNEL_RING_CONTACT, channelAdd, activeChannelIds);
            addChannel(CHANNEL_RING_SNAPSHOT, IMAGE, CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    CHANNEL_RING_SNAPSHOT_LABEL);
            addChannel(CHANNEL_DOORBELL_DEFAULT_MESSAGE, STRING, CHANNEL_DOORBELL_DEFAULT_MESSAGE, channelAdd,
                    activeChannelIds, CHANNEL_DOORBELL_DEFAULT_MESSAGE_LABEL);
            addChannel(CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT, "Number:Time",
                    CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT, channelAdd, activeChannelIds,
                    CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT_LABEL);
        }

        if (camera.featureFlags.hasPtz != null && camera.featureFlags.hasPtz) {
            addChannel(CHANNEL_PTZ_RELATIVE_PAN, NUMBER, CHANNEL_PTZ_RELATIVE_PAN, channelAdd, activeChannelIds);
            addChannel(CHANNEL_PTZ_RELATIVE_TILT, NUMBER, CHANNEL_PTZ_RELATIVE_TILT, channelAdd, activeChannelIds);
            addChannel(CHANNEL_PTZ_RELATIVE_ZOOM, NUMBER, CHANNEL_PTZ_RELATIVE_ZOOM, channelAdd, activeChannelIds);
            addChannel(CHANNEL_PTZ_CENTER, STRING, CHANNEL_PTZ_CENTER, channelAdd, activeChannelIds);
            addChannel(CHANNEL_PTZ_SET_HOME, SWITCH, CHANNEL_PTZ_SET_HOME, channelAdd, activeChannelIds);

            addChannel(CHANNEL_PTZ_CREATE_PRESET, STRING, CHANNEL_PTZ_CREATE_PRESET, channelAdd, activeChannelIds);
            addChannel(CHANNEL_PTZ_DELETE_PRESET, NUMBER, CHANNEL_PTZ_DELETE_PRESET, channelAdd, activeChannelIds);
        }

        // Smart Detection Controls
        if (flags.smartDetectTypes != null && !flags.smartDetectTypes.isEmpty()) {
            addChannel(CHANNEL_SMART_DETECT_PERSON_ENABLED, SWITCH, CHANNEL_SMART_DETECT_PERSON_ENABLED, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_VEHICLE_ENABLED, SWITCH, CHANNEL_SMART_DETECT_VEHICLE_ENABLED, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_FACE_ENABLED, SWITCH, CHANNEL_SMART_DETECT_FACE_ENABLED, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED, SWITCH, CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED,
                    channelAdd, activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_PACKAGE_ENABLED, SWITCH, CHANNEL_SMART_DETECT_PACKAGE_ENABLED, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_SMART_DETECT_ANIMAL_ENABLED, SWITCH, CHANNEL_SMART_DETECT_ANIMAL_ENABLED, channelAdd,
                    activeChannelIds);
        }

        if (Boolean.TRUE.equals(flags.hasMic)) {
            addChannel(CHANNEL_MIC_ENABLED, SWITCH, CHANNEL_MIC_ENABLED, channelAdd, activeChannelIds);
        }

        if (camera.featureFlags.hasLedIr != null && camera.featureFlags.hasLedIr) {
            addChannel(CHANNEL_IR_MODE, STRING, CHANNEL_IR_MODE, channelAdd, activeChannelIds);
        }

        if (Boolean.TRUE.equals(flags.hasHdr)) {
            addChannel(CHANNEL_HDR_ENABLED, SWITCH, CHANNEL_HDR_ENABLED, channelAdd, activeChannelIds);
        }
        if (flags.videoModes != null && !flags.videoModes.isEmpty()) {
            addChannel(CHANNEL_HIGH_FPS_ENABLED, SWITCH, CHANNEL_HIGH_FPS_ENABLED, channelAdd, activeChannelIds);
        }

        if (Boolean.TRUE.equals(flags.hasSpeaker)) {
            addChannel(CHANNEL_CAMERA_SPEAKER_VOLUME, DIMMER, CHANNEL_CAMERA_SPEAKER_VOLUME, channelAdd,
                    activeChannelIds);
        }

        if (camera.featureFlags.canOpticalZoom != null && camera.featureFlags.canOpticalZoom) {
            addChannel(CHANNEL_CAMERA_ZOOM_LEVEL, DIMMER, CHANNEL_CAMERA_ZOOM_LEVEL, channelAdd, activeChannelIds);
        }
        if (camera.ispSettings != null && camera.ispSettings.wdr != null) {
            addChannel(CHANNEL_CAMERA_WDR_LEVEL, NUMBER, CHANNEL_CAMERA_WDR_LEVEL, channelAdd, activeChannelIds);
        }

        if (camera.isDark != null) {
            addChannel(CHANNEL_IS_DARK, SWITCH, CHANNEL_IS_DARK, channelAdd, activeChannelIds);
        }
        if (camera.lastSmart != null) {
            addChannel(CHANNEL_LAST_SMART, DATETIME, CHANNEL_LAST_SMART, channelAdd, activeChannelIds);
        }
        if (camera.isLiveHeatmapEnabled != null) {
            addChannel(CHANNEL_IS_LIVE_HEATMAP_ENABLED, SWITCH, CHANNEL_IS_LIVE_HEATMAP_ENABLED, channelAdd,
                    activeChannelIds);
        }
        if (camera.videoReconfigurationInProgress != null) {
            addChannel(CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS, SWITCH, CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS,
                    channelAdd, activeChannelIds);
        }

        if (camera.hasWifi != null && camera.hasWifi) {
            addChannel(CHANNEL_PHY_RATE, NUMBER, CHANNEL_PHY_RATE, channelAdd, activeChannelIds);
            addChannel(CHANNEL_IS_PROBING_FOR_WIFI, SWITCH, CHANNEL_IS_PROBING_FOR_WIFI, channelAdd, activeChannelIds);
            addChannel(CHANNEL_IS_POOR_NETWORK, SWITCH, CHANNEL_IS_POOR_NETWORK, channelAdd, activeChannelIds);
            addChannel(CHANNEL_IS_WIRELESS_UPLINK_ENABLED, SWITCH, CHANNEL_IS_WIRELESS_UPLINK_ENABLED, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_WIFI_CHANNEL, NUMBER, CHANNEL_WIFI_CHANNEL, channelAdd, activeChannelIds);
            addChannel(CHANNEL_WIFI_FREQUENCY, NUMBER, CHANNEL_WIFI_FREQUENCY, channelAdd, activeChannelIds);
            addChannel(CHANNEL_WIFI_SIGNAL_QUALITY, "Number:Dimensionless", CHANNEL_WIFI_SIGNAL_QUALITY, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_WIFI_SIGNAL_STRENGTH, NUMBER, CHANNEL_WIFI_SIGNAL_STRENGTH, channelAdd,
                    activeChannelIds);

        }

        if (camera.hasBattery != null && camera.hasBattery) {
            addChannel(CHANNEL_VOLTAGE, "Number:ElectricPotential", CHANNEL_VOLTAGE, channelAdd, activeChannelIds);
            addChannel(CHANNEL_BATTERY_PERCENTAGE, "Number:Dimensionless", CHANNEL_BATTERY_PERCENTAGE, channelAdd,
                    activeChannelIds);
            addChannel(CHANNEL_BATTERY_IS_CHARGING, SWITCH, CHANNEL_BATTERY_IS_CHARGING, channelAdd, activeChannelIds);
            addChannel(CHANNEL_BATTERY_SLEEP_STATE, STRING, CHANNEL_BATTERY_SLEEP_STATE, channelAdd, activeChannelIds);
        }

        // Storage Stats
        addChannel(CHANNEL_STORAGE_USED, "Number:DataAmount", CHANNEL_STORAGE_USED, channelAdd, activeChannelIds);
        addChannel(CHANNEL_STORAGE_RATE, NUMBER, CHANNEL_STORAGE_RATE, channelAdd, activeChannelIds);

        // Device Reboot
        addChannel(CHANNEL_DEVICE_REBOOT, SWITCH, CHANNEL_DEVICE_REBOOT, channelAdd, activeChannelIds);

        updateThing(editThing().withChannels(channelAdd).build());
    }

    private void addChannel(String channelId, String itemType, String channelTypeId, List<Channel> channelAdd,
            Set<String> activeChannelIds) {
        addChannel(channelId, itemType, channelTypeId, channelAdd, activeChannelIds, null);
    }

    private void addChannel(String channelId, String itemType, String channelTypeId, List<Channel> channelAdd,
            Set<String> activeChannelIds, @Nullable String label) {
        ChannelUID uid = new ChannelUID(thing.getUID(), channelId);
        activeChannelIds.add(channelId);
        ChannelBuilder builder = ChannelBuilder.create(uid, itemType)
                .withType(new ChannelTypeUID(BINDING_ID, channelTypeId));
        if (label != null) {
            builder.withLabel(translationService.getTranslation(label));
        }
        channelAdd.add(builder.build());
    }

    private void addTriggerChannel(String channelId, String channelTypeId, List<Channel> channelAdd,
            Set<String> activeChannelIds, @Nullable String label) {
        ChannelUID uid = new ChannelUID(thing.getUID(), channelId);
        activeChannelIds.add(channelId);
        if (thing.getChannel(uid) == null) {
            ChannelBuilder cb = ChannelBuilder.create(uid, null).withType(new ChannelTypeUID(BINDING_ID, channelTypeId))
                    .withKind(ChannelKind.TRIGGER);
            if (label != null) {
                cb.withLabel(translationService.getTranslation(label));
            }
            channelAdd.add(cb.build());
        }
    }

    private Sequence getSnapshotSequence(String channelId) {
        if (getThing().getChannel(channelId) instanceof Channel channel) {
            return UnifiProtectSnapshotConfig.Sequence
                    .fromValue(channel.getConfiguration().as(UnifiProtectSnapshotConfig.class).sequence);
        }
        return UnifiProtectSnapshotConfig.Sequence.BEFORE;
    }

    // Updates the snapshot channel if the sequence is configured to take a snapshot
    // before or after the event or item state change.
    private void maybeUpdateSnapshot(String channelId, Sequence sequence) {
        Sequence sequenceConfig = getSnapshotSequence(channelId);
        if (sequence == sequenceConfig && sequence != Sequence.NONE) {
            updateSnapshot(channelId);
        }
    }

    private void updateSnapshot(String channelId) {
        if (hasChannel(channelId) && isLinked(channelId)) {
            UniFiProtectHybridClient client = getApiClient();
            if (client != null) {
                try {
                    updateState(channelId,
                            new RawType(client.getPublicClient().getSnapshot(deviceId, false), "image/jpeg"));
                } catch (IOException e) {
                    logger.debug("Error getting snapshot", e);
                }
            }

        }
    }

    private void triggerChannel(String channelId, List<? extends ApiValueEnum> smartDetectTypes) {
        String commaDelimitedTypes = smartDetectTypes.stream().map(ApiValueEnum::getApiValue)
                .collect(Collectors.joining(","));
        triggerChannel(new ChannelUID(thing.getUID(), channelId), commaDelimitedTypes);
    }
}
