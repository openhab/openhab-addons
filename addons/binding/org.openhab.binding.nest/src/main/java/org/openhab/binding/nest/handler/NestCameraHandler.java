/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.internal.data.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_APP_URL;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_AUDIO_INPUT_ENABLED;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_PUBLIC_SHARE_ENABLED;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_PUBLIC_SHARE_URL;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_SNAPSHOT_URL;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_STREAMING;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_VIDEO_HISTORY_ENABLED;
import static org.openhab.binding.nest.NestBindingConstants.CHANNEL_WEB_URL;
import static org.openhab.binding.nest.NestBindingConstants.NEST_CAMERA_UPDATE_URL;
import static org.openhab.binding.nest.NestBindingConstants.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.nest.NestBindingConstants.PROPERTY_ID;

/**
 * Handles all the updates to the camera as well as handling the commands that send
 * updates to nest.
 *
 * @author David Bennett - initial contribution
 */
public class NestCameraHandler extends NestBaseHandler {
    private Logger logger = LoggerFactory.getLogger(NestCameraHandler.class);

    public NestCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_STREAMING.equals(channelUID.getId())) {
            // Change the mode.
            if (command instanceof OnOffType) {
                // Set the mode to be the cmd value.
                addUpdateRequest("is_streaming", String.valueOf(command == OnOffType.ON));
            }
        }
    }

    /**
     * Updates the camera with data from nest.
     *
     * @param camera The new camera data
     */
    void updateCamera(Camera camera) {
        logger.debug("Updating camera {}", camera.getDeviceId());
        updateState(CHANNEL_STREAMING, camera.isStreaming() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_VIDEO_HISTORY_ENABLED, camera.isVideoHistoryEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_AUDIO_INPUT_ENABLED, camera.isAudioInputEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_PUBLIC_SHARE_ENABLED, camera.isPublicShareEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_PUBLIC_SHARE_URL, new StringType(camera.getPublicShareUrl()));
        updateState(CHANNEL_WEB_URL, new StringType(camera.getWebUrl()));
        updateState(CHANNEL_APP_URL, new StringType(camera.getAppUrl()));
        updateState(CHANNEL_SNAPSHOT_URL, new StringType(camera.getSnapshotUrl()));

        updateStatus(camera.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        // Setup the properties for this device.
        updateProperty(PROPERTY_ID, camera.getDeviceId());
        updateProperty(PROPERTY_FIRMWARE_VERSION, camera.getSoftwareVersion());
    }

    private void addUpdateRequest(String field, Object value) {
        addUpdateRequest(NEST_CAMERA_UPDATE_URL, field, value);
    }
}
