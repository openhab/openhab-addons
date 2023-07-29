/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * sound category test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class SoundCategoryTest {
    @Test
    public void testConversion() {
        for (SoundCategory value : SoundCategory.values()) {
            assertEquals(value, SoundCategory.toEnum(value.toRaw()));
        }
    }

    @Test
    public void testInvalidRawValue() {
        assertNull(SoundCategory.toEnum("invalid raw value"));
    }

    @Test
    public void testNullRawValue() {
        assertNull(SoundCategory.toEnum(null));
    }
}
