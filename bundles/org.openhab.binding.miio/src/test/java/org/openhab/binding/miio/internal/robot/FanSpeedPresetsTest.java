/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.robot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.types.StateOption;

/**
 * Test case for {@link FanSpeedPresets}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class FanSpeedPresetsTest {

    @Test
    public void modelSelectionTest() {
        assertEquals(FanSpeedPresets.E2, FanSpeedPresets.forModel("roborock.vacuum.e2"));
        assertEquals(FanSpeedPresets.E2, FanSpeedPresets.forModel("ROBOROCK.vacuum.E2"));
        assertEquals(FanSpeedPresets.DEFAULT, FanSpeedPresets.forModel("roborock.vacuum.s5"));
        assertEquals(FanSpeedPresets.DEFAULT, FanSpeedPresets.forModel("rockrobo.vacuum.v1"));
    }

    @Test
    public void e2DecodingTest() {
        FanSpeedPresets e2 = FanSpeedPresets.E2;
        for (int value : new int[] { 41, 50, 68, 79, 100 }) {
            assertEquals(value, e2.getValue(value));
        }
        assertEquals("Gentle", e2.getLabel(41));
        assertEquals("Silent", e2.getLabel(50));
        assertEquals("Strong", e2.getLabel(79));
        // default presets are not E2 presets
        assertEquals(FanSpeedPresets.CUSTOM_VALUE, e2.getValue(38));
        assertEquals(FanSpeedPresets.CUSTOM_VALUE, e2.getValue(60));
        assertEquals(FanSpeedPresets.CUSTOM_LABEL, e2.getLabel(60));
    }

    @Test
    public void defaultDecodingUnchangedTest() {
        FanSpeedPresets def = FanSpeedPresets.DEFAULT;
        for (int value : new int[] { 38, 60, 75, 77, 90, 100, 101, 102, 103, 104, 105, 106, 108 }) {
            assertEquals(value, def.getValue(value));
        }
        assertEquals("Silent", def.getLabel(38));
        assertEquals(FanSpeedPresets.CUSTOM_VALUE, def.getValue(50));
        assertEquals(FanSpeedPresets.CUSTOM_VALUE, def.getValue(0));
    }

    @Test
    public void e2OptionsSendE2ValuesTest() {
        List<StateOption> options = FanSpeedPresets.E2.getOptions();
        assertEquals(List.of("41", "50", "68", "79", "100", "-1"),
                options.stream().map(StateOption::getValue).toList());
        assertEquals("Silent", options.stream().filter(o -> "50".equals(o.getValue())).findFirst().get().getLabel());
    }
}
