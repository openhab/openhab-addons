/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.data.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all the updates to the camera as well as handling the commands that send
 * updates to nest.
 *
 * @author David Bennett - initial contribution
 */
public class NestCameraHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(NestCameraHandler.class);
    private Camera lastData;

    public NestCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_STREAMING)) {
            // Change the mode.
            if (command instanceof OnOffType) {
                OnOffType cmd = (OnOffType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest("is_streaming", cmd == OnOffType.ON ? "true" : "false");
            }
        }
    }

    private void addUpdateRequest(String field, Object value) {
        String deviceId = getThing().getProperties().get(NestBindingConstants.PROPERTY_ID);
        StringBuilder builder = new StringBuilder().append(NestBindingConstants.NEST_CAMERA_UPDATE_URL)
                .append(deviceId);
        NestUpdateRequest request = new NestUpdateRequest();
        request.setUpdateUrl(builder.toString());
        request.addValue(field, value);
        NestBridgeHandler bridge = (NestBridgeHandler) getBridge();
        bridge.addUpdateRequest(request);
    }

    /**
     * Updates the camera with data from nest.
     *
     * @param camera The new camera data
     */
    public void updateCamera(Camera camera) {
        logger.debug("Updating camera {}", camera.getDeviceId());
        if (lastData == null || !lastData.equals(camera)) {
            updateState(CHANNEL_STREAMING, camera.isStreaming() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_VIDEO_HISTORY_ENABLED, camera.isVideoHistoryEnabled() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_AUDIO_INPUT_ENABLED, camera.isAudioInputEnabled() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_PUBLIC_SHARE_ENABLED, camera.isPublicShareEnabled() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_PUBLIC_SHARE_URL, new StringType(camera.getPublicShareUrl()));
            updateState(CHANNEL_WEB_URL, new StringType(camera.getWebUrl()));
            updateState(CHANNEL_APP_URL, new StringType(camera.getAppUrl()));
            updateState(CHANNEL_SNAPSHOT_URL, new StringType(camera.getSnapshotUrl()));

            if (camera.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

            // Setup the properties for this device.
            updateProperty(PROPERTY_ID, camera.getDeviceId());
            updateProperty(PROPERTY_FIRMWARE_VERSION, camera.getSoftwareVersion());
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }
}
