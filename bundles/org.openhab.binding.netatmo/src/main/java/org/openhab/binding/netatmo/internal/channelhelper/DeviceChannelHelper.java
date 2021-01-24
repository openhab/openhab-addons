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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAPlace;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link DeviceChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceChannelHelper extends AbstractChannelHelper {

    public DeviceChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_DEVICE);
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        if (CHANNEL_LOCATION.equals(channelId)) {
            PointType point = null;
            if (naThing instanceof NAHome) {
                point = ((NAHome) naThing).getLocation();
            } else if (naThing instanceof NADevice) {
                NAPlace place = ((NADevice<?>) naThing).getPlace();
                point = place.getLocation();
            }
            return point != null ? point : UnDefType.UNDEF;
        } else if (CHANNEL_LAST_SEEN.equals(channelId)) {
            return ChannelTypeUtils.toDateTimeType(naThing.getLastSeen(), zoneId);
        }
        return null;
    }
}
