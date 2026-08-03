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
package org.openhab.binding.matter.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PercentType;

/**
 * Unit tests for {@link ValueUtils}.
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
class ValueUtilsTest {

    @Test
    void testLevelToPercentZeroAndOne() {
        assertEquals(PercentType.ZERO, ValueUtils.levelToPercent(0));
        assertEquals(new PercentType(1), ValueUtils.levelToPercent(1));
    }

    @Test
    void testLevelToPercentLowLevelsMapToOnePercent() {
        // Levels 2, 3, 4, 5 (representing ~0.78% to 1.96%) MUST map to 1% instead of rounding up to 2%
        assertEquals(new PercentType(1), ValueUtils.levelToPercent(2));
        assertEquals(new PercentType(1), ValueUtils.levelToPercent(3));
        assertEquals(new PercentType(1), ValueUtils.levelToPercent(4));
        assertEquals(new PercentType(1), ValueUtils.levelToPercent(5));
    }

    @Test
    void testLevelToPercentMidAndMaxLevels() {
        assertEquals(new PercentType(2), ValueUtils.levelToPercent(6));
        assertEquals(new PercentType(50), ValueUtils.levelToPercent(127));
        assertEquals(new PercentType(100), ValueUtils.levelToPercent(254));
    }

    @Test
    void testPercentToLevel() {
        assertEquals(0, ValueUtils.percentToLevel(PercentType.ZERO));
        assertEquals(3, ValueUtils.percentToLevel(new PercentType(1)));
        assertEquals(127, ValueUtils.percentToLevel(new PercentType(50)));
        assertEquals(254, ValueUtils.percentToLevel(new PercentType(100)));
    }
}
