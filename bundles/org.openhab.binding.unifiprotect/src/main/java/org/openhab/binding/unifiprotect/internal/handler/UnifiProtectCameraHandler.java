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

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifiprotect.internal.api.hybrid.devices.CameraDevice;
import org.openhab.binding.unifiprotect.internal.api.pub.client.UniFiProtectPublicClient;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.ApiValueEnum;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.Camera;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.CameraFeatureFlags;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.ChannelQuality;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.HdrType;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.LcdMessage;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.LedSettings;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.OsdSettings;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.RtspsStreams;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.TalkbackSession;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.VideoMode;
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
import org.openhab.core.library.CoreItemFactory;
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

import com.google.gson.JsonObject;

/**
 * Child handler for a UniFi Protect Camera.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectCameraHandler extends UnifiProtectAbstractDeviceHandler<CameraDevice> {

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
                case UnifiProtectBindingConstants.CHANNEL_SNAPSHOT:
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

        try {
            switch (id) {
                case UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME: {
                    int volume;
                    try {
                        volume = ((DecimalType) command).intValue();
                    } catch (Exception e) {
                        logger.debug("Error parsing mic volume command: {}", command, e);
                        break;
                    }
                    volume = Math.max(0, Math.min(100, volume));
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("micVolume", volume);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_HDR_TYPE: {
                    String value = command.toString();
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("hdrType", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE: {
                    String value = command.toString();
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("videoMode", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_OSD_NAME: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("osdSettings.isNameEnabled", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_OSD_DATE: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("osdSettings.isDateEnabled", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_OSD_LOGO: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("osdSettings.isLogoEnabled", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE: {
                    String value = command.toString();
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("lcdMessage.text", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT: {
                    Long value = timeToMilliseconds(command);
                    // null means forever, zero or less as well
                    if (value != null && value <= 0) {
                        value = null;
                    }
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("lcdMessage.resetAt", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_LED_ENABLED: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectPublicClient.buildPatch("ledSettings.isEnabled", value);
                    Camera updated = api.getPublicClient().patchCamera(deviceId, patch);
                    updateFromPublicDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT: {
                    int slot;
                    try {
                        slot = ((DecimalType) command).intValue();
                    } catch (Exception e) {
                        break;
                    }
                    if (slot <= 0) {
                        api.getPublicClient().ptzPatrolStop(deviceId);
                        updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, 0);
                    } else {
                        api.getPublicClient().ptzPatrolStart(deviceId, String.valueOf(slot));
                        updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, slot);
                    }
                    break;
                }
                // Private API Commands
                case UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_PAN: {
                    if (command instanceof DecimalType pan) {
                        float panValue = pan.floatValue();
                        api.getPrivateClient().ptzRelativeMove(deviceId, panValue, 0, 10, 10, 0)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("PTZ pan failed", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_TILT: {
                    if (command instanceof DecimalType tilt) {
                        float tiltValue = tilt.floatValue();
                        api.getPrivateClient().ptzRelativeMove(deviceId, 0, tiltValue, 10, 10, 0)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("PTZ tilt failed", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_ZOOM: {
                    if (command instanceof DecimalType zoom) {
                        float zoomValue = zoom.floatValue();
                        api.getPrivateClient().ptzZoom(deviceId, zoomValue, 10).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("PTZ zoom failed", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PTZ_CENTER: {
                    if (command instanceof StringType coords) {
                        try {
                            String[] parts = coords.toString().split(",");
                            if (parts.length == 3) {
                                int x = Integer.parseInt(parts[0]);
                                int y = Integer.parseInt(parts[1]);
                                int z = Integer.parseInt(parts[2]);
                                api.getPrivateClient().ptzCenter(deviceId, x, y, z).whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("PTZ center failed", ex);
                                    }
                                });
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Invalid PTZ center coordinates: {}", coords);
                        }
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PTZ_SET_HOME: {
                    if (command == OnOffType.ON) {
                        api.getPrivateClient().ptzSetHome(deviceId).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("PTZ set home failed", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PTZ_CREATE_PRESET: {
                    if (command instanceof StringType presetCmd) {
                        try {
                            // Format: "slot,name" e.g. "1,Front Door"
                            String[] parts = presetCmd.toString().split(",", 2);
                            if (parts.length == 2) {
                                int slot = Integer.parseInt(parts[0].trim());
                                String name = parts[1].trim();
                                api.getPrivateClient().ptzCreatePreset(deviceId, slot, name)
                                        .whenComplete((result, ex) -> {
                                            if (ex != null) {
                                                logger.debug("Failed to create PTZ preset", ex);
                                            }
                                        });
                            } else {
                                logger.debug("Invalid PTZ preset format. Expected 'slot,name', got: {}", presetCmd);
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Invalid PTZ preset slot number", e);
                        }
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PTZ_DELETE_PRESET: {
                    if (command instanceof DecimalType slotCmd) {
                        int slot = slotCmd.intValue();
                        api.getPrivateClient().ptzDeletePreset(deviceId, slot).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to delete PTZ preset", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_RECORDING_MODE: {
                    if (command instanceof StringType mode) {
                        api.getPrivateClient().setCameraRecordingMode(deviceId, mode.toString())
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set recording mode", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PERSON_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setPersonDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set person detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_VEHICLE_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setVehicleDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set vehicle detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_FACE_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setFaceDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set face detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setLicensePlateDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set license plate detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PACKAGE_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setPackageDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set package detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ANIMAL_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setAnimalDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set animal detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_DEVICE_REBOOT: {
                    if (command == OnOffType.ON) {
                        api.getPrivateClient().rebootDevice("camera", deviceId).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to reboot camera", ex);
                            }
                        });
                    }
                    break;
                }
                // Additional Private API Camera Controls
                case UnifiProtectBindingConstants.CHANNEL_MIC_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setCameraMicEnabled(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set microphone enabled", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_IR_MODE: {
                    if (command instanceof StringType mode) {
                        api.getPrivateClient().setCameraIRMode(deviceId, mode.toString()).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set IR mode", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_MOTION_DETECTION_ENABLED: {
                    if (command instanceof OnOffType enabled) {
                        api.getPrivateClient().setCameraMotionDetection(deviceId, enabled == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set motion detection", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_USE_GLOBAL_SETTINGS: {
                    if (command instanceof OnOffType useGlobal) {
                        api.getPrivateClient().setCameraUseGlobal(deviceId, useGlobal == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set use global settings", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_CAMERA_SPEAKER_VOLUME: {
                    if (command instanceof DecimalType volume) {
                        int vol = Math.max(0, Math.min(100, volume.intValue()));
                        api.getPrivateClient().setCameraSpeakerVolume(deviceId, vol).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set speaker volume", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_CAMERA_ZOOM_LEVEL: {
                    if (command instanceof DecimalType zoom) {
                        int zoomLevel = Math.max(0, Math.min(100, zoom.intValue()));
                        api.getPrivateClient().setCameraZoom(deviceId, zoomLevel).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set zoom level", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_CAMERA_WDR_LEVEL: {
                    if (command instanceof DecimalType wdr) {
                        int wdrLevel = Math.max(0, Math.min(3, wdr.intValue()));
                        api.getPrivateClient().setCameraWDR(deviceId, wdrLevel).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set WDR level", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_DOORBELL_RING_VOLUME: {
                    if (command instanceof DecimalType volume) {
                        int vol = Math.max(0, Math.min(100, volume.intValue()));
                        api.getPrivateClient().setDoorbellRingVolume(deviceId, vol).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set doorbell ring volume", ex);
                            }
                        });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_DOORBELL_CHIME_DURATION: {
                    Long duration = timeToMilliseconds(command);
                    if (duration != null) {
                        api.getPrivateClient().setDoorbellChimeDuration(deviceId, duration.intValue())
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set chime duration", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_HIGH_FPS_ENABLED: {
                    if (command instanceof OnOffType highFps) {
                        api.getPrivateClient()
                                .setCameraVideoMode(deviceId, highFps == OnOffType.ON ? "highFps" : "default")
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set high FPS mode", ex);
                                    }
                                });
                    }
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_HDR_ENABLED: {
                    if (command instanceof OnOffType hdr) {
                        api.getPrivateClient().setCameraHDR(deviceId, hdr == OnOffType.ON)
                                .whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set HDR", ex);
                                    }
                                });
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            logger.debug("Error handling command", e);
        }
    }

    @Override
    public void refreshFromDevice(CameraDevice camera) {
        super.refreshFromDevice(camera);
        addRemoveChannels();
        updateRtspsChannels(camera.privateDevice);
        updateFromPublicDevice(camera.publicDevice);
        updateFromPrivateDevice(camera.privateDevice);
    }

    protected void updateFromPublicDevice(Camera camera) {
        CameraFeatureFlags flags = camera.featureFlags;
        if (flags != null && flags.hasMic) {
            updateDimmerChannel(UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME, camera.micVolume);
        }

        OsdSettings osd = camera.osdSettings;
        if (osd != null) {
            updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_OSD_NAME, osd.isNameEnabled);
            updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_OSD_DATE, osd.isDateEnabled);
            updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_OSD_LOGO, osd.isLogoEnabled);
        }

        LedSettings led = camera.ledSettings;
        if (led != null) {
            updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_LED_ENABLED, led.isEnabled);
        }

        LcdMessage lcd = camera.lcdMessage;
        if (lcd != null && lcd.text != null) {
            updateStringChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE, lcd.text);
            updateTimeChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT, lcd.resetAt);
        }

        HdrType hdr = camera.hdrType;
        if (hdr != null) {
            updateApiValueChannel(UnifiProtectBindingConstants.CHANNEL_HDR_TYPE, hdr);
        }

        VideoMode videoMode = camera.videoMode;
        if (videoMode != null) {
            updateApiValueChannel(UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE, videoMode);
        }

        if (camera.activePatrolSlot != null) {
            updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, camera.activePatrolSlot);
        }

        // Update basic device info (available from Public API)
        if (camera.name != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_NAME, camera.name);
        }
        if (camera.state != null) {
            updateStringChannel(UnifiProtectBindingConstants.CHANNEL_DEVICE_STATE, camera.state.toString());
        }
    }

    /**
     * Internal method to update all Private API channels from camera data
     */
    protected void updateFromPrivateDevice(
            org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera privCamera) {
        logger.debug("Updating from private device: {}", privCamera);
        // Device status flags
        if (privCamera.isMotionDetected != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_MOTION_DETECTED,
                    OnOffType.from(privCamera.isMotionDetected));
        }
        if (privCamera.isSmartDetected != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_SMART_DETECTED,
                    OnOffType.from(privCamera.isSmartDetected));
        }
        if (privCamera.isRecording != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_RECORDING, OnOffType.from(privCamera.isRecording));
        }
        if (privCamera.isMicEnabled != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_MIC_ENABLED, OnOffType.from(privCamera.isMicEnabled));
        }
        if (privCamera.upSince != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_UPTIME_STARTED,
                    privCamera.upSince.toEpochMilli());
        }
        if (privCamera.connectedSince != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_CONNECTED_SINCE,
                    privCamera.connectedSince.toEpochMilli());
        }
        if (privCamera.lastSeen != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_LAST_SEEN, privCamera.lastSeen.toEpochMilli());
        }
        if (privCamera.lastRing != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_LAST_RING, privCamera.lastRing.toEpochMilli());
        }
        if (privCamera.marketName != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MODEL, privCamera.marketName);
        } else if (privCamera.type != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MODEL, privCamera.type);
        }
        if (privCamera.firmwareVersion != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_FIRMWARE_VERSION, privCamera.firmwareVersion);
        }
        if (privCamera.mac != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MAC_ADDRESS, privCamera.mac);
        }
        if (privCamera.host != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_IP_ADDRESS, privCamera.host);
        }
        if (privCamera.uptime != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_DEVICE_UPTIME,
                    new QuantityType<>(privCamera.uptime.toMillis(), Units.SECOND));
        }
        if (privCamera.lcdMessage != null && privCamera.lcdMessage.text != null) {
            updateStringChannel(UnifiProtectBindingConstants.CHANNEL_LCD_MESSAGE, privCamera.lcdMessage.text);
        }
        if (privCamera.lastRing != null) {
            long secondsSinceRing = Duration.between(privCamera.lastRing, Instant.now()).getSeconds();
            boolean isRinging = secondsSinceRing < 5; // Consider "ringing" if within last 5 seconds
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_RINGING, OnOffType.from(isRinging));
        }
        if (privCamera.isDark != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_DARK, OnOffType.from(privCamera.isDark));
        }
        if (privCamera.lastSmart != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_LAST_SMART, privCamera.lastSmart.toEpochMilli());
        }
        if (privCamera.isLiveHeatmapEnabled != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_LIVE_HEATMAP_ENABLED,
                    OnOffType.from(privCamera.isLiveHeatmapEnabled));
        }
        if (privCamera.videoReconfigurationInProgress != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS,
                    OnOffType.from(privCamera.videoReconfigurationInProgress));
        }
        if (privCamera.phyRate != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_PHY_RATE, new DecimalType(privCamera.phyRate));
        }
        if (privCamera.isProbingForWifi != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_PROBING_FOR_WIFI,
                    OnOffType.from(privCamera.isProbingForWifi));
        }
        if (privCamera.isPoorNetwork != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_POOR_NETWORK, OnOffType.from(privCamera.isPoorNetwork));
        }
        if (privCamera.isWirelessUplinkEnabled != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_IS_WIRELESS_UPLINK_ENABLED,
                    OnOffType.from(privCamera.isWirelessUplinkEnabled));
        }
        if (privCamera.voltage != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_VOLTAGE,
                    new org.openhab.core.library.types.QuantityType<>(privCamera.voltage, Units.VOLT));
        }
        if (privCamera.batteryStatus != null) {
            if (privCamera.batteryStatus.percentage != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_BATTERY_PERCENTAGE,
                        new org.openhab.core.library.types.QuantityType<>(privCamera.batteryStatus.percentage,
                                Units.PERCENT));
            }
            if (privCamera.batteryStatus.isCharging != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_BATTERY_IS_CHARGING,
                        OnOffType.from(privCamera.batteryStatus.isCharging));
            }
            if (privCamera.batteryStatus.sleepState != null) {
                updateStringChannel(UnifiProtectBindingConstants.CHANNEL_BATTERY_SLEEP_STATE,
                        privCamera.batteryStatus.sleepState);
            }
        }
        if (privCamera.isThirdPartyCamera != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_IS_THIRD_PARTY_CAMERA,
                    String.valueOf(privCamera.isThirdPartyCamera));
        }
        if (privCamera.platform != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_PLATFORM, privCamera.platform);
        }
        if (privCamera.hasSpeaker != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_HAS_SPEAKER, String.valueOf(privCamera.hasSpeaker));
        }
        if (privCamera.hasWifi != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_HAS_WIFI, String.valueOf(privCamera.hasWifi));
        }
        if (privCamera.hasBattery != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_HAS_BATTERY, String.valueOf(privCamera.hasBattery));
        }
        if (privCamera.stats != null && privCamera.stats.wifi != null) {
            if (privCamera.stats.wifi.channel != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_WIFI_CHANNEL,
                        new DecimalType(privCamera.stats.wifi.channel));
            }
            if (privCamera.stats.wifi.frequency != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_WIFI_FREQUENCY,
                        new DecimalType(privCamera.stats.wifi.frequency));
            }
            if (privCamera.stats.wifi.signalQuality != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_WIFI_SIGNAL_QUALITY,
                        new org.openhab.core.library.types.QuantityType<>(privCamera.stats.wifi.signalQuality,
                                Units.PERCENT));
            }
            if (privCamera.stats.wifi.signalStrength != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_WIFI_SIGNAL_STRENGTH,
                        new DecimalType(privCamera.stats.wifi.signalStrength));
            }
        }
        if (privCamera.stats != null && privCamera.stats.storage != null) {
            if (privCamera.stats.storage.used != null) {
                // Storage is in bytes - use DecimalType since channel is Number:DataAmount
                updateState(UnifiProtectBindingConstants.CHANNEL_STORAGE_USED,
                        new DecimalType(privCamera.stats.storage.used));
            }
            if (privCamera.stats.storage.rate != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_STORAGE_RATE,
                        new DecimalType(privCamera.stats.storage.getRatePerSecond()));
            }
        }
        if (privCamera.ispSettings != null && privCamera.ispSettings.wdr != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_CAMERA_WDR_LEVEL,
                    new DecimalType(privCamera.ispSettings.wdr));
        }

        // Recording settings
        if (privCamera.recordingSettings != null) {
            if (privCamera.recordingSettings.mode != null) {
                updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RECORDING_MODE,
                        privCamera.recordingSettings.mode.toString());
            }
            if (privCamera.recordingSettings.enableMotionDetection != null) {
                updateState(UnifiProtectBindingConstants.CHANNEL_MOTION_DETECTION_ENABLED,
                        OnOffType.from(privCamera.recordingSettings.enableMotionDetection));
            }
        }

        // Smart detection settings
        if (privCamera.smartDetectSettings != null && privCamera.smartDetectSettings.objectTypes != null) {
            List<org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType> types = privCamera.smartDetectSettings.objectTypes;
            updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PERSON_ENABLED, OnOffType.from(types.contains(
                    org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType.PERSON)));
            updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_VEHICLE_ENABLED,
                    OnOffType.from(types.contains(
                            org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType.VEHICLE)));
            updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_FACE_ENABLED, OnOffType.from(types.contains(
                    org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType.FACE)));
            updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED,
                    OnOffType.from(types.contains(
                            org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType.LICENSE_PLATE)));
            updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PACKAGE_ENABLED,
                    OnOffType.from(types.contains(
                            org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType.PACKAGE)));
            updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ANIMAL_ENABLED, OnOffType.from(types.contains(
                    org.openhab.binding.unifiprotect.internal.api.priv.dto.types.SmartDetectObjectType.ANIMAL)));
        }

        // IR Mode
        if (privCamera.ispSettings != null && privCamera.ispSettings.irLedMode != null) {
            updateStringChannel(UnifiProtectBindingConstants.CHANNEL_IR_MODE,
                    privCamera.ispSettings.irLedMode.toString());
        }

        // HDR enabled
        if (privCamera.hdrMode != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_HDR_ENABLED, OnOffType.from(privCamera.hdrMode));
        }

        // High FPS enabled
        if (privCamera.videoMode != null) {
            boolean isHighFps = privCamera.videoMode.toString().equalsIgnoreCase("highFps");
            updateState(UnifiProtectBindingConstants.CHANNEL_HIGH_FPS_ENABLED, OnOffType.from(isHighFps));
        }

        // Speaker settings
        if (privCamera.speakerSettings != null) {
            if (privCamera.speakerSettings.volume != null) {
                updateDimmerChannel(UnifiProtectBindingConstants.CHANNEL_CAMERA_SPEAKER_VOLUME,
                        privCamera.speakerSettings.volume);
            } else if (privCamera.speakerSettings.speakerVolume != null) {
                // Fallback to speakerVolume field if volume is null
                updateDimmerChannel(UnifiProtectBindingConstants.CHANNEL_CAMERA_SPEAKER_VOLUME,
                        privCamera.speakerSettings.speakerVolume);
            }
            if (privCamera.speakerSettings.ringVolume != null) {
                updateDimmerChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_RING_VOLUME,
                        privCamera.speakerSettings.ringVolume);
            }
        }

        // Chime duration
        if (privCamera.chimeDuration != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_DOORBELL_CHIME_DURATION,
                    new QuantityType<>(privCamera.chimeDuration, Units.SECOND));
        }

        // Zoom position
        if (privCamera.ispSettings != null && privCamera.ispSettings.zoomPosition != null) {
            updateDimmerChannel(UnifiProtectBindingConstants.CHANNEL_CAMERA_ZOOM_LEVEL,
                    privCamera.ispSettings.zoomPosition);
        }
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType eventType) {
        if (event.type == null) {
            return;
        }
        switch (event.type) {
            case CAMERA_MOTION:
                maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_MOTION_SNAPSHOT, Sequence.BEFORE);
                // Trigger motion on start and end if channel exists
                String motionChannelId = eventType == WSEventType.ADD
                        ? UnifiProtectBindingConstants.CHANNEL_MOTION_START
                        : UnifiProtectBindingConstants.CHANNEL_MOTION_UPDATE;
                if (hasChannel(motionChannelId)) {
                    triggerChannel(new ChannelUID(thing.getUID(), motionChannelId));
                    updateContactChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_CONTACT, OpenClosedType.OPEN);
                }
                maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_MOTION_SNAPSHOT, Sequence.AFTER);
                break;

            case SMART_AUDIO_DETECT:
                if (event instanceof CameraSmartDetectAudioEvent e) {
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT,
                            Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_CONTACT,
                                OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT,
                            Sequence.AFTER);
                }
                break;

            case SMART_DETECT_ZONE:
                if (event instanceof CameraSmartDetectZoneEvent e) {
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT,
                            Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_CONTACT,
                                OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT,
                            Sequence.AFTER);
                }
                break;

            case SMART_DETECT_LINE:
                if (event instanceof CameraSmartDetectLineEvent e) {
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT,
                            Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_CONTACT,
                                OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT,
                            Sequence.AFTER);
                }
                break;

            case SMART_DETECT_LOITER_ZONE:
                if (event instanceof CameraSmartDetectLoiterEvent e) {
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT,
                            Sequence.BEFORE);
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateContactChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_CONTACT,
                                OpenClosedType.OPEN);
                    }
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT,
                            Sequence.AFTER);
                }
                break;
            case RING:
                if (event instanceof RingEvent && hasChannel(UnifiProtectBindingConstants.CHANNEL_RING)) {
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT, Sequence.BEFORE);
                    triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_RING),
                            event.end == null ? "PRESSED" : "RELEASED");
                    updateContactChannel(UnifiProtectBindingConstants.CHANNEL_RING_CONTACT, OpenClosedType.OPEN);
                    maybeUpdateSnapshot(UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT, Sequence.AFTER);
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

    private void updateRtspsChannels(org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Camera privCamera) {
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
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_HIGH, rtspUrlHigh);
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_MEDIUM, rtspUrlMedium);
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_LOW, rtspUrlLow);
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_PACKAGE, null); // Package URL not in channels

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
                    String webRTCId = UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL + "-" + type;
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
        CameraDevice camera = device;
        if (camera == null || camera.privateDevice.featureFlags == null || camera.publicDevice.featureFlags == null) {
            logger.debug("Camera or feature flags are null, skipping channel addition");
            return;
        }
        List<Channel> channelAdd = new ArrayList<>();

        // active channel ids set accumulates all channels that should exist after this call
        Set<String> activeChannelIds = new HashSet<>();

        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_HIGH, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_HIGH_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_MEDIUM, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_MEDIUM_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_LOW, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_LOW_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_PACKAGE, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_PACKAGE_LABEL);

        addChannel(UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, CoreItemFactory.IMAGE,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_LABEL);

        addChannel(UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL_LABEL);

        String snapshotUrl = media.getImageBasePath() + "/" + baseSourceId;
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL, snapshotUrl);
        getThing().setProperty("snapshot-url", snapshotUrl);

        if (enableWebRTC) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_HIGH, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_HIGH_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_MEDIUM, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_MEDIUM_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_LOW, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_LOW_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_PACKAGE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_PACKAGE_LABEL);
        }

        CameraFeatureFlags flags = Objects.requireNonNull(camera.publicDevice.featureFlags,
                "Feature flags are required");
        if (flags.hasMic) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME, CoreItemFactory.DIMMER,
                    UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME, channelAdd, activeChannelIds);
        }
        if (flags.hasLedStatus) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_LED_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_LED_ENABLED, channelAdd, activeChannelIds);
        }
        if (flags.hasHdr) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_HDR_TYPE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_HDR_TYPE, channelAdd, activeChannelIds);
        }
        if (flags.videoModes != null && !flags.videoModes.isEmpty()) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE, channelAdd, activeChannelIds);
        }
        addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_START,
                UnifiProtectBindingConstants.CHANNEL_MOTION, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_MOTION_START_LABEL);
        addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_UPDATE,
                UnifiProtectBindingConstants.CHANNEL_MOTION, channelAdd, activeChannelIds,
                UnifiProtectBindingConstants.CHANNEL_MOTION_UPDATE_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_CONTACT, CoreItemFactory.CONTACT,
                UnifiProtectBindingConstants.CHANNEL_MOTION_CONTACT, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_SNAPSHOT, CoreItemFactory.IMAGE,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_DETECTION_ENABLED, CoreItemFactory.SWITCH,
                UnifiProtectBindingConstants.CHANNEL_MOTION_DETECTION_ENABLED, channelAdd, activeChannelIds);

        addChannel(UnifiProtectBindingConstants.CHANNEL_IS_MOTION_DETECTED, CoreItemFactory.SWITCH,
                UnifiProtectBindingConstants.CHANNEL_IS_MOTION_DETECTED, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_IS_SMART_DETECTED, CoreItemFactory.SWITCH,
                UnifiProtectBindingConstants.CHANNEL_IS_SMART_DETECTED, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_IS_RECORDING, CoreItemFactory.SWITCH,
                UnifiProtectBindingConstants.CHANNEL_IS_RECORDING, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_DEVICE_STATE, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_DEVICE_STATE, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_DEVICE_UPTIME, "Number:Time",
                UnifiProtectBindingConstants.CHANNEL_DEVICE_UPTIME, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_UPTIME_STARTED, "DateTime",
                UnifiProtectBindingConstants.CHANNEL_UPTIME_STARTED, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_CONNECTED_SINCE, "DateTime",
                UnifiProtectBindingConstants.CHANNEL_CONNECTED_SINCE, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_LAST_SEEN, "DateTime",
                UnifiProtectBindingConstants.CHANNEL_LAST_SEEN, channelAdd, activeChannelIds);

        addChannel(UnifiProtectBindingConstants.CHANNEL_USE_GLOBAL_SETTINGS, CoreItemFactory.SWITCH,
                UnifiProtectBindingConstants.CHANNEL_USE_GLOBAL_SETTINGS, channelAdd, activeChannelIds);

        // Recording Mode
        addChannel(UnifiProtectBindingConstants.CHANNEL_RECORDING_MODE, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RECORDING_MODE, channelAdd, activeChannelIds);

        if (flags.smartDetectTypes != null && !flags.smartDetectTypes.isEmpty()) {
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_START,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_START_LABEL);
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_UPDATE,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_UPDATE_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_CONTACT, CoreItemFactory.CONTACT,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_CONTACT, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT, CoreItemFactory.IMAGE,
                    UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT_LABEL);

            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_START,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_START_LABEL);
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_UPDATE,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_UPDATE_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_CONTACT, CoreItemFactory.CONTACT,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_CONTACT, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT, CoreItemFactory.IMAGE,
                    UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT_LABEL);

            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_START,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_START_LABEL);
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_UPDATE,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_UPDATE_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_CONTACT, CoreItemFactory.CONTACT,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_CONTACT, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT, CoreItemFactory.IMAGE,
                    UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT_LABEL);
        }
        if (flags.smartDetectAudioTypes != null && !flags.smartDetectAudioTypes.isEmpty()) {
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_START,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_START_LABEL);
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_UPDATE,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_UPDATE_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_CONTACT, CoreItemFactory.CONTACT,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_CONTACT, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT, CoreItemFactory.IMAGE,
                    UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT_LABEL);
        }
        if (camera.publicDevice.osdSettings != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_OSD_NAME, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_OSD_NAME, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_OSD_DATE, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_OSD_DATE, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_OSD_LOGO, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_OSD_LOGO, channelAdd, activeChannelIds);
        }
        if (camera.publicDevice.activePatrolSlot != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, channelAdd, activeChannelIds);
        }

        if (camera.privateDevice.isDoorbell()) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_RING_VOLUME, CoreItemFactory.DIMMER,
                    UnifiProtectBindingConstants.CHANNEL_DOORBELL_RING_VOLUME, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_CHIME_DURATION, "Number:Time",
                    UnifiProtectBindingConstants.CHANNEL_DOORBELL_CHIME_DURATION, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_LCD_MESSAGE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_LCD_MESSAGE, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_LAST_RING, "DateTime",
                    UnifiProtectBindingConstants.CHANNEL_LAST_RING, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_IS_RINGING, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_IS_RINGING, channelAdd, activeChannelIds);

            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_RING, UnifiProtectBindingConstants.CHANNEL_RING,
                    channelAdd, activeChannelIds, UnifiProtectBindingConstants.CHANNEL_RING_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_RING_CONTACT, CoreItemFactory.CONTACT,
                    UnifiProtectBindingConstants.CHANNEL_RING_CONTACT, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT, CoreItemFactory.IMAGE,
                    UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE, channelAdd, activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT, "Number:Time",
                    UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT, channelAdd,
                    activeChannelIds,
                    UnifiProtectBindingConstants.CHANNEL_DOORBELL_DEFAULT_MESSAGE_RESET_TIMEOUT_LABEL);
        }

        if (camera.privateDevice.featureFlags.hasPtz != null && camera.privateDevice.featureFlags.hasPtz) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_PAN, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_PAN, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_TILT, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_TILT, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_ZOOM, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_RELATIVE_ZOOM, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_CENTER, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_CENTER, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_SET_HOME, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_SET_HOME, channelAdd, activeChannelIds);

            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_CREATE_PRESET, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_CREATE_PRESET, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_PTZ_DELETE_PRESET, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_PTZ_DELETE_PRESET, channelAdd, activeChannelIds);
        }

        // Smart Detection Controls
        if (flags.smartDetectTypes != null && !flags.smartDetectTypes.isEmpty()) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PERSON_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PERSON_ENABLED, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_VEHICLE_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_VEHICLE_ENABLED, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_FACE_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_FACE_ENABLED, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LICENSE_PLATE_ENABLED, channelAdd,
                    activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PACKAGE_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_PACKAGE_ENABLED, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ANIMAL_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ANIMAL_ENABLED, channelAdd, activeChannelIds);
        }

        if (flags.hasMic) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_MIC_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_MIC_ENABLED, channelAdd, activeChannelIds);
        }

        if (camera.privateDevice.featureFlags.hasLedIr != null && camera.privateDevice.featureFlags.hasLedIr) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_IR_MODE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_IR_MODE, channelAdd, activeChannelIds);
        }

        if (flags.hasHdr) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_HDR_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_HDR_ENABLED, channelAdd, activeChannelIds);
        }
        if (flags.videoModes != null && !flags.videoModes.isEmpty()) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_HIGH_FPS_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_HIGH_FPS_ENABLED, channelAdd, activeChannelIds);
        }

        if (flags.hasSpeaker) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_CAMERA_SPEAKER_VOLUME, CoreItemFactory.DIMMER,
                    UnifiProtectBindingConstants.CHANNEL_CAMERA_SPEAKER_VOLUME, channelAdd, activeChannelIds);
        }

        if (camera.privateDevice.featureFlags.canOpticalZoom != null
                && camera.privateDevice.featureFlags.canOpticalZoom) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_CAMERA_ZOOM_LEVEL, CoreItemFactory.DIMMER,
                    UnifiProtectBindingConstants.CHANNEL_CAMERA_ZOOM_LEVEL, channelAdd, activeChannelIds);
        }
        if (camera.privateDevice.ispSettings != null && camera.privateDevice.ispSettings.wdr != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_CAMERA_WDR_LEVEL, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_CAMERA_WDR_LEVEL, channelAdd, activeChannelIds);
        }

        if (camera.privateDevice.isDark != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_IS_DARK, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_IS_DARK, channelAdd, activeChannelIds);
        }
        if (camera.privateDevice.lastSmart != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_LAST_SMART, "DateTime",
                    UnifiProtectBindingConstants.CHANNEL_LAST_SMART, channelAdd, activeChannelIds);
        }
        if (camera.privateDevice.isLiveHeatmapEnabled != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_IS_LIVE_HEATMAP_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_IS_LIVE_HEATMAP_ENABLED, channelAdd, activeChannelIds);
        }
        if (camera.privateDevice.videoReconfigurationInProgress != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_VIDEO_RECONFIGURATION_IN_PROGRESS, channelAdd,
                    activeChannelIds);
        }

        if (camera.privateDevice.hasWifi != null && camera.privateDevice.hasWifi) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_PHY_RATE, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_PHY_RATE, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_IS_PROBING_FOR_WIFI, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_IS_PROBING_FOR_WIFI, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_IS_POOR_NETWORK, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_IS_POOR_NETWORK, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_IS_WIRELESS_UPLINK_ENABLED, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_IS_WIRELESS_UPLINK_ENABLED, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WIFI_CHANNEL, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_WIFI_CHANNEL, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WIFI_FREQUENCY, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_WIFI_FREQUENCY, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WIFI_SIGNAL_QUALITY, "Number:Dimensionless",
                    UnifiProtectBindingConstants.CHANNEL_WIFI_SIGNAL_QUALITY, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WIFI_SIGNAL_STRENGTH, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_WIFI_SIGNAL_STRENGTH, channelAdd, activeChannelIds);

        }

        if (camera.privateDevice.hasBattery != null && camera.privateDevice.hasBattery) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_VOLTAGE, "Number:ElectricPotential",
                    UnifiProtectBindingConstants.CHANNEL_VOLTAGE, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_BATTERY_PERCENTAGE, "Number:Dimensionless",
                    UnifiProtectBindingConstants.CHANNEL_BATTERY_PERCENTAGE, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_BATTERY_IS_CHARGING, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_BATTERY_IS_CHARGING, channelAdd, activeChannelIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_BATTERY_SLEEP_STATE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_BATTERY_SLEEP_STATE, channelAdd, activeChannelIds);
        }

        // Storage Stats
        addChannel(UnifiProtectBindingConstants.CHANNEL_STORAGE_USED, "Number:DataAmount",
                UnifiProtectBindingConstants.CHANNEL_STORAGE_USED, channelAdd, activeChannelIds);
        addChannel(UnifiProtectBindingConstants.CHANNEL_STORAGE_RATE, CoreItemFactory.NUMBER,
                UnifiProtectBindingConstants.CHANNEL_STORAGE_RATE, channelAdd, activeChannelIds);

        // Device Reboot
        addChannel(UnifiProtectBindingConstants.CHANNEL_DEVICE_REBOOT, CoreItemFactory.SWITCH,
                UnifiProtectBindingConstants.CHANNEL_DEVICE_REBOOT, channelAdd, activeChannelIds);

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
        // if (thing.getChannel(uid) == null) {
        ChannelBuilder builder = ChannelBuilder.create(uid, itemType)
                .withType(new ChannelTypeUID(UnifiProtectBindingConstants.BINDING_ID, channelTypeId));
        if (label != null) {
            builder.withLabel(translationService.getTranslation(label));
        }
        Channel ch = builder.build();
        channelAdd.add(ch);
        // }
    }

    private void addTriggerChannel(String channelId, String channelTypeId, List<Channel> channelAdd,
            Set<String> activeChannelIds, @Nullable String label) {
        ChannelUID uid = new ChannelUID(thing.getUID(), channelId);
        activeChannelIds.add(channelId);
        if (thing.getChannel(uid) == null) {
            ChannelBuilder cb = ChannelBuilder.create(uid, null)
                    .withType(new ChannelTypeUID(UnifiProtectBindingConstants.BINDING_ID, channelTypeId))
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
