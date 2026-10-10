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
package org.openhab.binding.caldav.internal.logic;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.config.CalendarConfiguration;

@NonNullByDefault
public record CalendarWindow(ZonedDateTime start, ZonedDateTime end) {
    public static CalendarWindow from(CalendarConfiguration configuration, ZoneId zone, ZonedDateTime now) {
        ZonedDateTime anchor = "NOW".equalsIgnoreCase(configuration.rangeAnchor) ? now
                : now.withZoneSameInstant(zone).toLocalDate().atStartOfDay(zone);
        return new CalendarWindow(anchor.plusDays(configuration.rangeStartOffset),
                anchor.plusDays(configuration.rangeEndOffset + 1L));
    }
}
