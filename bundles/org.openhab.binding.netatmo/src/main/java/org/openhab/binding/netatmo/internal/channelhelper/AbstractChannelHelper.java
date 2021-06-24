/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.channelhelper;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * The {@link AbstractChannelHelper} handle specific common behaviour
 * of all channel helpers
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public abstract class AbstractChannelHelper {
    private @Nullable NAThing data;
    private final Set<String> providedGroup;

    public AbstractChannelHelper(Set<String> providedGroup) {
        this.providedGroup = providedGroup;
    }

    public void setNewData(NAThing data) {
        this.data = data;
    }

    public final @Nullable State getChannelState(ChannelUID channelUID) {
        State result = null;
        NAThing currentData = data;
        if (currentData != null) {
            String channelId = channelUID.getIdWithoutGroup();
            String groupId = channelUID.getGroupId();
            if (providedGroup.isEmpty() || (groupId != null && providedGroup.contains(groupId))) {
                result = internalGetProperty(channelId, currentData);
                if (result == null) {
                    NADashboard dashboard = currentData.getDashboardData();
                    if (dashboard != null) {
                        result = internalGetDashboard(channelId, dashboard);
                    }
                }
            }
        }
        return result;
    }

    protected @Nullable State internalGetDashboard(String channelId, NADashboard dashboard) {
        return null;
    }

    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        return null;
    }
}
