/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.welcome;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;

import io.swagger.client.model.NAWelcomeCamera;

/**
 * {@link NAWelcomeCameraHandler} is the class used to handle the Welcome Camera Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeCameraHandler extends NetatmoModuleHandler<NAWelcomeCamera> {
    private static final String LIVE_PICTURE = "/live/snapshot_720.jpg";
    private String livePictureURL;
    private String vpnUrl;
    private boolean isLocal = false;
    private String liveStreamURL;

    public NAWelcomeCameraHandler(@NonNull Thing thing) {
        super(thing);
    }

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
        if (livePictureURL == null && module != null && module.getVpnUrl() != null) {
            livePictureURL = module.getVpnUrl() + LIVE_PICTURE;
        }
        return livePictureURL;
    }

    /**
     * Get the url for the live stream depending wether local or not
     *
     * @return Url of the live stream
     */
    private String getLiveStreamURL() {
        if (liveStreamURL == null && module != null) {
            liveStreamURL = getVpnUrl();
            if (liveStreamURL != null) {
                liveStreamURL += "/live/index";
                liveStreamURL += isLocal ? "_local" : "";
                liveStreamURL += ".m3u8";
            }
        }
        return liveStreamURL;
    }

    private String getVpnUrl() {
        if (vpnUrl == null && module != null) {
            vpnUrl = module.getVpnUrl();
            if (vpnUrl != null) {
                isLocal = module.getIsLocal();
            }
        }
        return vpnUrl;
    }

    public String getStreamURL(String videoId) {
        String result = getVpnUrl();
        if (result != null) {
            result += "/vod/" + videoId + "/index";
            result += isLocal ? "_local" : "";
            result += ".m3u8";
        }
        return result;
    }

}
