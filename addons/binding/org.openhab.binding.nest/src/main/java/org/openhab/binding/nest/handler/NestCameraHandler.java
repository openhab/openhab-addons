/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.data.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all the updates to the camera as well as handling the commands that send
 * updates to Nest.
 *
 * @author David Bennett - initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
public class NestCameraHandler extends NestBaseHandler<Camera> {
    private final Logger logger = LoggerFactory.getLogger(NestCameraHandler.class);

    public NestCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, Camera camera) {
        switch (channelUID.getId()) {
            case CHANNEL_APP_URL:
                return getAsStringTypeOrNull(camera.getAppUrl());
            case CHANNEL_AUDIO_INPUT_ENABLED:
                return getAsOnOffType(camera.isAudioInputEnabled());
            case CHANNEL_LAST_ONLINE_CHANGE:
                return getAsDateTimeTypeOrNull(camera.getLastIsOnlineChange());
            case CHANNEL_PUBLIC_SHARE_ENABLED:
                return getAsOnOffType(camera.isPublicShareEnabled());
            case CHANNEL_PUBLIC_SHARE_URL:
                return getAsStringTypeOrNull(camera.getPublicShareUrl());
            case CHANNEL_SNAPSHOT_URL:
                return getAsStringTypeOrNull(camera.getSnapshotUrl());
            case CHANNEL_STREAMING:
                return getAsOnOffType(camera.isStreaming());
            case CHANNEL_VIDEO_HISTORY_ENABLED:
                return getAsOnOffType(camera.isVideoHistoryEnabled());
            case CHANNEL_WEB_URL:
                return getAsStringTypeOrNull(camera.getWebUrl());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            if (getLastUpdate() != null) {
                updateState(channelUID, getChannelState(channelUID, getLastUpdate()));
            }
        } else if (CHANNEL_STREAMING.equals(channelUID.getId())) {
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
        updateStatus(camera.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
        updateProperty(PROPERTY_FIRMWARE_VERSION, camera.getSoftwareVersion());
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_CAMERA_UPDATE_URL, field, value);
    }
}
