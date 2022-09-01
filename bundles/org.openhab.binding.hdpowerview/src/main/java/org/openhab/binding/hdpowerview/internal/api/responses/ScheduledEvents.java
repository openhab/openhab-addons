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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * State of all Scheduled Events in an HD PowerView hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ScheduledEvents {

    public static final EnumSet<DayOfWeek> WEEKDAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    public static final EnumSet<DayOfWeek> WEEKENDS = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    public static final int SCHEDULED_EVENT_TYPE_TIME = 0;
    public static final int SCHEDULED_EVENT_TYPE_SUNRISE = 1;
    public static final int SCHEDULED_EVENT_TYPE_SUNSET = 2;

    public @Nullable List<ScheduledEvent> scheduledEventData;
    public @Nullable List<Integer> scheduledEventIds;
}
