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
package org.openhab.binding.netatmo.internal.welcome;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.internal.handler.NetatmoModuleHandler;

import io.swagger.client.model.NAWelcomeCamera;

/**
 * {@link NAWelcomeCameraHandler} is the class used to handle the Welcome Camera Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
public class NAWelcomeCameraHandler extends NetatmoModuleHandler<NAWelcomeCamera> {
    private static final String LIVE_PICTURE = "/live/snapshot_720.jpg";

    public NAWelcomeCameraHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(NAWelcomeCamera moduleData) {
        updateProperties(null, moduleData.getType());
    }

    @SuppressWarnings("null")
    @Override
    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_WELCOME_CAMERA_STATUS:
                return module != null ? toOnOffType(module.getStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_CAMERA_SDSTATUS:
                return module != null ? toOnOffType(module.getSdStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_CAMERA_ALIMSTATUS:
                return module != null ? toOnOffType(module.getAlimStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_CAMERA_ISLOCAL:
                return (module == null || module.getIsLocal() == null) ? UnDefType.UNDEF
                        : module.getIsLocal() ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL:
                return getLivePictureURL() == null ? UnDefType.UNDEF : toStringType(getLivePictureURL());
            case CHANNEL_WELCOME_CAMERA_LIVEPICTURE:
                return getLivePictureURL() == null ? UnDefType.UNDEF : HttpUtil.downloadImage(getLivePictureURL());
            case CHANNEL_WELCOME_CAMERA_LIVESTREAM_URL:
                return getLiveStreamURL() == null ? UnDefType.UNDEF : new StringType(getLiveStreamURL());
        }
        return super.getNAThingProperty(chanelId);
    }

    /**
     * Get the url for the live snapshot
     *
     * @return Url of the live snapshot
     */
    private String getLivePictureURL() {
        String result = getVpnUrl();
        if (result != null) {
            result += LIVE_PICTURE;
        }
        return result;
    }

    /**
     * Get the url for the live stream depending wether local or not
     *
     * @return Url of the live stream
     */
    private String getLiveStreamURL() {
        String result = getVpnUrl();
        if (result != null) {
            result += "/live/index";
            result += isLocal() ? "_local" : "";
            result += ".m3u8";
        }
        return result;
    }

    @SuppressWarnings("null")
    private String getVpnUrl() {
        return (module == null) ? null : module.getVpnUrl();
    }

    public String getStreamURL(String videoId) {
        String result = getVpnUrl();
        if (result != null) {
            result += "/vod/" + videoId + "/index";
            result += isLocal() ? "_local" : "";
            result += ".m3u8";
        }
        return result;
    }

    @SuppressWarnings("null")
    private boolean isLocal() {
        return (module == null || module.getIsLocal() == null) ? false : module.getIsLocal().booleanValue();
    }
}
