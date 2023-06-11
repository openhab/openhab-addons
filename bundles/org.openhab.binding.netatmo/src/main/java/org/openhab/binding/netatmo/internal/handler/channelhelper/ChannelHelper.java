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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.Dashboard;
import org.openhab.binding.netatmo.internal.api.dto.Event;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
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

    private @Nullable NAObject data;

    public ChannelHelper(Set<String> providedGroups) {
        channelGroups = providedGroups;
    }

    public void setNewData(@Nullable NAObject data) {
        this.data = data;
    }

    public final @Nullable State getChannelState(String channelId, @Nullable String groupId, Configuration config) {
        State result = null;
        if (channelGroups.isEmpty() || (groupId != null && channelGroups.contains(groupId))) {
            NAObject localData = data;
            if (localData instanceof HomeEvent) {
                result = internalGetHomeEvent(channelId, groupId, (HomeEvent) localData);
                if (result != null) {
                    return result;
                }
            }
            if (localData instanceof Event) {
                result = internalGetEvent(channelId, (Event) localData);
                if (result != null) {
                    return result;
                }
            }
            if (localData instanceof NAThing) {
                NAThing naThing = (NAThing) localData;
                result = internalGetProperty(channelId, naThing, config);
                if (result != null) {
                    return result;
                }
                Dashboard dashboard = naThing.getDashboardData();
                if (dashboard != null) {
                    result = internalGetDashboard(channelId, dashboard);
                    if (result != null) {
                        return result;
                    }
                }
            }
            if (localData instanceof NAObject) {
                result = internalGetObject(channelId, localData);
                if (result != null) {
                    return result;
                }
            }
            result = internalGetOther(channelId);
        }
        return result;
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

    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        return null;
    }

    protected @Nullable State internalGetEvent(String channelId, Event event) {
        return null;
    }

    protected @Nullable State internalGetHomeEvent(String channelId, @Nullable String groupId, HomeEvent event) {
        return null;
    }
}
