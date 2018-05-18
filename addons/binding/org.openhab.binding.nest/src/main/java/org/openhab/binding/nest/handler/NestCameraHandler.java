/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.Camera.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all the updates to the camera as well as handling the commands that send
 * updates to Nest.
 *
 * @author David Bennett - initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class NestCameraHandler extends NestBaseHandler<Camera> {
    private final Logger logger = LoggerFactory.getLogger(NestCameraHandler.class);

    public NestCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, Camera camera) {
        if (channelUID.getId().startsWith(CHANNEL_GROUP_CAMERA_PREFIX)) {
            return getCameraChannelState(channelUID, camera);
        } else if (channelUID.getId().startsWith(CHANNEL_GROUP_LAST_EVENT_PREFIX)) {
            return getLastEventChannelState(channelUID, camera);
        } else {
            logger.error("Unsupported channelId '{}'", channelUID.getId());
            return UnDefType.UNDEF;
        }
    }

    protected State getCameraChannelState(ChannelUID channelUID, Camera camera) {
        switch (channelUID.getId()) {
            case CHANNEL_CAMERA_APP_URL:
                return getAsStringTypeOrNull(camera.getAppUrl());
            case CHANNEL_CAMERA_AUDIO_INPUT_ENABLED:
                return getAsOnOffTypeOrNull(camera.isAudioInputEnabled());
            case CHANNEL_CAMERA_LAST_ONLINE_CHANGE:
                return getAsDateTimeTypeOrNull(camera.getLastIsOnlineChange());
            case CHANNEL_CAMERA_PUBLIC_SHARE_ENABLED:
                return getAsOnOffTypeOrNull(camera.isPublicShareEnabled());
            case CHANNEL_CAMERA_PUBLIC_SHARE_URL:
                return getAsStringTypeOrNull(camera.getPublicShareUrl());
            case CHANNEL_CAMERA_SNAPSHOT_URL:
                return getAsStringTypeOrNull(camera.getSnapshotUrl());
            case CHANNEL_CAMERA_STREAMING:
                return getAsOnOffTypeOrNull(camera.isStreaming());
            case CHANNEL_CAMERA_VIDEO_HISTORY_ENABLED:
                return getAsOnOffTypeOrNull(camera.isVideoHistoryEnabled());
            case CHANNEL_CAMERA_WEB_URL:
                return getAsStringTypeOrNull(camera.getWebUrl());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    protected State getLastEventChannelState(ChannelUID channelUID, Camera camera) {
        Event lastEvent = camera.getLastEvent();
        if (lastEvent == null) {
            return UnDefType.NULL;
        }

        switch (channelUID.getId()) {
            case CHANNEL_LAST_EVENT_ACTIVITY_ZONES:
                return getAsStringTypeListOrNull(lastEvent.getActivityZones());
            case CHANNEL_LAST_EVENT_ANIMATED_IMAGE_URL:
                return getAsStringTypeOrNull(lastEvent.getAnimatedImageUrl());
            case CHANNEL_LAST_EVENT_APP_URL:
                return getAsStringTypeOrNull(lastEvent.getAppUrl());
            case CHANNEL_LAST_EVENT_END_TIME:
                return getAsDateTimeTypeOrNull(lastEvent.getEndTime());
            case CHANNEL_LAST_EVENT_HAS_MOTION:
                return getAsOnOffTypeOrNull(lastEvent.isHasMotion());
            case CHANNEL_LAST_EVENT_HAS_PERSON:
                return getAsOnOffTypeOrNull(lastEvent.isHasPerson());
            case CHANNEL_LAST_EVENT_HAS_SOUND:
                return getAsOnOffTypeOrNull(lastEvent.isHasSound());
            case CHANNEL_LAST_EVENT_IMAGE_URL:
                return getAsStringTypeOrNull(lastEvent.getImageUrl());
            case CHANNEL_LAST_EVENT_START_TIME:
                return getAsDateTimeTypeOrNull(lastEvent.getStartTime());
            case CHANNEL_LAST_EVENT_URLS_EXPIRE_TIME:
                return getAsDateTimeTypeOrNull(lastEvent.getUrlsExpireTime());
            case CHANNEL_LAST_EVENT_WEB_URL:
                return getAsStringTypeOrNull(lastEvent.getWebUrl());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            Camera lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        } else if (CHANNEL_CAMERA_STREAMING.equals(channelUID.getId())) {
            // Change the mode.
            if (command instanceof OnOffType) {
                // Set the mode to be the cmd value.
                addUpdateRequest("is_streaming", command == OnOffType.ON);
            }
        }
    }

    @Override
    public void onNewNestCameraData(Camera camera) {
        if (isNotHandling(camera)) {
            logger.debug("Camera {} is not handling update for {}", getDeviceId(), camera.getDeviceId());
            return;
        }

        logger.debug("Updating camera {}", camera.getDeviceId());

        setLastUpdate(camera);
        updateChannels(camera);
        updateStatus(camera.isOnline() == null ? ThingStatus.UNKNOWN
                : camera.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        updateProperty(PROPERTY_FIRMWARE_VERSION, camera.getSoftwareVersion());
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_CAMERA_UPDATE_PATH, field, value);
    }
}
