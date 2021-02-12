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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.ThermostatZoneType;
import org.openhab.binding.netatmo.internal.api.dto.NAThermProgram;
import org.openhab.binding.netatmo.internal.api.dto.NAThermostat;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NATimeTableItem;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link Therm1SetpointChannelHelper} handle specific behavior
 * of the thermostat module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Therm1SetpointChannelHelper extends AbstractChannelHelper {

    public Therm1SetpointChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, GROUP_TH_SETPOINT);
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NAThermostat thermostat = (NAThermostat) naThing;
        switch (channelId) {
            case CHANNEL_VALUE:
                return getCurrentSetpoint(thermostat);
            case CHANNEL_SETPOINT_END_TIME:
                long endTime = thermostat.getSetpointEndtime();
                return toDateTimeType(endTime != 0 ? endTime : getNextProgramTime(thermostat.getActiveProgram()),
                        zoneId);
            case CHANNEL_SETPOINT_MODE:
                return new StringType(thermostat.getSetpointMode().name());
        }
        return null;
    }

    private State getCurrentSetpoint(NAThermostat thermostat) {
        SetpointMode currentMode = thermostat.getSetpointMode();
        NAThermProgram currentProgram = thermostat.getActiveProgram();
        switch (currentMode) {
            case PROGRAM:
                NATimeTableItem currentProgramMode = getCurrentProgramMode(thermostat.getActiveProgram());
                if (currentProgram != null && currentProgramMode != null) {
                    ThermostatZoneType zoneType = currentProgramMode.getZoneType();
                    return toQuantityType(currentProgram.getZoneTemperature(zoneType),
                            MeasureClass.INTERIOR_TEMPERATURE);
                }
            case AWAY:
            case FROST_GUARD:
                return toQuantityType(currentProgram != null ? currentProgram.getZoneTemperature(currentMode) : null,
                        MeasureClass.INTERIOR_TEMPERATURE);
            case MANUAL:
                return toQuantityType(thermostat.getSetpointTemp(), MeasureClass.INTERIOR_TEMPERATURE);
            case OFF:
            case MAX:
            case UNKNOWN:
                return UnDefType.UNDEF;
        }
        return UnDefType.NULL;
    }

    private @Nullable NATimeTableItem getCurrentProgramMode(@Nullable NAThermProgram activeProgram) {
        if (activeProgram != null) {
            long diff = getTimeDiff();
            return activeProgram.getTimetable().stream().filter(t -> t.getMOffset() < diff)
                    .reduce((first, second) -> second).orElse(null);
        }
        return null;
    }

    private long getNextProgramTime(@Nullable NAThermProgram activeProgram) {
        long diff = getTimeDiff();
        if (activeProgram != null) {
            // By default we'll use the first slot of next week - this case will be true if
            // we are in the last schedule of the week so below loop will not exit by break
            List<NATimeTableItem> timetable = activeProgram.getTimetable();
            int next = timetable.get(0).getMOffset() + (7 * 24 * 60);
            for (NATimeTableItem timeTable : timetable) {
                if (timeTable.getMOffset() > diff) {
                    next = timeTable.getMOffset();
                    break;
                }
            }
            return next * 60 + getProgramBaseTime();
        }
        return -1;
    }
}
