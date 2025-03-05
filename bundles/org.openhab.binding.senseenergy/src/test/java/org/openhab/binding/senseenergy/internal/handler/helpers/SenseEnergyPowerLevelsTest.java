/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.handler.helpers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.List;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;

/**
 * {@link SenseEnergyPowerLevesTest }
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
class SenseEnergyPowerLevelsTest {
    SenseEnergyPowerLevels powerLevels = new SenseEnergyPowerLevels();

    @SuppressWarnings("null")
    void testParse(String s, int itemsLevel, int itemsState, String pretty, @Nullable List<Object> tests) {
        QuantityType<Power> qtResult;
        float result;

        powerLevels.parse(s);
        Assertions.assertEquals(itemsLevel, powerLevels.getNumValueLevels(), "Wrong number of Value Levels");
        Assertions.assertEquals(itemsState, powerLevels.getNumStateLevels(), "Wrong number of State Levels");

        String levelsPretty = powerLevels.toString();
        Assertions.assertEquals(pretty, levelsPretty, "toString format differs");

        if (tests == null) {
            return;
        }
        Iterator<Object> iter = tests.iterator();
        while (iter.hasNext()) {
            qtResult = null;
            Object value = iter.next();
            if (value instanceof String) {
                qtResult = powerLevels.getLevel((String) value);
            } else if (value instanceof Integer) {
                qtResult = powerLevels.getLevel((int) value);
            }

            Assertions.assertNotNull(qtResult, "getLevel result is null: " + s);

            result = qtResult.floatValue();
            float expected = (float) iter.next();
            Assertions.assertEquals(expected, result, .0001, "getLevel result incorrect");
        }
    }

    @SuppressWarnings("null")
    @Test
    void test() {
        // Valid input
        testParse("5", 1, 0, "5 W", List.of("ON", 5f, 100, 5f, "OFF", 0f, 0, 0f, 50, 2.5f));
        testParse("2W", 1, 0, "2 W", List.of("ON", 2f, 100, 2f, "OFF", 0f, 0, 0f, 50, 1f));
        testParse(".5,2W", 2, 0, "0.5 W,2 W", List.of("ON", 2f, "OFF", .5f));
        testParse("  0 ,  1.5mW  ,  3W , 2.5mW, 5W   ", 5, 0, "0 W,1.5 mW,2.5 mW,3 W,5 W",
                List.of(0, 0f, 100, 5f, 25, .0015f, 50, .0025f));
        testParse("OFF=0, LOW=2W, HIGH=5W,3W", 1, 3, "3 W,OFF=0 W,LOW=2 W,HIGH=5 W",
                List.of("OFF", 0f, "LOW", 2f, "HIGH", 5f));

        // Invalid input
        assertThrows(Exception.class, () -> powerLevels.parse("Hello"));
        assertThrows(Exception.class, () -> powerLevels.parse("ON="));
    }
}
