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

package org.openhab.binding.blinds.action.internal.util;

import java.util.Iterator;
import java.util.Optional;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class History {
    private final RingBuffer<Timeslot> history;

    public History(long startTime, int slots) {
        history = new RingBuffer<>(slots);

        // history is never empty, we always start with a new timeslot
        history.add(new Timeslot(startTime));
    }

    public synchronized void add(int value) {
        history.getLast().add(value);
    }

    public synchronized void nextSlot(long startTime) {
        history.add(new Timeslot(startTime, history.getLast()));
    }

    public synchronized Optional<Integer> getMaximumSince(long timestamp) {
        if (history.getFirst().getStartTime() > timestamp) {
            return Optional.empty();
        }

        boolean allSlotsHaveData = true;

        // iterate from the newest entry to the oldest
        int max = Integer.MIN_VALUE;
        Iterator<Timeslot> iterator = history.backwardIterator();
        while (iterator.hasNext()) {
            Timeslot item = iterator.next();
            max = Math.max(max, item.getStatCounter().getMax());

            if (!item.getStatCounter().hasValue()) {
                allSlotsHaveData = false;
                break;
            }

            if (item.getStartTime() <= timestamp) {
                break;
            }
        }

        if (allSlotsHaveData) {
            return Optional.of(max);
        } else {
            return Optional.empty();
        }
    }

    public synchronized Optional<Integer> getMinimumSince(long timestamp) {
        if (history.getFirst().getStartTime() > timestamp) {
            return Optional.empty();
        }

        boolean allSlotsHaveData = true;

        // iterate from the newest entry to the oldest
        int min = Integer.MAX_VALUE;
        Iterator<Timeslot> iterator = history.backwardIterator();
        while (iterator.hasNext()) {
            Timeslot item = iterator.next();
            min = Math.min(min, item.getStatCounter().getMin());

            if (!item.getStatCounter().hasValue()) {
                allSlotsHaveData = false;
                break;
            }

            if (item.getStartTime() <= timestamp) {
                break;
            }
        }

        if (allSlotsHaveData) {
            return Optional.of(min);
        } else {
            return Optional.empty();
        }
    }

}
