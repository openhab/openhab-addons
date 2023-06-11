/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.client.model;

import static org.openhab.binding.homeconnect.internal.client.model.EventType.EVENT;
import static org.openhab.binding.homeconnect.internal.client.model.EventType.NOTIFY;
import static org.openhab.binding.homeconnect.internal.client.model.EventType.STATUS;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event model
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class Event {

    private final String haId;
    // event type
    private final EventType type;
    // event key
    private @Nullable final String key;
    // user-friendly name of the feature key
    private @Nullable final String name;
    // URI of the resource that changed
    private @Nullable final String uri;
    // creation time of event
    private @Nullable final ZonedDateTime creation;
    // level of the event
    private @Nullable final EventLevel level;
    // expected activity
    private @Nullable final EventHandling handling;
    // new value, e.g. in case of a status update (string, number or boolean)
    private @Nullable final String value;
    // unit string
    private @Nullable final String unit;

    public Event(final String haId, final EventType type) {
        this.haId = haId;
        this.type = type;
        this.key = null;
        this.name = null;
        this.uri = null;
        this.creation = ZonedDateTime.now();
        this.level = null;
        this.handling = null;
        this.value = null;
        this.unit = null;
    }

    public Event(final String haId, final EventType type, @Nullable final String key, @Nullable final String name,
            @Nullable final String uri, @Nullable final ZonedDateTime creation, @Nullable final EventLevel level,
            @Nullable final EventHandling handling, @Nullable final String value, @Nullable final String unit) {
        this.haId = haId;
        this.type = type;
        this.key = key;
        this.name = name;
        this.uri = uri;
        this.creation = creation;
        this.level = level;
        this.handling = handling;
        this.value = value;
        this.unit = unit;
    }

    public String getHaId() {
        return haId;
    }

    public EventType getType() {
        return type;
    }

    public @Nullable String getKey() {
        return key;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getUri() {
        return uri;
    }

    public @Nullable ZonedDateTime getCreation() {
        return creation;
    }

    public @Nullable EventLevel getLevel() {
        return level;
    }

    public @Nullable EventHandling getHandling() {
        return handling;
    }

    public @Nullable String getValue() {
        return value;
    }

    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(value);
    }

    public int getValueAsInt() {
        String stringValue = value;
        return stringValue != null ? Float.valueOf(stringValue).intValue() : 0;
    }

    public @Nullable String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        if (STATUS.equals(type) || EVENT.equals(type) || NOTIFY.equals(type)) {
            return "Event{" + "haId='" + haId + '\'' + ", type=" + type + ", key='" + key + '\'' + ", name='" + name
                    + '\'' + ", uri='" + uri + '\'' + ", creation=" + creation + ", level=" + level + ", handling="
                    + handling + ", value='" + value + '\'' + ", unit='" + unit + '\'' + '}';
        } else {
            return "Event{" + "haId='" + haId + '\'' + ", type=" + type + '}';
        }
    }
}
