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
package org.openhab.binding.pegelonline.internal.config;

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PegelOnlineConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PegelOnlineConfiguration {
    public String uuid = UNKNOWN;
    public int warningLevel1 = Integer.MAX_VALUE;
    public int warningLevel2 = Integer.MAX_VALUE;
    public int warningLevel3 = Integer.MAX_VALUE;
    public int hq10 = Integer.MAX_VALUE;
    public int hq100 = Integer.MAX_VALUE;
    public int hqExtreme = Integer.MAX_VALUE;
    public int refreshInterval = 15;

    public boolean uuidCheck() {
        // https://stackoverflow.com/questions/20041051/how-to-judge-a-string-is-uuid-type
        return uuid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    /**
     * Check if configured warning levels are in ascending order
     *
     * @return true if ascending, false otherwise
     */
    public boolean warningCheck() {
        TreeMap<Integer, Integer> warnMap = this.getWarnings();
        Entry<Integer, Integer> currentEntry = warnMap.firstEntry();
        Entry<Integer, Integer> nextEntry = warnMap.higherEntry(currentEntry.getKey());
        while (nextEntry != null) {
            // ignore non configured values
            if (nextEntry.getKey() != Integer.MAX_VALUE) {
                if (nextEntry.getValue() < currentEntry.getValue()) {
                    return false;
                }
            }
            currentEntry = nextEntry;
            nextEntry = warnMap.higherEntry(currentEntry.getKey());
        }
        return true;
    }

    /**
     * Calculate sorted map with level height and warning level based on configuration
     *
     * @return TreeMap with keys containing level height and values containing warning level
     */
    public TreeMap<Integer, Integer> getWarnings() {
        TreeMap<Integer, Integer> warnMap = new TreeMap<>();
        warnMap.put(0, NO_WARNING);
        warnMap.put(warningLevel1, WARN_LEVEL_1);
        warnMap.put(warningLevel2, WARN_LEVEL_2);
        warnMap.put(warningLevel3, WARN_LEVEL_3);
        warnMap.put(hq10, HQ10);
        warnMap.put(hq100, HQ100);
        warnMap.put(hqExtreme, HQ_EXTREME);
        return warnMap;
    }
}
