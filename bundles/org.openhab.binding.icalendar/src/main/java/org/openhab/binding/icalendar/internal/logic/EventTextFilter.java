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
package org.openhab.binding.icalendar.internal.logic;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport class for a simple text filter.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
public class EventTextFilter {
    public enum Type {
        TEXT,
        REGEX
    }

    public enum Field {
        SUMMARY,
        DESCRIPTION,
        COMMENT,
        CONTACT,
        LOCATION
    }

    public Field field;
    public String value;
    public Type type;

    public EventTextFilter(Field field, String value, Type type) {
        this.field = field;
        this.value = value;
        this.type = type;
    }
}
