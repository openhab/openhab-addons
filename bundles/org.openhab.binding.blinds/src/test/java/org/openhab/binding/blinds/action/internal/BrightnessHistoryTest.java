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

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;
import org.openhab.binding.blinds.action.internal.util.History;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class BrightnessHistoryTest {

    @Test
    public void testHistoryAvailability() {
        BrightnessHistory history = new BrightnessHistory();
        Optional<History> itemHistoryOptional = history.getHistory("testItem");

        assertFalse("No history should be available as the item was never registered", itemHistoryOptional.isPresent());

        history.add("testItem", 10);
        itemHistoryOptional = history.getHistory("testItem");
        assertFalse("No history should be available as the item was never registered", itemHistoryOptional.isPresent());

        history.assureItemRegistered("testItem", 1);
        itemHistoryOptional = history.getHistory("testItem");

        assertTrue(itemHistoryOptional.isPresent());
    }

    @Test
    public void testHistory() {
        BrightnessHistory history = new BrightnessHistory();

        history.assureItemRegistered("testItem", 1);
        Optional<History> itemHistoryOptional = history.getHistory("testItem");

        assertTrue(itemHistoryOptional.isPresent());
        History itemHistory = itemHistoryOptional.get();

        Optional<Integer> result = itemHistory.getMaximumSince(0);
        assertFalse("No maximum since 0 available as the history does not contain data since 0", result.isPresent());

        itemHistory.add(10);
        result = itemHistory.getMaximumSince(0);
        assertFalse("No maximum since 0 available as the history does not contain data since 0", result.isPresent());

        result = itemHistory.getMinimumSince(0);
        assertFalse("No minimum since 0 available as the history does not contain data since 0", result.isPresent());

        result = itemHistory.getMaximumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(10), result.get());

        result = itemHistory.getMinimumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(10), result.get());

        itemHistory.add(1);

        result = itemHistory.getMaximumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(10), result.get());

        result = itemHistory.getMinimumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(1), result.get());

        // moving to timeslot 2. without adding any data we should have only the last value in slot 2
        history.nextSlot(2);
        result = itemHistory.getMaximumSince(2);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(1), result.get());

        result = itemHistory.getMinimumSince(2);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(1), result.get());

        // maximum and minimum from slot 1 should be the same
        result = itemHistory.getMaximumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(10), result.get());

        result = itemHistory.getMinimumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(1), result.get());

        itemHistory.add(0);
        result = itemHistory.getMaximumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(10), result.get());

        result = itemHistory.getMinimumSince(1);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(0), result.get());

        // adding lots of new timeslots, afterwards we should not get a result anymore when querying from 1

        for (int i = 3; i < 500; i++) {
            history.nextSlot(i);
        }

        result = itemHistory.getMaximumSince(0);
        assertFalse("No maximum since 0 available as the history does not contain data since 0", result.isPresent());

        result = itemHistory.getMinimumSince(0);
        assertFalse("No minimum since 0 available as the history does not contain data since 0", result.isPresent());

        // the current maximum should be 0 as this was the last value that was propagated
        result = itemHistory.getMaximumSince(490);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(0), result.get());

        result = itemHistory.getMinimumSince(490);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(0), result.get());

        itemHistory.add(5);
        result = itemHistory.getMaximumSince(490);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(5), result.get());

        result = itemHistory.getMinimumSince(490);
        assertTrue(result.isPresent());
        assertEquals(Integer.valueOf(0), result.get());
    }

}
