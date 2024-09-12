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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Cache of Warnings to update the
 * {@link org.openhab.binding.dwdunwetter.internal.DwdUnwetterBindingConstants#CHANNEL_UPDATED}
 * if a new warning is sent to a channel.
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdWarningCache {

    // Remove Entries 30 Minutes after they expired
    private static final long WAIT_TIME_IN_MINUTES = 30;

    private final Map<String, Instant> idExpiresMap;

    public DwdWarningCache() {
        idExpiresMap = new HashMap<>();
    }

    private boolean isExpired(Entry<String, Instant> entry) {
        Instant expireTime = entry.getValue().plus(WAIT_TIME_IN_MINUTES, ChronoUnit.MINUTES);
        return Instant.now().isAfter(expireTime);
    }

    /**
     * Adds a Warning
     *
     * @param data The warning data
     * @return <code>true</code> if it is a new warning, <code>false</code> if the warning is not new.
     */
    public boolean addEntry(DwdWarningData data) {
        return idExpiresMap.put(data.getId(), data.getExpires()) == null;
    }

    /**
     * Removes the expired Entries
     */
    public void deleteOldEntries() {
        List<String> oldEntries = idExpiresMap.entrySet().stream().filter(this::isExpired).map(Entry::getKey)
                .collect(Collectors.toList());
        oldEntries.forEach(idExpiresMap::remove);
    }
}
