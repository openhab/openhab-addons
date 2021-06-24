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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toQuantityType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link RoomChannelHelper} handle specific behavior
 * of the room
 *
 * @author Markus Dillmann - Initial contribution
 *
 */
@NonNullByDefault
public class RoomChannelHelper extends AbstractChannelHelper {

    public RoomChannelHelper() {
        super(Set.of(GROUP_ROOM_PROPERTIES, GROUP_ROOM_TEMPERATURE));
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        if (naThing instanceof NARoom) {
            NARoom room = (NARoom) naThing;
            switch (channelId) {
                case CHANNEL_ROOM_WINDOW_OPEN:
                    return (room.isOpenWindow() ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                case CHANNEL_ANTICIPATING:
                    return (room.isAnticipating() ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                case CHANNEL_ROOM_HEATING_POWER:
                    return toQuantityType(room.getHeatingPowerRequest(), Units.PERCENT);
                case CHANNEL_VALUE:
                    return toQuantityType(room.getThermMeasuredTemperature(), MeasureClass.INTERIOR_TEMPERATURE);
            }
        }
        return null;
    }
}
