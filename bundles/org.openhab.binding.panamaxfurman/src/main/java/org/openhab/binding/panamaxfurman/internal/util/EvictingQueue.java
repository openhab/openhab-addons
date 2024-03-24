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
package org.openhab.binding.panamaxfurman.internal.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Fixed length FIFO queue
 *
 * @author Dave Badia - Initial contribution
 *
 */
@NonNullByDefault
public class EvictingQueue<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 7499624698517766887L;
    private int fixedSize;

    /**
     * Constructor
     *
     * @param fixedSize the maximum size of the queue
     */
    public EvictingQueue(int fixedSize) {
        this.fixedSize = fixedSize;
    }

    /**
     * Returns {@code true} if this map should remove its eldest entry.
     */
    @Override
    protected boolean removeEldestEntry(Map.@Nullable Entry<K, V> eldest) {
        return this.size() > fixedSize;
    }
}
