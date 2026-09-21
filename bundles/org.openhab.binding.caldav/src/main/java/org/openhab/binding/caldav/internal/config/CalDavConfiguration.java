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
package org.openhab.binding.caldav.internal.config;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;

/**
 * Shared validation keeps text and UI configuration behavior identical.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
public final class CalDavConfiguration {
    private CalDavConfiguration() {
    }

    public static void validate(AccountConfiguration c) {
        CalDavUris.validate(URI.create(c.url));
        if (c.username.isBlank() != c.password.isBlank()) {
            throw new IllegalArgumentException("Username and password must both be provided or both be empty");
        }
        if (c.requestTimeout < 1 || c.requestTimeout > 300 || c.refreshInterval < 30 || c.maxPastDays < 0
                || c.maxFutureDays < 1 || c.maxPastDays > 36500 || c.maxFutureDays > 36500
                || !Set.of("AUTO", "BASIC", "DIGEST").contains(c.authType)
                || !Set.of("AUTO", "DIRECT").contains(c.discoveryMode)
                || !Set.of("AUTO", "FULL", "ETAG", "SYNC_TOKEN").contains(c.syncMode) || !c.readOnly) {
            throw new IllegalArgumentException("Invalid account settings; calendar writes are not supported");
        }
        if (!c.calendarHome.isBlank()) {
            CalDavUris.resolve(URI.create(c.url), c.calendarHome);
        }
    }

    public static URI validate(CalendarConfiguration c, AccountConfiguration a) {
        if (c.path.isBlank() || c.calendarId.isBlank() || c.maxEvents < 1 || c.maxEvents > 50000
                || c.rangeStartOffset > c.rangeEndOffset || !Set.of("TODAY", "NOW").contains(c.rangeAnchor)) {
            throw new IllegalArgumentException("Invalid calendar settings");
        }
        return CalDavUris.resolve(URI.create(a.url), c.path);
    }

    public static CalendarWindow horizon(AccountConfiguration a, ZoneId zone, ZonedDateTime now) {
        ZonedDateTime day = now.withZoneSameInstant(zone).toLocalDate().atStartOfDay(zone);
        return new CalendarWindow(day.minusDays(a.maxPastDays), day.plusDays(a.maxFutureDays + 1L));
    }

    public static void validate(CalendarWindow window, CalendarWindow horizon) {
        if (window.start().isBefore(horizon.start()) || window.end().isAfter(horizon.end())
                || !window.start().isBefore(window.end())) {
            throw new IllegalArgumentException("Calendar range must fit within the account synchronization horizon");
        }
    }
}
