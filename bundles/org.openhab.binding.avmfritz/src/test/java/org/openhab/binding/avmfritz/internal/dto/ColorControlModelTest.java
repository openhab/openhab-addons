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
package org.openhab.binding.avmfritz.internal.dto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PercentType;

/**
 * Tests for {@link ColorControlModel} methods.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
class ColorControlModelTest {

    @Test
    public void testColorControlModelSaturationConversionIsBijective() {
        for (int percent = 0; percent <= 100; ++percent) {
            PercentType percentType = new PercentType(percent);
            int saturation = ColorControlModel.fromPercent(percentType);
            assertThat(ColorControlModel.toPercent(saturation).intValue(), is(percent));
        }
    }

    @Test
    public void testColorControlModelPercentConversionRestrictsToLowerBounds() {
        assertThat(ColorControlModel.toPercent(-1), is(PercentType.ZERO));
    }

    @Test
    public void testColorControlModelPercentConversionRestrictsToUpperBounds() {
        assertThat(ColorControlModel.toPercent(999), is(PercentType.HUNDRED));
    }

    @Test
    public void hsbSaturationAlwaysGreaterThanZero() {
        // a saturation greater than 1 should result in a percentage greater than 1
        for (int saturation = 1; saturation <= 254; ++saturation) {
            PercentType percentType = ColorControlModel.toPercent(saturation);
            assertTrue(ColorControlModel.fromPercent(percentType) > 0);
        }
    }
}
