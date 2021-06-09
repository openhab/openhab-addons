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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_VALUE;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.GROUP_ROOM_TEMPERATURE;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.types.State;

/**
 * The {@link RoomTempChannelHelper} handle specific behavior
 * of the thermostat module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RoomTempChannelHelper extends AbstractChannelHelper {

    public RoomTempChannelHelper() {
        super(Set.of(GROUP_ROOM_TEMPERATURE));
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NARoom room = (NARoom) naThing;
        Double temp = room.getThermMeasuredTemperature();
        if (temp != null && CHANNEL_VALUE.equals(channelId)) {
            return toQuantityType(temp, MeasureClass.INTERIOR_TEMPERATURE);
        }
        return null;
    }
}
