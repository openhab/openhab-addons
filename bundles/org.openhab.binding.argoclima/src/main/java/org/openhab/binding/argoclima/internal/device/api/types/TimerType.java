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
package org.openhab.binding.argoclima.internal.device.api.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.configuration.IScheduleConfigurationProvider.ScheduleTimerType;

/**
 * Type representing Argo currently selected timer. Int values are matching device's API.
 * <p>
 * The device supports a "delay" timer + 3 configurable "schedule" timers. All the schedule timers share the same API
 * fields for configuring days of week when they are active as well as start/stop time
 *
 * @see ScheduleTimerType - for schedule-specific enum (used in binding configuration)
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public enum TimerType implements IArgoApiEnum {
    NO_TIMER(0),
    DELAY_TIMER(1),
    SCHEDULE_TIMER_1(2),
    SCHEDULE_TIMER_2(3),
    SCHEDULE_TIMER_3(4);

    private int value;

    TimerType(int intValue) {
        this.value = intValue;
    }

    @Override
    public int getIntValue() {
        return this.value;
    }

    /**
     * Converts to {@link ScheduleTimerType}
     *
     * @implNote This function will throw, if passed a non-schedule-timer type. Not using optional response, given its
     *           simple usage and extra boilerplate it would do. Needs care when being used though!
     * @param val Value to convert
     * @return Converted value
     * @throws IllegalArgumentException - on passing a timer which is not one of schedule timers
     */
    public static ScheduleTimerType toScheduleTimerType(TimerType val) {
        switch (val) {
            case SCHEDULE_TIMER_1:
                return ScheduleTimerType.SCHEDULE_1;
            case SCHEDULE_TIMER_2:
                return ScheduleTimerType.SCHEDULE_2;
            case SCHEDULE_TIMER_3:
                return ScheduleTimerType.SCHEDULE_3;
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to convert TimerType: %s to ScheduleTimerType", val));
        }
    }

    /**
     * Converts from {@link ScheduleTimerType}
     *
     * @param val Value to convert
     * @return Converted value
     * @throws IllegalArgumentException - on passing an out-of-range enum (extremely unlikely!)
     */
    public static TimerType fromScheduleTimerType(ScheduleTimerType val) {
        switch (val) {
            case SCHEDULE_1:
                return TimerType.SCHEDULE_TIMER_1;
            case SCHEDULE_2:
                return TimerType.SCHEDULE_TIMER_2;
            case SCHEDULE_3:
                return TimerType.SCHEDULE_TIMER_3;
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to convert ScheduleTimerType: %s to TimerType", val));
        }
    }
}
