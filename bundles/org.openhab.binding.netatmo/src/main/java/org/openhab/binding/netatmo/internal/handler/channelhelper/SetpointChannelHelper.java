/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.Room;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SetpointChannelHelper} handles channels for a room capable of managing a thermostat
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SetpointChannelHelper extends ChannelHelper {

    public SetpointChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetObject(String channelId, NAObject naObject) {
        if (naObject instanceof Room room) {
            switch (channelId) {
                case CHANNEL_SETPOINT_MODE:
                    return toStringType(room.getSetpointMode().name());
                case CHANNEL_SETPOINT_START_TIME:
                    return toDateTimeType(room.getSetpointBegin());
                case CHANNEL_SETPOINT_END_TIME:
                    return toDateTimeType(room.getSetpointEnd());
                case CHANNEL_VALUE:
                    switch (room.getSetpointMode()) {
                        case OFF:
                        case MAX:
                            return UnDefType.UNDEF;
                        case AWAY:
                        case HOME:
                        case MANUAL:
                        case SCHEDULE:
                        case FROST_GUARD:
                        case PROGRAM:
                            return toQuantityType(room.getSetpointTemp(), MeasureClass.INSIDE_TEMPERATURE);
                        case UNKNOWN:
                            return UnDefType.NULL;
                    }
            }
        }
        return null;
    }
}
