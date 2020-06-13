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

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toOnOffType;

import io.swagger.client.model.NAWelcomeCamera;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
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
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE;

/**
 * {@link NAPresenceCameraHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein
 */
@NonNullByDefault
public class NAPresenceCameraHandler extends CameraHandler {

    private static final String PING_URL_PATH = "/command/ping";
    private static final String FLOODLIGHT_SET_URL_PATH = "/command/floodlight_set_config";

    private final Logger logger = LoggerFactory.getLogger(NAPresenceCameraHandler.class);

    private Optional<String> localCameraURL = Optional.empty();
    private boolean isLocalCameraURLLoaded;
    private State floodlightAutoModeState = UnDefType.UNDEF;

    public NAPresenceCameraHandler(final Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                if(OnOffType.ON.equals(command)) {
                    switchFloodlight(true);
                } else if(OnOffType.OFF.equals(command)) {
                    switchFloodlight(false);
                }
                break;
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                if(OnOffType.ON.equals(command)) {
                    switchFloodlightAutoMode(true);
                } else if(OnOffType.OFF.equals(command)) {
                    switchFloodlightAutoMode(false);
                }
                break;
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    protected State getNAThingProperty(@NonNull String channelId) {
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                return getFloodlightState();
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                if(UnDefType.UNDEF.equals(floodlightAutoModeState)) {
                    floodlightAutoModeState = getFloodlightAutoModeState();
                }
                return floodlightAutoModeState;
        }
        return super.getNAThingProperty(channelId);
    }

    private State getFloodlightState() {
        if (module != null) {
            final boolean isOn = NAWelcomeCamera.LightModeStatusEnum.ON.equals(module.getLightModeStatus());
            return toOnOffType(isOn);
        }
        return UnDefType.UNDEF;
    }

    private State getFloodlightAutoModeState() {
        if (module != null) {
            return toOnOffType(NAWelcomeCamera.LightModeStatusEnum.AUTO.equals(module.getLightModeStatus()));
        }
        return UnDefType.UNDEF;
    }

    private void switchFloodlight(boolean isOn) {
        if (isOn) {
            changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum.ON);
        } else {
            switchFloodlightAutoMode(OnOffType.ON.equals(floodlightAutoModeState));
        }
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        floodlightAutoModeState = toOnOffType(isAutoMode);
        if (isAutoMode) {
            changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum.AUTO);
        } else {
            changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum.OFF);
        }
    }

    private void changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum mode) {
        Optional<String> localCameraURL = getLocalCameraURL();
        if (localCameraURL.isPresent()) {
            String url = localCameraURL.get()
                    + FLOODLIGHT_SET_URL_PATH
                    + "?config=%7B%22mode%22:%22"
                    + mode.toString()
                    + "%22%7D";
            executeGETRequest(url);
        }
    }

    private Optional<String> getLocalCameraURL() {
        if (!isLocalCameraURLLoaded) {
            String vpnUrl = getVpnUrl();
            if (vpnUrl != null) {
                String pingURL = vpnUrl + PING_URL_PATH;
                Optional<JSONObject> json = executeGETRequestJSON(pingURL);
                localCameraURL = json.map(j -> j.getString("local_url"));
                isLocalCameraURLLoaded = true;
            }
        }
        return localCameraURL;
    }

    private Optional<JSONObject> executeGETRequestJSON(String url) {
        Optional<JSONObject> jsonContent = executeGETRequest(url).map(JSONObject::new);
        if(!jsonContent.isPresent()) {
            logger.error("The request-result could not get retrieved!");
        }
        return jsonContent;
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
