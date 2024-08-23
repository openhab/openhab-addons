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
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;

/**
 * The {@link EventCameraChannelHelper} handles specific channels of sub-events (presence camera and doorbell).
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class EventCameraChannelHelper extends EventChannelHelper {

    public EventCameraChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetHomeEvent(String channelId, @Nullable String groupId, HomeEvent event) {
        if (groupId != null && groupId.startsWith(GROUP_SUB_EVENT)) {
            return switch (channelId) {
                case CHANNEL_EVENT_TYPE -> toStringType(event.getEventType());
                case CHANNEL_EVENT_TIME -> new DateTimeType(event.getTime());
                case CHANNEL_EVENT_MESSAGE -> toStringType(event.getName());
                case CHANNEL_EVENT_SNAPSHOT -> toRawType(event.getSnapshotUrl());
                case CHANNEL_EVENT_SNAPSHOT_URL -> toStringType(event.getSnapshotUrl());
                case CHANNEL_EVENT_VIGNETTE -> toRawType(event.getVignetteUrl());
                case CHANNEL_EVENT_VIGNETTE_URL -> toStringType(event.getVignetteUrl());
                default -> super.internalGetHomeEvent(channelId, groupId, event);
            };
        }
        return super.internalGetHomeEvent(channelId, groupId, event);
    }
}
