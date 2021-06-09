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
import static org.openhab.binding.netatmo.internal.utils.NetatmoCalendarUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEnergy;
import org.openhab.binding.netatmo.internal.api.dto.NAThermProgram;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NATimeTableItem;
import org.openhab.binding.netatmo.internal.api.dto.NAZone;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link HomeEnergyChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeEnergyChannelHelper extends AbstractChannelHelper {

    public HomeEnergyChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, Set.of(GROUP_HOME_ENERGY));
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NAHomeEnergy localThing = (NAHomeEnergy) naThing;
        NAThermProgram currentProgram = localThing.getActiveProgram();
        SetpointMode thermMode = localThing.getThermMode();
        switch (channelId) {
            case CHANNEL_SETPOINT_DURATION:
                return toQuantityType(localThing.getThermSetpointDefaultDuration(), Units.MINUTE);
            case CHANNEL_PLANNING:
                return (currentProgram != null ? toStringType(currentProgram.getName()) : null);
            case CHANNEL_SETPOINT_MODE:
                switch (thermMode) {
                    case PROGRAM:
                    case HOME:
                    case SCHEDULE:
                        NATimeTableItem currentProgramMode = getCurrentProgramMode(localThing.getActiveProgram());
                        if (currentProgram != null && currentProgramMode != null) {
                            NAZone zone = currentProgram.getZone(String.valueOf(currentProgramMode.getZoneId()));
                            if (zone != null) {
                                return new StringType(zone.getName());
                            }
                            return UnDefType.NULL;
                        }
                    case AWAY:
                    case MANUAL:
                    case FROST_GUARD:
                        return new StringType(thermMode.name());
                    case OFF:
                    case MAX:
                    case UNKNOWN:
                        return UnDefType.UNDEF;
                }
                return null;
            case CHANNEL_SETPOINT_END_TIME:
                switch (thermMode) {
                    case PROGRAM:
                    case HOME:
                    case SCHEDULE:
                        return toDateTimeType(getNextProgramTime(localThing.getActiveProgram()), zoneId);
                    case AWAY:
                    case MANUAL:
                    case FROST_GUARD:
                        return toDateTimeType(localThing.getThermModeEndTime());
                    case OFF:
                    case MAX:
                    case UNKNOWN:
                        return UnDefType.UNDEF;
                }
        }
        return null;
    }
}
