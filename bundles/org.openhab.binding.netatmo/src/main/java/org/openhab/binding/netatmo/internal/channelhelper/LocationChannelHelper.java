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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAPlace;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.library.types.PointType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link LocationChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class LocationChannelHelper extends AbstractChannelHelper {

    public LocationChannelHelper() {
        super(Set.of(GROUP_LOCATION));
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        if (CHANNEL_LOCATION.equals(channelId)) {
            PointType point = null;
            if (naThing instanceof NAHome) {
                point = ((NAHome) naThing).getLocation();
            } else if (naThing instanceof NADevice) {
                NAPlace place = ((NADevice) naThing).getPlace();
                if (place != null) {
                    point = place.getLocation();
                }
            }
            return point != null ? point : UnDefType.UNDEF;
        }
        return null;
    }
}
