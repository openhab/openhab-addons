/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SlidingTimeWindow} is responsible for managing a set of
 * time-based values. This class is used to calculate trends over time.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
class SlidingTimeWindow<T> {
    protected long period = 0;
    protected final SortedMap<Long, T> storage = Collections.synchronizedSortedMap(new TreeMap<>());

    /**
     * Create a sliding time window for the provided time period
     */
    public SlidingTimeWindow(long period) {
        this.period = period;
    }

    public void put(T value) {
        storage.put(System.currentTimeMillis(), value);
    }

    public void removeOldEntries() {
        long old = System.currentTimeMillis() - period;
        synchronized (storage) {
            for (Iterator<Long> iterator = storage.keySet().iterator(); iterator.hasNext();) {
                long time = iterator.next();
                if (time < old) {
                    iterator.remove();
                }
            }
        }
    }
}
