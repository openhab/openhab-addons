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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAThermProgram;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NATimeTableItem;
import org.openhab.binding.netatmo.internal.api.dto.NAZone;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link HomeEnergyChannelHelper} handles specific channels of thermostat settings at home level.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeEnergyChannelHelper extends ChannelHelper {

    public HomeEnergyChannelHelper() {
        super(GROUP_ENERGY);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing data, Configuration config) {
        if (data instanceof NAHomeData) {
            NAHomeData homeData = (NAHomeData) data;
            SetpointMode thermMode = homeData.getThermMode();
            NAThermProgram currentProgram = homeData.getActiveProgram();
            switch (channelId) {
                case CHANNEL_SETPOINT_DURATION:
                    return toQuantityType(homeData.getThermSetpointDefaultDuration(), Units.MINUTE);
                case CHANNEL_PLANNING:
                    return (currentProgram != null ? toStringType(currentProgram.getName()) : null);
                case CHANNEL_SETPOINT_END_TIME:
                    switch (thermMode) {
                        case PROGRAM:
                        case HOME:
                        case SCHEDULE:
                            return currentProgram != null ? toDateTimeType(nextProgramTime(currentProgram))
                                    : UnDefType.UNDEF;
                        default:
                            return UnDefType.UNDEF;
                    }
                case CHANNEL_SETPOINT_MODE:
                    switch (thermMode) {
                        case OFF:
                        case MAX:
                        case UNKNOWN:
                            return UnDefType.UNDEF;
                        case PROGRAM:
                        case HOME:
                        case SCHEDULE:
                            if (currentProgram != null) {
                                NATimeTableItem currentProgramMode = currentProgramMode(currentProgram);
                                if (currentProgramMode != null) {
                                    NAZone zone = currentProgram
                                            .getZone(String.valueOf(currentProgramMode.getZoneId()));
                                    if (zone != null) {
                                        return new StringType(zone.getName());
                                    }
                                }
                            }
                            return UnDefType.NULL;
                        default:
                            return toStringType(thermMode);
                    }
            }
        }
        return null;
    }

    private static ZonedDateTime programBaseTimeZdt() {
        return ZonedDateTime.now().with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
    }

    private static long minutesSinceProgramBaseTime() {
        return ChronoUnit.MINUTES.between(programBaseTimeZdt(), ZonedDateTime.now());
    }

    private static @Nullable NATimeTableItem currentProgramMode(NAThermProgram activeProgram) {
        long diff = minutesSinceProgramBaseTime();
        return activeProgram.getTimetable().stream().filter(t -> t.getMinuteOffset() < diff)
                .reduce((first, second) -> second).orElse(null);
    }

    private static ZonedDateTime nextProgramTime(NAThermProgram activeProgram) {
        long diff = minutesSinceProgramBaseTime();
        // By default we'll use the first slot of next week - this case will be true if
        // we are in the last schedule of the week so below loop will not exit by break
        List<NATimeTableItem> timetable = activeProgram.getTimetable();
        int next = timetable.get(0).getMinuteOffset() + (7 * 24 * 60);
        for (NATimeTableItem timeTable : timetable) {
            if (timeTable.getMinuteOffset() > diff) {
                next = timeTable.getMinuteOffset();
                break;
            }
        }
        return programBaseTimeZdt().plusMinutes(next);
    }
}
