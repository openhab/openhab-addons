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
package org.openhab.binding.unifiprotect.internal.handler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.api.UniFiProtectApiClient;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectCameraConfiguration;
import org.openhab.binding.unifiprotect.internal.dto.ApiValueEnum;
import org.openhab.binding.unifiprotect.internal.dto.Camera;
import org.openhab.binding.unifiprotect.internal.dto.CameraFeatureFlags;
import org.openhab.binding.unifiprotect.internal.dto.ChannelQuality;
import org.openhab.binding.unifiprotect.internal.dto.HdrType;
import org.openhab.binding.unifiprotect.internal.dto.LedSettings;
import org.openhab.binding.unifiprotect.internal.dto.ObjectType;
import org.openhab.binding.unifiprotect.internal.dto.OsdSettings;
import org.openhab.binding.unifiprotect.internal.dto.RtspsStreams;
import org.openhab.binding.unifiprotect.internal.dto.TalkbackSession;
import org.openhab.binding.unifiprotect.internal.dto.VideoMode;
import org.openhab.binding.unifiprotect.internal.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectAudioEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectLineEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectLoiterEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.CameraSmartDetectZoneEvent;
import org.openhab.binding.unifiprotect.internal.dto.events.RingEvent;
import org.openhab.binding.unifiprotect.internal.media.UnifiMediaService;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Child handler for a UniFi Protect Camera.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectCameraHandler extends UnifiProtectAbstractDeviceHandler<Camera> {
    private final Logger logger = LoggerFactory.getLogger(UnifiProtectCameraHandler.class);

    private final UnifiMediaService media;
    private boolean enableWebRTC = true;
    private final String baseSourceId;

    public UnifiProtectCameraHandler(Thing thing, UnifiMediaService media) {
        super(thing);
        this.media = media;
        this.baseSourceId = thing.getUID().getAsString();
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
            Camera cam = device;
            if (cam == null) {
                return;
            }

            switch (id) {
                case UnifiProtectBindingConstants.CHANNEL_SNAPSHOT:
                    updateSnapshot(id);
                    return;
                default:
                    refreshState(id);
                    return;
            }
        }

        UniFiProtectApiClient api = getApiClient();
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
                        break;
                    }
                    volume = Math.max(0, Math.min(100, volume));
                    JsonObject patch = UniFiProtectApiClient.buildPatch("micVolume", volume);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_HDR_TYPE: {
                    String value = command.toString();
                    JsonObject patch = UniFiProtectApiClient.buildPatch("hdrType", value);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE: {
                    String value = command.toString();
                    JsonObject patch = UniFiProtectApiClient.buildPatch("videoMode", value);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_OSD_NAME: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectApiClient.buildPatch("osdSettings.isNameEnabled", value);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_OSD_DATE: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectApiClient.buildPatch("osdSettings.isDateEnabled", value);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_OSD_LOGO: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectApiClient.buildPatch("osdSettings.isLogoEnabled", value);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_LED_ENABLED: {
                    boolean value = OnOffType.ON.equals(command);
                    JsonObject patch = UniFiProtectApiClient.buildPatch("ledSettings.isEnabled", value);
                    Camera updated = api.patchCamera(deviceId, patch);
                    updateFromDevice(updated);
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
                        api.ptzPatrolStop(deviceId);
                        updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, 0);
                    } else {
                        api.ptzPatrolStart(deviceId, String.valueOf(slot));
                        updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, slot);
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
    public void updateFromDevice(Camera camera) {
        super.updateFromDevice(camera);

        addRemoveChannels(camera);
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME, camera.micVolume);

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

        HdrType hdr = camera.hdrType;
        if (hdr != null) {
            updateApiValueChannel(UnifiProtectBindingConstants.CHANNEL_HDR_TYPE, hdr);
        }

        VideoMode videoMode = camera.videoMode;
        if (videoMode != null) {
            updateApiValueChannel(UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE, videoMode);
        }

        updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, camera.activePatrolSlot);
        updateRtspsChannels();
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType eventType) {
        if (event.type == null) {
            return;
        }

        switch (event.type) {
            case CAMERA_MOTION:
                if (eventType == WSEventType.ADD) {
                    updateSnapshot(UnifiProtectBindingConstants.CHANNEL_MOTION_SNAPSHOT);
                }
                // Trigger motion on start and end if channel exists
                if (hasChannel(UnifiProtectBindingConstants.CHANNEL_MOTION)) {
                    String channelId = eventType == WSEventType.ADD ? UnifiProtectBindingConstants.CHANNEL_MOTION_START
                            : UnifiProtectBindingConstants.CHANNEL_MOTION_UPDATE;
                    triggerChannel(new ChannelUID(thing.getUID(), channelId));
                    updateState(UnifiProtectBindingConstants.CHANNEL_MOTION_CONTACT,
                            eventType == WSEventType.ADD ? OnOffType.OFF : OnOffType.ON);
                }
                break;

            case SMART_AUDIO_DETECT:
                if (event instanceof CameraSmartDetectAudioEvent e) {
                    if (eventType == WSEventType.ADD) {
                        updateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT);
                    }
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_CONTACT,
                                eventType == WSEventType.ADD ? OnOffType.OFF : OnOffType.ON);
                    }
                }
                break;

            case SMART_DETECT_ZONE:
                if (event instanceof CameraSmartDetectZoneEvent e) {
                    if (eventType == WSEventType.ADD) {
                        updateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT);
                    }
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_CONTACT,
                                eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    }
                }
                break;

            case SMART_DETECT_LINE:
                if (event instanceof CameraSmartDetectLineEvent e) {
                    if (eventType == WSEventType.ADD) {
                        updateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT);
                    }
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_CONTACT,
                                eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    }
                }
                break;

            case SMART_DETECT_LOITER_ZONE:
                if (event instanceof CameraSmartDetectLoiterEvent e) {
                    if (eventType == WSEventType.ADD) {
                        updateSnapshot(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT);
                    }
                    String channelId = eventType == WSEventType.ADD
                            ? UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_START
                            : UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_UPDATE;
                    if (hasChannel(channelId)) {
                        triggerChannel(channelId, e.smartDetectTypes);
                        updateState(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_CONTACT,
                                eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                    }
                }
                break;
            case RING:
                if (event instanceof RingEvent && hasChannel(UnifiProtectBindingConstants.CHANNEL_RING)) {
                    updateSnapshot(UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT);
                    triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_RING),
                            event.end == null ? "PRESSED" : "RELEASED");
                    updateState(UnifiProtectBindingConstants.CHANNEL_RING_CONTACT,
                            eventType == WSEventType.ADD ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
                break;

            default:
                // ignore other event types in camera handler
                break;
        }
    }

    public TalkbackSession startTalkback() throws IOException {
        UniFiProtectApiClient api = getApiClient();
        if (api == null) {
            throw new IOException("API client is null");
        }
        return api.createTalkbackSession(deviceId);
    }

    public byte[] getSnapshot(boolean highQuality) throws IOException {
        UniFiProtectApiClient api = getApiClient();
        if (api == null) {
            throw new IOException("API client is null");
        }
        return api.getSnapshot(deviceId, highQuality);
    }

    private void updateRtspsChannels() {
        UniFiProtectApiClient api = getApiClient();
        if (api == null) {
            return;
        }
        RtspsStreams rtsps = null;

        // Create RTSP streams if WebRTC is enabled
        if (enableWebRTC) {
            try {
                rtsps = api.createRtspsStream(deviceId,
                        List.of(ChannelQuality.HIGH, ChannelQuality.MEDIUM, ChannelQuality.LOW));
            } catch (IOException e) {
                logger.debug("Failed to create RTSP streams", e);
            }
        }
        // update existing channels
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_HIGH, rtsps != null ? rtsps.high : null);
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_MEDIUM, rtsps != null ? rtsps.medium : null);
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_LOW, rtsps != null ? rtsps.low : null);
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_PACKAGE,
                rtsps != null ? rtsps.packageUrl : null);

        getThing().setProperty("stream-publish-url", null);

        // Register new streams if available
        if (enableWebRTC && rtsps != null) {
            URI bc = null;
            try {
                TalkbackSession talkback = startTalkback();
                bc = URI.create(talkback.url);
            } catch (IOException e) {
                logger.debug("Talkback not stupported: {}", e.getMessage());
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

    private void addRemoveChannels(Camera camera) {
        List<Channel> channelRemove = new ArrayList<>();
        for (Channel existing : thing.getChannels()) {
            channelRemove.add(existing);
        }
        updateThing(editThing().withoutChannels(channelRemove).build());
        List<Channel> channelAdd = new ArrayList<>();

        // Desired set accumulates all channels that should exist after this call
        Set<String> desiredIds = new HashSet<>();

        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_HIGH, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, desiredIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_HIGH_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_MEDIUM, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, desiredIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_MEDIUM_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_LOW, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, desiredIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_LOW_LABEL);
        addChannel(UnifiProtectBindingConstants.CHANNEL_RTSP_URL_PACKAGE, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL, channelAdd, desiredIds,
                UnifiProtectBindingConstants.CHANNEL_RTSP_URL_PACKAGE_LABEL);

        addChannel(UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, CoreItemFactory.IMAGE,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_LABEL);

        addChannel(UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL, CoreItemFactory.STRING,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL, channelAdd, desiredIds,
                UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL_LABEL);

        String snapshotUrl = media.getImageBasePath() + "/" + baseSourceId;
        updateStringChannel(UnifiProtectBindingConstants.CHANNEL_SNAPSHOT_URL, snapshotUrl);
        getThing().setProperty("snapshot-url", snapshotUrl);

        if (enableWebRTC) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_HIGH, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, desiredIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_HIGH_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_MEDIUM, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, desiredIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_MEDIUM_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_LOW, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, desiredIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_LOW_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_PACKAGE, CoreItemFactory.STRING,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL, channelAdd, desiredIds,
                    UnifiProtectBindingConstants.CHANNEL_WEBRTC_URL_PACKAGE_LABEL);
        }

        CameraFeatureFlags flags = camera.featureFlags;
        if (flags != null) {
            if (flags.hasMic) {
                addChannel(UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME, CoreItemFactory.NUMBER,
                        UnifiProtectBindingConstants.CHANNEL_MIC_VOLUME, channelAdd, desiredIds);
            }
            if (flags.hasLedStatus) {
                addChannel(UnifiProtectBindingConstants.CHANNEL_LED_ENABLED, CoreItemFactory.SWITCH,
                        UnifiProtectBindingConstants.CHANNEL_LED_ENABLED, channelAdd, desiredIds);
            }
            if (flags.hasHdr) {
                addChannel(UnifiProtectBindingConstants.CHANNEL_HDR_TYPE, CoreItemFactory.STRING,
                        UnifiProtectBindingConstants.CHANNEL_HDR_TYPE, channelAdd, desiredIds);
            }
            if (flags.videoModes != null && !flags.videoModes.isEmpty()) {
                addChannel(UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE, CoreItemFactory.STRING,
                        UnifiProtectBindingConstants.CHANNEL_VIDEO_MODE, channelAdd, desiredIds);
            }
            if (flags.smartDetectTypes != null && !flags.smartDetectTypes.isEmpty()) {
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_START,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_START_LABEL);
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_UPDATE,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_UPDATE_LABEL);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_CONTACT, CoreItemFactory.CONTACT,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_CONTACT, channelAdd, desiredIds);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT, CoreItemFactory.IMAGE,
                        UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_ZONE_SNAPSHOT_LABEL);

                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_START,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_START_LABEL);
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_UPDATE,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_UPDATE_LABEL);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_CONTACT, CoreItemFactory.CONTACT,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_CONTACT, channelAdd, desiredIds);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT, CoreItemFactory.IMAGE,
                        UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LINE_SNAPSHOT_LABEL);

                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_START,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_START_LABEL);
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_UPDATE,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_UPDATE_LABEL);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_CONTACT, CoreItemFactory.CONTACT,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_CONTACT, channelAdd, desiredIds);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT, CoreItemFactory.IMAGE,
                        UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_LOITER_SNAPSHOT_LABEL);
            } else {
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_START,
                        UnifiProtectBindingConstants.CHANNEL_MOTION, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_MOTION_START_LABEL);
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_UPDATE,
                        UnifiProtectBindingConstants.CHANNEL_MOTION, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_MOTION_UPDATE_LABEL);
                addChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_CONTACT, CoreItemFactory.CONTACT,
                        UnifiProtectBindingConstants.CHANNEL_MOTION_CONTACT, channelAdd, desiredIds);
                addChannel(UnifiProtectBindingConstants.CHANNEL_MOTION_SNAPSHOT, CoreItemFactory.IMAGE,
                        UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds);
            }
            if (flags.smartDetectAudioTypes != null && !flags.smartDetectAudioTypes.isEmpty()) {
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_START,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_START_LABEL);
                addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_UPDATE,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_UPDATE_LABEL);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_CONTACT, CoreItemFactory.CONTACT,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_CONTACT, channelAdd, desiredIds);
                addChannel(UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT, CoreItemFactory.IMAGE,
                        UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds,
                        UnifiProtectBindingConstants.CHANNEL_SMART_DETECT_AUDIO_SNAPSHOT_LABEL);
            }
        }

        if (camera.osdSettings != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_OSD_NAME, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_OSD_NAME, channelAdd, desiredIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_OSD_DATE, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_OSD_DATE, channelAdd, desiredIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_OSD_LOGO, CoreItemFactory.SWITCH,
                    UnifiProtectBindingConstants.CHANNEL_OSD_LOGO, channelAdd, desiredIds);
        }

        if (camera.activePatrolSlot != null) {
            addChannel(UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, CoreItemFactory.NUMBER,
                    UnifiProtectBindingConstants.CHANNEL_ACTIVE_PATROL_SLOT, channelAdd, desiredIds);
        }

        if (camera.lcdMessage != null || camera.featureFlags.smartDetectTypes.contains(ObjectType.PACKAGE)) {
            addTriggerChannel(UnifiProtectBindingConstants.CHANNEL_RING, UnifiProtectBindingConstants.CHANNEL_RING,
                    channelAdd, desiredIds, UnifiProtectBindingConstants.CHANNEL_RING_LABEL);
            addChannel(UnifiProtectBindingConstants.CHANNEL_RING_CONTACT, CoreItemFactory.CONTACT,
                    UnifiProtectBindingConstants.CHANNEL_RING_CONTACT, channelAdd, desiredIds);
            addChannel(UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT, CoreItemFactory.IMAGE,
                    UnifiProtectBindingConstants.CHANNEL_SNAPSHOT, channelAdd, desiredIds,
                    UnifiProtectBindingConstants.CHANNEL_RING_SNAPSHOT_LABEL);
        }

        updateThing(editThing().withChannels(channelAdd).build());
    }

    private void addChannel(String channelId, String itemType, String channelTypeId, List<Channel> channelAdd,
            Set<String> desiredIds) {
        addChannel(channelId, itemType, channelTypeId, channelAdd, desiredIds, null);
    }

    private void addChannel(String channelId, String itemType, String channelTypeId, List<Channel> channelAdd,
            Set<String> desiredIds, @Nullable String label) {
        ChannelUID uid = new ChannelUID(thing.getUID(), channelId);
        desiredIds.add(channelId);
        if (thing.getChannel(uid) == null) {
            ChannelBuilder builder = ChannelBuilder.create(uid, itemType)
                    .withType(new ChannelTypeUID(UnifiProtectBindingConstants.BINDING_ID, channelTypeId));
            if (label != null) {
                builder.withLabel(label);
            }
            Channel ch = builder.build();
            channelAdd.add(ch);
        }
    }

    private void addTriggerChannel(String channelId, String channelTypeId, List<Channel> channelAdd,
            Set<String> desiredIds, @Nullable String label) {
        ChannelUID uid = new ChannelUID(thing.getUID(), channelId);
        desiredIds.add(channelId);
        if (thing.getChannel(uid) == null) {
            ChannelBuilder cb = ChannelBuilder.create(uid, null)
                    .withType(new ChannelTypeUID(UnifiProtectBindingConstants.BINDING_ID, channelTypeId))
                    .withKind(ChannelKind.TRIGGER);
            if (label != null) {
                cb.withLabel(label);
            }
            channelAdd.add(cb.build());
        }
    }

    private void updateSnapshot(String channelId) {
        if (hasChannel(channelId) && isLinked(channelId)) {
            UniFiProtectApiClient client = getApiClient();
            if (client != null) {
                try {
                    updateState(channelId, new RawType(client.getSnapshot(deviceId, true), "image/jpeg"));
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
