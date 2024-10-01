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
package org.openhab.binding.argoclima.internal.configuration;

import java.time.LocalTime;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.types.Weekday;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;

/**
 * Interface for schedule provider
 * The device (its remote) supports 3 schedules, so the same is implemented herein.
 * <p>
 * Noteworthy, the device itself (when communicated-to) only takes the type of timer (schedule) and on/off times +
 * weekdays, so technically number of schedules supported may be expanded beyond 3
 *
 * @implNote Only one schedule may be active at a time. Currently implemented through config, as it is easier to edit
 *           this way. Note that delay timer is instead implemented as a channel!
 *
 * @implNote While the boilerplate can be reduced, config-side these are modeled as individual properties (easier to
 *           edit), hence not doing anything fancy here
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public interface IScheduleConfigurationProvider {
    /**
     * The type of schedule timer (1|2|3)
     *
     * @author Mateusz Bronk - Initial contribution
     */
    public enum ScheduleTimerType {
        SCHEDULE_1,
        SCHEDULE_2,
        SCHEDULE_3;

        public static ScheduleTimerType fromInt(int value) {
            switch (value) {
                case 1:
                    return SCHEDULE_1;
                case 2:
                    return SCHEDULE_2;
                case 3:
                    return SCHEDULE_3;
                default:
                    throw new IllegalArgumentException(String.format("Invalid value for ScheduleTimerType: %d", value));
            }
        }
    }

    /**
     * The days of week when schedule shall be active
     *
     * @param scheduleType Which schedule timer to target (1|2|3)
     * @return The configured value
     * @throws ArgoConfigurationException In case of configuration error
     */
    public EnumSet<Weekday> getScheduleDayOfWeek(ScheduleTimerType scheduleType) throws ArgoConfigurationException;

    /**
     * The time of day schedule 1 shall turn the AC on
     *
     * @param scheduleType Which schedule timer to target (1|2|3)
     * @return The configured value
     * @throws ArgoConfigurationException In case of configuration error
     */
    public LocalTime getScheduleOnTime(ScheduleTimerType scheduleType) throws ArgoConfigurationException;

    /**
     * The time of day schedule 1 shall turn the AC off
     *
     * @param scheduleType Which schedule timer to target (1|2|3)
     * @return The configured value
     * @throws ArgoConfigurationException In case of configuration error
     */
    public LocalTime getScheduleOffTime(ScheduleTimerType scheduleType) throws ArgoConfigurationException;
}
