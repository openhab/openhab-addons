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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.VideoStatus;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EventChannelHelper} handles specific channels of cameras
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class EventChannelHelper extends ChannelHelper {
    private @Nullable String vpnUrl;
    private @Nullable String localUrl;
    protected ModuleType moduleType = ModuleType.UNKNOWN;

    public EventChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public void setUrls(@Nullable String vpnUrl, @Nullable String localUrl) {
        this.localUrl = localUrl;
        this.vpnUrl = vpnUrl;
    }

    @Override
    public void setNewData(@Nullable NAObject data) {
        if (data instanceof Event event && !event.getEventType().validFor(moduleType)) {
            return;
        }
        super.setNewData(data);
    }

    @Override
    protected @Nullable State internalGetEvent(String channelId, Event event) {
        return switch (channelId) {
            case CHANNEL_EVENT_TYPE -> toStringType(event.getEventType());
            case CHANNEL_EVENT_MESSAGE -> toStringType(event.getName());
            case CHANNEL_EVENT_TIME -> new DateTimeType(event.getTime());
            case CHANNEL_EVENT_PERSON_ID -> toStringType(event.getPersonId());
            case CHANNEL_EVENT_CAMERA_ID -> toStringType(event.getCameraId());
            case CHANNEL_EVENT_SUBTYPE ->
                event.getSubTypeDescription().map(ChannelTypeUtils::toStringType).orElse(UnDefType.NULL);
            case CHANNEL_EVENT_SNAPSHOT -> toRawType(event.getSnapshotUrl());
            case CHANNEL_EVENT_SNAPSHOT_URL -> toStringType(event.getSnapshotUrl());
            default -> null;
        };
    }

    @Override
    protected @Nullable State internalGetHomeEvent(String channelId, @Nullable String groupId, HomeEvent event) {
        return switch (channelId) {
            case CHANNEL_EVENT_VIDEO_STATUS ->
                event.getVideoId() != null ? toStringType(event.getVideoStatus()) : UnDefType.NULL;
            case CHANNEL_EVENT_VIDEO_LOCAL_URL -> getStreamURL(true, event.getVideoId(), event.getVideoStatus());
            case CHANNEL_EVENT_VIDEO_VPN_URL -> getStreamURL(false, event.getVideoId(), event.getVideoStatus());
            default -> null;
        };
    }

    private @Nullable String getUrl(boolean local) {
        return local ? localUrl : vpnUrl;
    }

    private State getStreamURL(boolean local, @Nullable String videoId, VideoStatus videoStatus) {
        String url = getUrl(local);
        if (url == null || videoId == null || videoStatus != VideoStatus.AVAILABLE) {
            return UnDefType.NULL;
        }
        return toStringType("%s/vod/%s/index.m3u8", url, videoId);
    }
}
