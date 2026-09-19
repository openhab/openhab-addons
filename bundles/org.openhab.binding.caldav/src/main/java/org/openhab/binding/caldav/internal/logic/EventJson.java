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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Serializes the public event contract without exposing implementation fields.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Complete JSON escaping
 */
@NonNullByDefault
public final class EventJson {
    private static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

    private EventJson() {
    }

    public static String serialize(List<CalendarEvent> events) {
        JsonArray result = new JsonArray();
        for (CalendarEvent event : events) {
            JsonObject item = new JsonObject();
            item.addProperty("instanceId", event.instanceId());
            item.addProperty("uid", event.uid());
            item.addProperty("recurrenceId", event.recurrenceId());
            item.addProperty("title", event.title());
            item.addProperty("description", event.description());
            item.addProperty("location", event.location());
            var timedStart = event.start();
            var timedEnd = event.end();
            var start = event.allDay() ? event.allDayStart()
                    : timedStart == null ? null : timedStart.toOffsetDateTime();
            var end = event.allDay() ? event.allDayEnd() : timedEnd == null ? null : timedEnd.toOffsetDateTime();
            if (start == null) {
                item.add("start", JsonNull.INSTANCE);
            } else {
                item.addProperty("start", start.toString());
            }
            if (end == null) {
                item.add("end", JsonNull.INSTANCE);
            } else {
                item.addProperty("end", end.toString());
            }
            item.addProperty("allDay", event.allDay());
            item.addProperty("status", event.status());
            item.add("categories", GSON.toJsonTree(event.categories()));
            item.addProperty("organizer", event.organizer());
            result.add(item);
        }
        return GSON.toJson(result);
    }
}
