/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiaccess.internal.dto;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Holiday Group for Access Policy Schedules.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AccessPolicyHolidayGroup {
    public String id;
    public String name;
    public Boolean isDefault;
    public String description;
    public String templateName;
    public List<Holiday> holidays;

    public boolean isHoliday(ZonedDateTime when) {
        return activeHolidayAt(when) != null;
    }

    public Holiday activeHolidayAt(ZonedDateTime when) {
        Objects.requireNonNull(when, "when");
        if (holidays == null || holidays.isEmpty())
            return null;
        for (Holiday h : holidays) {
            if (h != null && h.contains(when))
                return h;
        }
        return null;
    }

    public static class Holiday {
        public String id;
        public String name;
        public String description;
        public Boolean repeat;
        public Boolean isTemplate;
        public String startTime;
        public String endTime;

        public Instant parsedStart() {
            return UaTime.parseInstant(startTime);
        }

        public Instant parsedEnd() {
            return UaTime.parseInstant(endTime);
        }

        public boolean contains(ZonedDateTime when) {
            final Instant s = parsedStart();
            final Instant e = parsedEnd();
            if (s == null || e == null || when == null) {
                return false;
            }
            final Instant x = when.toInstant();
            return !x.isBefore(s) && x.isBefore(e); // [start, end)
        }
    }
}
