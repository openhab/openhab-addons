/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.presence;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.camera.CameraHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_CAMERA_FLOODLIGHT;

/**
 * {@link NAPresenceCameraHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein
 */
@NonNullByDefault
public class NAPresenceCameraHandler extends CameraHandler {

    private static final String PING_URL_PATH = "/command/ping";
    private static final String FLOODLIGHT_GET_URL_PATH = "/command/floodlight_get_config";
    private static final String FLOODLIGHT_SET_URL_PATH = "/command/floodlight_set_config";

    private final Logger logger = LoggerFactory.getLogger(NAPresenceCameraHandler.class);

    private Optional<String> localCameraURL = Optional.empty();
    private boolean isLocalCameraURLLoaded;
    private boolean isAutoMode;

    public NAPresenceCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        if (CHANNEL_CAMERA_FLOODLIGHT.equals(channelId)) {
            switchFloodlight(OnOffType.ON.equals(command));
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    protected State getNAThingProperty(@NonNull String channelId) {
        if (CHANNEL_CAMERA_FLOODLIGHT.equals(channelId)) {
            return getFloodlightState();
        }
        return super.getNAThingProperty(channelId);
    }

    private State getFloodlightState() {
        Optional<String> localCameraURL = getLocalCameraURL();
        if(localCameraURL.isPresent()) {
            String floodlightURL = localCameraURL.get() + FLOODLIGHT_GET_URL_PATH;
            try {
                String content = HttpUtil.executeUrl("GET", floodlightURL, 5000);
                if (content != null && !content.isEmpty()) {
                    JSONObject json = new JSONObject(content);
                    String mode = json.getString("mode");
                    if("auto".equals(mode)) {
                        isAutoMode = true; //TODO don't change a state within a get method...
                    }
                    return ChannelTypeUtils.toOnOffType("on".equals(mode));
                } else {
                    logger.error("The floodlight state could not get found!");
                }
            } catch (IOException | JSONException e) {
                logger.error("Error on checking floodlight state!", e);
            }
        }
        return UnDefType.UNDEF;
    }

    private void switchFloodlight(boolean isOn) {
        Optional<String> localCameraURL = getLocalCameraURL();
        if(localCameraURL.isPresent()) {
            StringBuilder url = new StringBuilder();
            url.append(localCameraURL.get());
            url.append(FLOODLIGHT_SET_URL_PATH);
            url.append("?config=%7B%22mode%22:%22");
            if(isOn) {
                url.append("on");
            } else {
                if(isAutoMode) {
                    url.append("auto");
                } else {
                    url.append("off");
                }
            }
            url.append("%22%7D");

            try {
                HttpUtil.executeUrl("GET", url.toString(), 5000);
            } catch (IOException | JSONException e) {
                logger.error("Error on checking floodlight state!", e);
            }
        }
    }

    private Optional<String> getLocalCameraURL() {
        if(!isLocalCameraURLLoaded) {
            String vpnUrl = getVpnUrl();
            if(vpnUrl != null) {
                String pingURL = vpnUrl + PING_URL_PATH;
                try {
                    String content = HttpUtil.executeUrl("GET", pingURL, 5000);
                    if (content != null && !content.isEmpty()) {
                        JSONObject json = new JSONObject(content);
                        localCameraURL = Optional.of(json.getString("local_url"));
                    } else {
                        logger.error("The local camera url could not get found!");
                    }
                } catch (IOException | JSONException e) {
                    logger.error("Error on loading local camera url!", e);
                }
                isLocalCameraURLLoaded = true;
            }
        }
        return localCameraURL;
    }
}
