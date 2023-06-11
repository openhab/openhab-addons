/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Set;

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
    private static final String QUALITY_CONF_ENTRY = "quality";
    private static final String LIVE_PICTURE = "/live/snapshot_720.jpg";
    private boolean isLocal;
    private @Nullable String vpnUrl;
    private @Nullable String localUrl;

    public CameraChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
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
            boolean isMonitoring = OnOffType.ON.equals(camera.getMonitoring());
            switch (channelId) {
                case CHANNEL_MONITORING:
                    return camera.getMonitoring();
                case CHANNEL_SD_CARD:
                    return toStringType(camera.getSdStatus());
                case CHANNEL_ALIM_STATUS:
                    return toStringType(camera.getAlimStatus());
                case CHANNEL_LIVEPICTURE_VPN_URL:
                    return toStringType(getLivePictureURL(false, isMonitoring));
                case CHANNEL_LIVEPICTURE_LOCAL_URL:
                    return toStringType(getLivePictureURL(true, isMonitoring));
                case CHANNEL_LIVEPICTURE:
                    return toRawType(getLivePictureURL(isLocal, isMonitoring));
                case CHANNEL_LIVESTREAM_VPN_URL:
                    return getLiveStreamURL(false, (String) config.get(QUALITY_CONF_ENTRY), isMonitoring);
                case CHANNEL_LIVESTREAM_LOCAL_URL:
                    return getLiveStreamURL(true, (String) config.get(QUALITY_CONF_ENTRY), isMonitoring);
            }
        }
        return null;
    }

    private @Nullable String getLivePictureURL(boolean local, boolean isMonitoring) {
        String url = local ? localUrl : vpnUrl;
        if (!isMonitoring || (local && !isLocal) || url == null) {
            return null;
        }
        return String.format("%s%s", url, LIVE_PICTURE);
    }

    private State getLiveStreamURL(boolean local, @Nullable String configQual, boolean isMonitoring) {
        String url = local ? localUrl : vpnUrl;
        if (!isMonitoring || (local && !isLocal) || url == null) {
            return UnDefType.NULL;
        }
        String finalQual = configQual != null ? configQual : "poor";
        return toStringType("%s/live/%s", url, local ? String.format("files/%s/index.m3u8", finalQual) : "index.m3u8");
    }
}
