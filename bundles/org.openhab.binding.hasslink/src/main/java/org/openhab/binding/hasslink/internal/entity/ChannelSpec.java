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
package org.openhab.binding.hasslink.internal.entity;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Metadata specification for dynamically constructing openHAB channels from Home Assistant entity attributes.
 *
 * @param attribute openHAB attribute key (or {@link EntityType#PRIMARY_ATTR})
 * @param itemType target openHAB {@link ItemType}
 * @param kind openHAB {@link ChannelKind} (STATE or TRIGGER)
 * @param label optional channel label override
 * @param description optional channel description
 * @param defaultTags default semantic tags (e.g., "Status", "Control", "Event")
 * @param autoUpdatePolicy openHAB auto-update policy override (or null for default)
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record ChannelSpec( //
        String attribute, //
        ItemType itemType, //
        ChannelKind kind, //
        @Nullable String label, //
        @Nullable String description, //
        Set<String> defaultTags, //
        @Nullable AutoUpdatePolicy autoUpdatePolicy) {

    public ChannelSpec {
        defaultTags = Set.copyOf(defaultTags);
    }

    /**
     * Secondary constructor defaulting to ChannelKind.STATE for standard channels.
     */
    public ChannelSpec(String attribute, ItemType itemType, @Nullable String label, @Nullable String description,
            Set<String> defaultTags, @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        this(attribute, itemType, ChannelKind.STATE, label, description, defaultTags, autoUpdatePolicy);
    }
}
