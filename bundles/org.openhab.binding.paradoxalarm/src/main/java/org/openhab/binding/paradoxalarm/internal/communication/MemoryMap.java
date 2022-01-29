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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link MemoryMap} this keeps Paradox RAM map as cached object inside the communicator.
 * Every record in the list is byte array which contains 64 byte RAM page.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class MemoryMap {
    private List<byte[]> ramCache = new ArrayList<>();

    public MemoryMap(List<byte[]> ramCache) {
        this.ramCache = ramCache;
    }

    public List<byte[]> getRamCache() {
        return ramCache;
    }

    public void setRamCache(List<byte[]> ramCache) {
        this.ramCache = ramCache;
    }

    public synchronized byte[] getElement(int index) {
        return ramCache.get(index);
    }

    public synchronized void updateElement(int index, byte[] elementValue) {
        ramCache.set(index - 1, elementValue);
    }
}
