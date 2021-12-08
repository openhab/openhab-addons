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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.GROUP_EXTENSION;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
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
    private final Set<String> channelGroupTypes;
    private final Set<String> channelGroups = new HashSet<>();
    private @Nullable MeasureClass measureClass = null;

    public AbstractChannelHelper(String... providedGroups) {
        this.channelGroupTypes = Set.of(providedGroups);
        // Sets the list of served groups base on group type names minus '-extended'
        channelGroupTypes.forEach(groupType -> channelGroups.add(groupType.replace(GROUP_EXTENSION, "")));
    }

    public AbstractChannelHelper(String providedGroup, MeasureClass measureClass) {
        this(providedGroup);
        this.measureClass = measureClass;
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
            if (channelGroups.isEmpty() || (groupId != null && channelGroups.contains(groupId))) {
                result = internalGetProperty(channelId, currentData);
                if (result == null) {
                    NADashboard dashboard = currentData.getDashboardData();
                    if (dashboard != null) {
                        result = internalGetDashboard(channelId, dashboard);
                    }
                    if (result == null) {
                        result = internalGetOther(channelId);
                    }
                }
            }
        }
        return result;
    }

    protected @Nullable State internalGetOther(String channelId) {
        return null;
    }

    protected @Nullable State internalGetDashboard(String channelId, NADashboard dashboard) {
        return null;
    }

    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        return null;
    }

    public Set<String> getChannelGroupTypes() {
        return channelGroupTypes;
    }

    public Set<String> getMeasureChannels() {
        MeasureClass measure = measureClass;
        return measure != null ? measure.channels.keySet() : Set.of();
    }
}
