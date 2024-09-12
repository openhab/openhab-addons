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
package org.openhab.binding.lametrictime.internal.api.dto.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * brightness mode test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BrightnessModeTest {
    @Test
    public void testConversion() {
        for (BrightnessMode value : BrightnessMode.values()) {
            assertEquals(value, BrightnessMode.toEnum(value.toRaw()));
        }
    }

    @Test
    public void testInvalidRawValue() {
        assertNull(BrightnessMode.toEnum("invalid raw value"));
    }

    @Test
    public void testNullRawValue() {
        assertNull(BrightnessMode.toEnum(null));
    }
}
