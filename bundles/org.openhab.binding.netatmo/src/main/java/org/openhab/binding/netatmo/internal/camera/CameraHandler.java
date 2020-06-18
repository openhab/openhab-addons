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
package org.openhab.binding.netatmo.internal.camera;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.netatmo.internal.ChannelTypeUtils;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import io.swagger.client.model.NAWelcomeCamera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * {@link CameraHandler} is the class used to handle Camera Data
 *
 * @author Sven Strohschein (partly moved code from NAWelcomeCameraHandler to introduce inheritance, see
 *         NAWelcomeCameraHandler)
 *
 */
@NonNullByDefault
public abstract class CameraHandler extends NetatmoModuleHandler<NAWelcomeCamera> {

    private static final String PING_URL_PATH = "/command/ping";
    private static final String STATUS_CHANGE_URL_PATH = "/command/changestatus";
    private static final String LIVE_PICTURE = "/live/snapshot_720.jpg";

    private final Logger logger = LoggerFactory.getLogger(CameraHandler.class);

    private Optional<CameraAddress> cameraAddress = Optional.empty();

    protected CameraHandler(@NonNull Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        switch (channelId) {
            case CHANNEL_CAMERA_STATUS:
            case CHANNEL_WELCOME_CAMERA_STATUS:
                if(command == OnOffType.ON) {
                    switchVideoSurveillance(true);
                } else if(command == OnOffType.OFF) {
                    switchVideoSurveillance(false);
                }
                break;
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    protected void updateProperties(NAWelcomeCamera moduleData) {
        updateProperties(null, moduleData.getType());
    }

    @SuppressWarnings("null")
    @Override
    protected State getNAThingProperty(@NonNull String channelId) {
        switch (channelId) {
            case CHANNEL_CAMERA_STATUS:
                return getStatusState();
            case CHANNEL_CAMERA_SDSTATUS:
                return getSdStatusState();
            case CHANNEL_CAMERA_ALIMSTATUS:
                return getAlimStatusState();
            case CHANNEL_CAMERA_ISLOCAL:
                return getIsLocalState();
            case CHANNEL_CAMERA_LIVEPICTURE_URL:
                return getLivePictureURLState();
            case CHANNEL_CAMERA_LIVEPICTURE:
                return getLivePictureState();
            case CHANNEL_CAMERA_LIVESTREAM_URL:
                return getLiveStreamState();
        }
        return super.getNAThingProperty(channelId);
    }

    protected State getStatusState() {
        return module != null ? toOnOffType(module.getStatus()) : UnDefType.UNDEF;
    }

    protected State getSdStatusState() {
        return module != null ? toOnOffType(module.getSdStatus()) : UnDefType.UNDEF;
    }

    protected State getAlimStatusState() {
        return module != null ? toOnOffType(module.getAlimStatus()) : UnDefType.UNDEF;
    }

    protected State getIsLocalState() {
        return module != null ? toOnOffType(module.getIsLocal()) : UnDefType.UNDEF;
    }

    protected State getLivePictureURLState() {
        Optional<String> livePictureURL = getLivePictureURL();
        return livePictureURL.map(ChannelTypeUtils::toStringType).orElse(UnDefType.UNDEF);
    }

    protected State getLivePictureState() {
        Optional<String> livePictureURL = getLivePictureURL();
        return !livePictureURL.isPresent() ? UnDefType.UNDEF : HttpUtil.downloadImage(livePictureURL.get());
    }

    protected State getLiveStreamState() {
        Optional<String> liveStreamURL = getLiveStreamURL();
        return !liveStreamURL.isPresent() ? UnDefType.UNDEF : new StringType(liveStreamURL.get());
    }

    /**
     * Get the url for the live snapshot
     *
     * @return Url of the live snapshot
     */
    private Optional<String> getLivePictureURL() {
        return getVpnUrl().map(u -> u += LIVE_PICTURE);
    }

    /**
     * Get the url for the live stream depending wether local or not
     *
     * @return Url of the live stream
     */
    private Optional<String> getLiveStreamURL() {
        Optional<String> result = getVpnUrl();
        if (!result.isPresent()) {
            return Optional.empty();
        }

        StringBuilder resultStringBuilder = new StringBuilder(result.get());
        resultStringBuilder.append("/live/index");
        if (isLocal()) {
            resultStringBuilder.append("_local");
        }
        resultStringBuilder.append(".m3u8");
        return Optional.of(resultStringBuilder.toString());
    }

    private Optional<String> getVpnUrl() {
        return (module == null) ? Optional.empty() : Optional.ofNullable(module.getVpnUrl());
    }

    public Optional<String> getStreamURL(String videoId) {
        Optional<String> result = getVpnUrl();
        if (!result.isPresent()) {
            return Optional.empty();
        }

        StringBuilder resultStringBuilder = new StringBuilder(result.get());
        resultStringBuilder.append("/vod/");
        resultStringBuilder.append(videoId);
        resultStringBuilder.append("/index");
        if (isLocal()) {
            resultStringBuilder.append("_local");
        }
        resultStringBuilder.append(".m3u8");
        return Optional.of(resultStringBuilder.toString());
    }

    @SuppressWarnings("null")
    private boolean isLocal() {
        return (module == null || module.getIsLocal() == null) ? false : module.getIsLocal();
    }

    private void switchVideoSurveillance(boolean isOn) {
        Optional<String> localCameraURL = getLocalCameraURL();
        if (localCameraURL.isPresent()) {
            String url = localCameraURL.get() + STATUS_CHANGE_URL_PATH + "?status=";
            if(isOn) {
                url += "on";
            } else {
                url += "off";
            }
            executeGETRequest(url);
        }
    }

    protected Optional<String> getLocalCameraURL() {
        Optional<String> vpnURLOptional = getVpnUrl();
        if (vpnURLOptional.isPresent()) {
            final String vpnURL = vpnURLOptional.get();

            //The local address is (re-)requested when it wasn't already determined or when the vpn address was changed.
            if (!cameraAddress.isPresent() || cameraAddress.get().isVpnURLChanged(vpnURL)) {
                Optional<JSONObject> json = executeGETRequestJSON(vpnURL + PING_URL_PATH);
                cameraAddress = json.map(j -> j.optString("local_url", null))
                        .map(localURL -> new CameraAddress(vpnURL, localURL));
            }
        }
        return cameraAddress.map(CameraAddress::getLocalURL);
    }

    private Optional<JSONObject> executeGETRequestJSON(String url) {
        try {
            return executeGETRequest(url).map(JSONObject::new);
        } catch (JSONException e) {
            logger.warn("Error on parsing the content as JSON!", e);
        }
        return Optional.empty();
    }

    protected @NonNull Optional<@NonNull String> executeGETRequest(@NonNull String url) {
        try {
            String content = HttpUtil.executeUrl("GET", url, 5000);
            if (content != null && !content.isEmpty()) {
                return Optional.of(content);
            }
        } catch (IOException e) {
            logger.warn("Error on accessing local camera url!", e);
        }
        return Optional.empty();
    }
}
