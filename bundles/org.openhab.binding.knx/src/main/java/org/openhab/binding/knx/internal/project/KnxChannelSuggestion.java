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
package org.openhab.binding.knx.internal.project;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One piece of channel text for a group address or for one object of a device.
 *
 * @param groupAddress the group address the text is for, the one the channel writes to
 * @param key the channel configuration key: {@code ga}, or the key of the suggested channel type
 * @param value the value of the key, {@code [<dpt>:][<]G}
 * @param channelType the suggested channel type id, {@code null} when the table has no row for the type
 * @param label what the text is for: the name of the address, or {@code <number> – <name> – <function text>}
 * @param groupObject the object of a device entry, {@code null} for a group address entry
 * @param notes why the text is incomplete, empty when it is not
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxChannelSuggestion(KnxGroupAddress groupAddress, String key, String value, @Nullable String channelType,
        String label, @Nullable KnxGroupObject groupObject, List<String> notes) {

    public KnxChannelSuggestion {
        notes = List.copyOf(notes);
    }

    /** The configuration line, {@code key="value"}. */
    public String text() {
        return key + "=\"" + value + "\"";
    }
}
