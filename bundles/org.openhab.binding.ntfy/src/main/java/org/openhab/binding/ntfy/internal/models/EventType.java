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
package org.openhab.binding.ntfy.internal.models;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of supported event types returned by the ntfy service.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public enum EventType {
    /** A regular message event containing a notification message. */
    MESSAGE,

    /** An "open" event indicating the user opened the notification. */
    OPEN,

    /** A "delete" event indicating the user deleted the notification. */
    DELETE,

    /** Unknown or unsupported event type. */
    UNKNOWN;

    /**
     * Converts a string representation of an event type to the corresponding enum value.
     * The comparison is performed using the root locale to avoid locale dependent behavior.
     *
     * @param text the raw event text (may be null)
     * @return the matching {@link EventType} or {@link #UNKNOWN} if not recognised
     */
    public static EventType fromString(@Nullable String text) {
        if (text == null) {
            return UNKNOWN;
        }
        switch (text.toLowerCase(Locale.ROOT)) {
            case "message":
                return MESSAGE;
            case "open":
                return OPEN;
            case "message_delete":
                return DELETE;
            default:
                return UNKNOWN;
        }
    }
}
