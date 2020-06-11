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
            String floodlightGetURL = localCameraURL.get() + FLOODLIGHT_GET_URL_PATH;

            Optional<JSONObject> json = executeGETRequestJSON(floodlightGetURL);
            Optional<String> mode = json.map(j -> j.getString("mode"));
            if(mode.isPresent()) {
                isAutoMode = mode.get().equals("auto");
            } else {
                logger.error("The floodlight state could not get found!");
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

            executeGETRequest(url.toString());
        }
    }

    private Optional<String> getLocalCameraURL() {
        if(!isLocalCameraURLLoaded) {
            String vpnUrl = getVpnUrl();
            if(vpnUrl != null) {
                String pingURL = vpnUrl + PING_URL_PATH;
                Optional<JSONObject> json = executeGETRequestJSON(pingURL);
                localCameraURL = json.map(j -> j.getString("local_url"));
                isLocalCameraURLLoaded = true;
            }
        }
        return localCameraURL;
    }

    private Optional<JSONObject> executeGETRequestJSON(String url) {
        Optional<String> content = executeGETRequest(url);
        if (content.isPresent()) {
            return Optional.of(new JSONObject(content.get()));
        }

        logger.error("The request-result could not get retrieved!");
        return Optional.empty();
    }

    private Optional<String> executeGETRequest(String url) {
        try {
            String content = HttpUtil.executeUrl("GET", url, 5000);
            if (content != null && !content.isEmpty()) {
                return Optional.of(content);
            }
        } catch (IOException | JSONException e) {
            logger.error("Error on loading local camera url!", e);
        }
        return Optional.empty();
    }
}
