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
 * The writable channels of one thing type, keyed by channel group and then channel id.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCCommandRegistry {

    private final Map<String, Map<String, E3DCCommandSpec>> byGroup = new LinkedHashMap<>();

    E3DCCommandRegistry group(String groupId, Map<String, E3DCCommandSpec> channels) {
        byGroup.put(groupId, channels);
        return this;
    }

    public Map<String, Map<String, E3DCCommandSpec>> groups() {
        return Collections.unmodifiableMap(byGroup);
    }

    public @Nullable E3DCCommandSpec spec(String groupId, String channelId) {
        Map<String, E3DCCommandSpec> channels = byGroup.get(groupId);
        return channels != null ? channels.get(channelId) : null;
    }

    public List<E3DCCommandSpec> all() {
        return byGroup.values().stream().flatMap(map -> map.values().stream()).distinct().toList();
    }
}
