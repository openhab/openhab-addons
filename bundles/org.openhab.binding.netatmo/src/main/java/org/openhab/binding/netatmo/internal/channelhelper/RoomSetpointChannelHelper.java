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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.NARoom;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link RoomSetpointChannelHelper} handle specific behavior
 * of a room
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class RoomSetpointChannelHelper extends AbstractChannelHelper {

    public RoomSetpointChannelHelper() {
        super(Set.of(GROUP_TH_SETPOINT));
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NARoom room = (NARoom) naThing;
        switch (channelId) {
            case CHANNEL_VALUE:
                return getCurrentSetpoint(room);
            case CHANNEL_SETPOINT_MODE:
                return new StringType(room.getThermSetpointMode().name());
            case CHANNEL_SETPOINT_START_TIME:
                return toDateTimeType(room.getThermSetpointStartTime());
            case CHANNEL_SETPOINT_END_TIME:
                return toDateTimeType(room.getThermSetpointEndTime());
        }
        return null;
    }

    private State getCurrentSetpoint(NARoom room) {
        SetpointMode thermSetPointMode = room.getThermSetpointMode();
        switch (thermSetPointMode) {
            case AWAY:
            case HOME:
            case MANUAL:
            case SCHEDULE:
            case FROST_GUARD:
            case PROGRAM:
                return toQuantityType(room.getThermSetpointTemperature(), MeasureClass.INTERIOR_TEMPERATURE);
            case OFF:
            case MAX:
            case UNKNOWN:
                return UnDefType.UNDEF;
        }
        return UnDefType.NULL;
    }
}
