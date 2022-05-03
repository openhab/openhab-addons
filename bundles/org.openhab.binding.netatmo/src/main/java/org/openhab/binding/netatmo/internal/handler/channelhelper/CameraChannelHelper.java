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
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

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
            boolean monitoring = camera.getMonitoring() == OnOffType.ON;
            switch (channelId) {
                case CHANNEL_MONITORING:
                    return camera.getMonitoring();
                case CHANNEL_SD_CARD:
                    return toStringType(camera.getSdStatus());
                case CHANNEL_ALIM_STATUS:
                    return toStringType(camera.getAlimStatus());
                case CHANNEL_LIVEPICTURE_URL:
                    return toStringType(getLivePictureURL(monitoring));
                case CHANNEL_LIVEPICTURE:
                    return toRawType(getLivePictureURL(monitoring));
                case CHANNEL_LIVESTREAM_VPN_URL:
                    return getLiveStreamURL(monitoring, false, (String) config.get("quality"));
                case CHANNEL_LIVESTREAM_LOCAL_URL:
                    return getLiveStreamURL(monitoring, true, (String) config.get("quality"));
            }
        }
        return null;
    }

    private @Nullable String getLivePictureURL(boolean monitoring) {
        String url = isLocal ? localUrl : vpnUrl;
        return !monitoring || url == null ? null : String.format("%s%s", url, LIVE_PICTURE);
    }

    private State getLiveStreamURL(boolean monitoring, boolean local, @Nullable String configQual) {
        String url = local ? localUrl : vpnUrl;
        if (!monitoring || (local && !isLocal) || url == null) {
            return UnDefType.NULL;
        }
        String finalQual = configQual != null ? configQual : "poor";
        return toStringType(String.format("%s/live/%s", url,
                local ? String.format("files/%s/index.m3u8", finalQual) : "index.m3u8"));
    }
}
