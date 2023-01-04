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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_VALUE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.Device;
import org.openhab.binding.netatmo.internal.api.dto.Home;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link LocationChannelHelper} handles specific channels of modules holding a location data
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class LocationChannelHelper extends ChannelHelper {

    public LocationChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (CHANNEL_VALUE.equals(channelId)) {
            State point = UnDefType.UNDEF;
            if (naThing instanceof Home) {
                point = ((Home) naThing).getLocation();
            } else if (naThing instanceof Device) {
                point = ((Device) naThing).getPlace().map(place -> place.getLocation()).orElse(point);
            }
            return point;
        }
        return null;
    }
}
