/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.WebhookDashboard;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.api.dto.WebhookExtraParams;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.types.State;

/**
 * The {@link ChannelHelper} is the base class for all channel helpers.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class ChannelHelper {
    private final Set<String> channelGroups;

    private @Nullable Object data;

    public ChannelHelper(Set<String> providedGroups) {
        channelGroups = providedGroups;
    }

    public void setNewData(@Nullable Object data) {
        this.data = data;
    }

    public final @Nullable State getChannelState(String channelId, @Nullable String groupId, Configuration config) {
        if (channelGroups.isEmpty() || (groupId != null && channelGroups.contains(groupId))) {
            if (data instanceof HomeEvent homeEvent
                    && internalGetHomeEvent(channelId, groupId, homeEvent) instanceof State state) {
                return state;
            }

            if (data instanceof WebhookEvent webHookEvent
                    && internalGetWebhookEvent(channelId, groupId, webHookEvent) instanceof State state) {
                return state;
            }

            if (data instanceof WebhookExtraParams extraParams
                    && internalGetExtraParams(channelId, groupId, extraParams) instanceof State state) {
                return state;
            }

            if (data instanceof WebhookDashboard whDashboard
                    && internalGetWebDashboard(channelId, groupId, whDashboard) instanceof State state) {
                return state;
            }

            if (data instanceof Event event && internalGetEvent(channelId, event) instanceof State state) {
                return state;
            }

            if (data instanceof NAThing naThing) {
                if (internalGetProperty(channelId, naThing, config) instanceof State state) {
                    return state;
                }
                if (naThing.getDashboardData() instanceof Dashboard dashboard
                        && internalGetDashboard(channelId, dashboard) instanceof State state) {
                    return state;
                }
            }

            if (data instanceof NAObject naObject && internalGetObject(channelId, naObject) instanceof State state) {
                return state;
            }

            return internalGetOther(channelId);
        }
        return null;
    }

    protected @Nullable State internalGetExtraParams(String channelId, @Nullable String groupId,
            WebhookExtraParams extraParams) {
        return null;
    }

    protected @Nullable State internalGetObject(String channelId, NAObject localData) {
        return null;
    }

    protected @Nullable State internalGetOther(String channelId) {
        return null;
    }

    protected @Nullable State internalGetDashboard(String channelId, Dashboard dashboard) {
        return null;
    }

    protected @Nullable State internalGetWebDashboard(String channelId, @Nullable String groupId,
            WebhookDashboard dashboard) {
        return null;
    }

    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        return null;
    }

    protected @Nullable State internalGetEvent(String channelId, Event event) {
        return null;
    }

    protected @Nullable State internalGetHomeEvent(String channelId, @Nullable String groupId, HomeEvent event) {
        return null;
    }

    protected @Nullable State internalGetWebhookEvent(String channelId, @Nullable String groupId, WebhookEvent event) {
        return null;
    }
}
