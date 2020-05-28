/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.blinds.action.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.blinds.action.internal.util.History;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class BrightnessHistory {

    private final Map<String, History> history = new HashMap<>();

    public synchronized void assureItemRegistered(@NonNull String itemName, long startTime) {
        if (!history.containsKey(itemName)) {
            history.put(itemName, new History(startTime, 120));
        }
    }

    public synchronized void add(@NonNull String itemName, int value) {
        if (!history.containsKey(itemName)) {
            return;
        }

        history.get(itemName).add(value);
    }

    public synchronized Optional<History> getHistory(@NonNull String itemName) {
        History result = history.get(itemName);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public synchronized void nextSlot(long timestamp) {
        history.forEach((k, v) -> v.nextSlot(timestamp));
    }

    public synchronized void clear() {
        history.clear();
    }
}
