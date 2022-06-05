/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

/**
 * Fixed length FIFO queue
 *
 * @author Dave Badia - Initial contribution
 *
 */
// Can't use @NonNullByDefault here, causes compilation errors
public class EvictingQueue<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 7499624698517766887L;
    private int fixedSize;

    public EvictingQueue(int fixedSize) {
        this.fixedSize = fixedSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > fixedSize;
    }
}
