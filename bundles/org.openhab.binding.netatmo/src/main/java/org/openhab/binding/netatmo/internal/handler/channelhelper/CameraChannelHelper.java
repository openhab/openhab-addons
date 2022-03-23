/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.types.State;

/**
 * The {@link CameraChannelHelper} handles specific channels of cameras
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class CameraChannelHelper extends ChannelHelper {
    private static final String LIVE_PICTURE = "/live/snapshot_720.jpg";
    private boolean isLocal;
    private @Nullable String vpnUrl;
    private @Nullable String localUrl;

    public CameraChannelHelper() {
        super(GROUP_CAM_STATUS, GROUP_CAM_LIVE);
    }

    public void setUrls(String vpnUrl, @Nullable String localUrl) {
        this.localUrl = localUrl;
        this.vpnUrl = vpnUrl;
        this.isLocal = localUrl != null;
    }

    public @Nullable String getLocalURL() {
        return localUrl;
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (naThing instanceof HomeStatusModule) {
            HomeStatusModule camera = (HomeStatusModule) naThing;
            switch (channelId) {
                case CHANNEL_MONITORING:
                    return camera.getMonitoring();
                case CHANNEL_SD_CARD:
                    return toStringType(camera.getSdStatus());
                case CHANNEL_ALIM_STATUS:
                    return toStringType(camera.getAlimStatus());
                case CHANNEL_LIVEPICTURE_URL:
                    return toStringType(getLivePictureURL());
                case CHANNEL_LIVEPICTURE:
                    return toRawType(getLivePictureURL());
                case CHANNEL_LIVESTREAM_URL:
                    return toStringType(getLiveStreamURL((String) config.get("quality")));
            }
        }
        return null;
    }

    @Override
    protected @Nullable State internalGetEvent(String channelId, Event event) {
        if (event instanceof HomeEvent && CHANNEL_EVENT_VIDEO_URL.equals(channelId)) {
            HomeEvent homeEvent = (HomeEvent) event;
            return toStringType(getStreamURL(homeEvent.getVideoId()));
        }
        return null;
    }

    private @Nullable String getStreamURL(@Nullable String videoId) {
        String url = isLocal ? localUrl : vpnUrl;
        return url == null || videoId == null ? null
                : String.format("%s/vod/%s/index%s.m3u8", url, videoId, isLocal ? "_local" : "");
    }

    private @Nullable String getLivePictureURL() {
        String url = isLocal ? localUrl : vpnUrl;
        return url == null ? null : String.format("%s%s", url, LIVE_PICTURE);
    }

    private @Nullable String getLiveStreamURL(@Nullable String configQual) {
        String finalQual = configQual != null ? configQual : "poor";
        String url = isLocal ? localUrl : vpnUrl;
        return url == null ? null
                : String.format("%s/live/%s", url,
                        isLocal ? String.format("files/%s/index.m3u8", finalQual) : "index.m3u8");
    }
}
