/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.caldav.internal.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public record CalendarEvent(String uid, @Nullable String recurrenceId, String title, String description,
        String location, @Nullable ZonedDateTime start, @Nullable ZonedDateTime end, @Nullable LocalDate allDayStart,
        @Nullable LocalDate allDayEnd, boolean allDay, String status, List<String> categories, String organizer) {
    public String instanceId() {
        String occurrence = recurrenceId;
        if (occurrence == null) {
            occurrence = allDay ? String.valueOf(allDayStart) : String.valueOf(start);
        }
        return uid + "|" + occurrence;
    }
}
