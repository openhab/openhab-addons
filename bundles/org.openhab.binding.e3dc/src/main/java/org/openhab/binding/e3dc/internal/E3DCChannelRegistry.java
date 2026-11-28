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
package org.openhab.binding.e3dc.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The channel specifications of one thing type, grouped by channel group.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCChannelRegistry {

    private final Map<String, Map<String, E3DCChannelSpec>> byGroup = new LinkedHashMap<>();

    E3DCChannelRegistry group(String groupId, Map<String, E3DCChannelSpec> channels) {
        byGroup.put(groupId, channels);
        return this;
    }

    /**
     * The channels of one group, keyed by the id inside the group.
     */
    public Map<String, E3DCChannelSpec> channels(String groupId) {
        Map<String, E3DCChannelSpec> channels = byGroup.get(groupId);
        return channels != null ? channels : Map.of();
    }

    /**
     * All groups with their channels, keyed by the group id.
     */
    public Map<String, Map<String, E3DCChannelSpec>> groups() {
        return Collections.unmodifiableMap(byGroup);
    }

    /**
     * Every channel specification of this registry.
     */
    public List<E3DCChannelSpec> all() {
        return byGroup.values().stream().flatMap(map -> map.values().stream()).distinct().toList();
    }

    /**
     * The specification of one channel, or null when the group or the channel is unknown.
     */
    public @Nullable E3DCChannelSpec spec(String groupId, String channelId) {
        Map<String, E3DCChannelSpec> channels = byGroup.get(groupId);
        return channels != null ? channels.get(channelId) : null;
    }

    /**
     * The containers the channels of this registry read from.
     */
    public List<Integer> containers() {
        return all().stream().map(E3DCChannelSpec::container).filter(container -> container != 0).distinct().toList();
    }
}
