/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Objects;
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

    private @NonNullByDefault({}) String vpnUrl;
    private @Nullable String localUrl;

    public CameraChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    public void setUrls(String vpnUrl, @Nullable String localUrl) {
        this.vpnUrl = vpnUrl;
        this.localUrl = localUrl;
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (naThing instanceof HomeStatusModule camera) {
            return switch (channelId) {
                case CHANNEL_MONITORING -> camera.getMonitoring();
                case CHANNEL_SD_CARD -> toStringType(camera.getSdStatus());
                case CHANNEL_ALIM_STATUS -> toStringType(camera.getAlimStatus());
                default -> liveChannels(channelId, config, camera, OnOffType.ON.equals(camera.getMonitoring()),
                        localUrl != null);
            };
        }
        return null;
    }

    private @Nullable State liveChannels(String channelId, Configuration config, HomeStatusModule camera,
            boolean isMonitoring, boolean isLocal) {
        if (vpnUrl == null) {
            setUrls(Objects.requireNonNull(camera.getVpnUrl()), localUrl);
        }
        return switch (channelId) {
            case CHANNEL_LIVEPICTURE_VPN_URL -> toStringType(getLivePictureURL(false, isMonitoring));
            case CHANNEL_LIVEPICTURE_LOCAL_URL ->
                isLocal ? toStringType(getLivePictureURL(true, isMonitoring)) : UnDefType.NULL;
            case CHANNEL_LIVEPICTURE -> {
                State result = toRawType(getLivePictureURL(isLocal, isMonitoring));
                if (UnDefType.NULL.equals(result) && isLocal) {
                    // If local read of the picture is unsuccessfull, try the VPN version
                    result = toRawType(getLivePictureURL(false, isMonitoring));
                }
                yield result;
            }
            case CHANNEL_LIVESTREAM_VPN_URL ->
                getLiveStreamURL(false, (String) config.get(QUALITY_CONF_ENTRY), isMonitoring);
            case CHANNEL_LIVESTREAM_LOCAL_URL ->
                isLocal ? getLiveStreamURL(true, (String) config.get(QUALITY_CONF_ENTRY), isMonitoring)
                        : UnDefType.NULL;
            default -> null;
        };
    }

    private String getUrl(boolean local) {
        return Objects.requireNonNull(local ? localUrl : vpnUrl);
    }

    public @Nullable String getLivePictureURL(boolean local, boolean isMonitoring) {
        if (!isMonitoring || (local && (localUrl != null))) {
            return null;
        }
        return "%s%s".formatted(getUrl(local), LIVE_PICTURE);
    }

    private State getLiveStreamURL(boolean local, @Nullable String configQual, boolean isMonitoring) {
        if (!isMonitoring || (local && (localUrl != null))) {
            return UnDefType.NULL;
        }
        String finalQual = configQual != null ? configQual : "poor";
        return toStringType("%s/live/%sindex.m3u8", getUrl(local), local ? "files/%s/".formatted(finalQual) : "");
    }
}
