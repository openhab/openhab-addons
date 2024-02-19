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
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Enumeration of channel kinds (item or trigger)
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public enum NeeoDeviceChannelKind {
    /** Represents an item */
    ITEM("item"),
    /** Represents a trigger item */
    TRIGGER("trigger");

    /** The text value of the enum */
    private final String text;

    /**
     * Constructs the NeeoDeviceChannelKind using the specified text
     *
     * @param text the text
     */
    private NeeoDeviceChannelKind(final String text) {
        Objects.requireNonNull(text, "text is required");
        this.text = text;
    }

    /**
     * Parses the text into a NeeoDeviceChannelKind enum (ignoring case)
     *
     * @param text the text to parse
     * @return the NeeoDeviceChannelKind type
     */
    public static NeeoDeviceChannelKind parse(final String text) {
        if (text.isEmpty()) {
            return ITEM;
        }
        for (NeeoDeviceChannelKind enm : NeeoDeviceChannelKind.values()) {
            if (text.equalsIgnoreCase(enm.text)) {
                return enm;
            }
        }

        return ITEM;
    }

    /**
     * Returns the {@link NeeoDeviceChannelKind} for the given {@link ChannelKind}
     *
     * @param kind a non-null {@link ChannelKind}
     * @return a non-null {@link NeeoDeviceChannelKind}
     */
    public static NeeoDeviceChannelKind get(ChannelKind kind) {
        Objects.requireNonNull(kind, "kind cannot be null");
        return kind == ChannelKind.TRIGGER ? TRIGGER : ITEM;
    }

    @Override
    public String toString() {
        return text;
    }
}
