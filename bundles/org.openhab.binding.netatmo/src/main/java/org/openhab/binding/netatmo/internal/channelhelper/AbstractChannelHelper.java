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

import java.time.ZoneId;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADashboard;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
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
    // TODO : finish removal of ZoneId in channelhelpers
    protected final ZoneId zoneId;
    protected final Thing thing;
    private @Nullable NAThing naThing;
    private final Set<String> providedGroup;

    public AbstractChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        this(thing, timeZoneProvider, Set.of());
    }

    public AbstractChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider, Set<String> providedGroup) {
        this.zoneId = timeZoneProvider.getTimeZone();
        this.thing = thing;
        this.providedGroup = providedGroup;
    }

    public void setNewData(NAThing naThing) {
        this.naThing = naThing;
    }

    public @Nullable State getNAThingProperty(ChannelUID channelUID) {
        State result = null;
        NAThing module = this.naThing;
        if (module != null) {
            String channelId = channelUID.getIdWithoutGroup();
            String groupId = channelUID.getGroupId();
            if (providedGroup.isEmpty() || (groupId != null && providedGroup.contains(groupId))) {
                result = internalGetProperty(module, channelId);
                if (result == null) {
                    NADashboard dashboard = module.getDashboardData();
                    if (dashboard != null) {
                        result = internalGetDashboard(dashboard, channelId);
                    }
                }
            }
        }
        return result;
    }

    protected @Nullable State internalGetDashboard(NADashboard dashboard, String channelId) {
        return null;
    }

    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        return null;
    }
}
