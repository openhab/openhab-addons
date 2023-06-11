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
package org.openhab.binding.tradfri.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

/**
 * Tests for {@link TradfriColor}.
 *
 * @author Holger Reichert - Initial contribution
 */
@NonNullByDefault
public class TradfriColorTest {

    @Test
    public void testFromCieKnownGood1() {
        TradfriColor color = new TradfriColor(29577, 12294, 354);
        assertEquals(29577, (int) color.xyX);
        assertEquals(12294, (int) color.xyY);
        assertEquals(254, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(321, hsbType.getHue().intValue());
        assertEquals(100, hsbType.getSaturation().intValue());
        assertEquals(100, hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromCieKnownGood2() {
        TradfriColor color = new TradfriColor(19983, 37417, 84);
        assertEquals(19983, (int) color.xyX);
        assertEquals(37417, (int) color.xyY);
        assertEquals(84, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(115, hsbType.getHue().intValue());
        assertEquals(77, hsbType.getSaturation().intValue());
        assertEquals(34, hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromCieKnownGood3() {
        TradfriColor color = new TradfriColor(19983, 37417, 1);
        assertEquals(19983, (int) color.xyX);
        assertEquals(37417, (int) color.xyY);
        assertEquals(1, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(115, hsbType.getHue().intValue());
        assertEquals(77, hsbType.getSaturation().intValue());
        assertEquals(1, hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromCieKnownGood4() {
        TradfriColor color = new TradfriColor(11413, 31334, 181);
        assertEquals(11413, (int) color.xyX);
        assertEquals(31334, (int) color.xyY);
        assertEquals(181, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(158, hsbType.getHue().intValue());
        assertEquals(100, hsbType.getSaturation().intValue());
        assertEquals(72, hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromHSBTypeKnownGood1() {
        TradfriColor color = new TradfriColor(HSBType.RED);
        assertEquals(41947, (int) color.xyX);
        assertEquals(21625, (int) color.xyY);
        assertEquals(254, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(0, hsbType.getHue().intValue());
        assertEquals(100, hsbType.getSaturation().intValue());
        assertEquals(100, hsbType.getBrightness().intValue());
    }

    @Test
    public void testFromHSBTypeKnownGood2() {
        TradfriColor color = new TradfriColor(new HSBType("0,100,1"));
        assertEquals(41947, (int) color.xyX);
        assertEquals(21625, (int) color.xyY);
        assertEquals(2, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(0, hsbType.getHue().intValue());
        assertEquals(100, hsbType.getSaturation().intValue());
        assertEquals(1, hsbType.getBrightness().intValue());
    }

    @Test
    public void testConversionReverse() {
        // convert from HSBType
        TradfriColor color = new TradfriColor(HSBType.GREEN);
        assertEquals(19660, (int) color.xyX);
        assertEquals(39321, (int) color.xyY);
        assertEquals(254, (int) color.brightness);
        HSBType hsbType = color.getHSB();
        assertNotNull(hsbType);
        assertEquals(120, hsbType.getHue().intValue());
        assertEquals(100, hsbType.getSaturation().intValue());
        assertEquals(100, hsbType.getBrightness().intValue());
        // convert the result again based on the XY values
        TradfriColor reverse = new TradfriColor(color.xyX, color.xyY, color.brightness);
        assertEquals(19660, (int) reverse.xyX);
        assertEquals(39321, (int) reverse.xyY);
        assertEquals(254, (int) reverse.brightness);
        HSBType hsbTypeReverse = color.getHSB();
        assertNotNull(hsbTypeReverse);
        assertEquals(120, hsbTypeReverse.getHue().intValue());
        assertEquals(100, hsbTypeReverse.getSaturation().intValue());
        assertEquals(100, hsbTypeReverse.getBrightness().intValue());
    }

    @Test
    public void testFromColorTemperatureMinMiddleMax() {
        // coldest color temperature -> preset 1
        TradfriColor colorMin = new TradfriColor(PercentType.ZERO);
        assertNotNull(colorMin);
        assertEquals(24933, (int) colorMin.xyX);
        assertEquals(24691, (int) colorMin.xyY);
        // middle color temperature -> preset 2
        TradfriColor colorMiddle = new TradfriColor(new PercentType(50));
        assertNotNull(colorMiddle);
        assertEquals(30138, (int) colorMiddle.xyX);
        assertEquals(26909, (int) colorMiddle.xyY);
        // warmest color temperature -> preset 3
        TradfriColor colorMax = new TradfriColor(PercentType.HUNDRED);
        assertNotNull(colorMax);
        assertEquals(33137, (int) colorMax.xyX);
        assertEquals(27211, (int) colorMax.xyY);
    }

    @Test
    public void testFromColorTemperatureInbetween() {
        // 30 percent must be between preset 1 and 2
        TradfriColor color2 = new TradfriColor(new PercentType(30));
        assertNotNull(color2);
        assertEquals(28056, (int) color2.xyX);
        assertEquals(26022, (int) color2.xyY);
        // 70 percent must be between preset 2 and 3
        TradfriColor color3 = new TradfriColor(new PercentType(70));
        assertNotNull(color3);
        assertEquals(31338, (int) color3.xyX);
        assertEquals(27030, (int) color3.xyY);
    }

    @Test
    public void testCalculateColorTemperature() {
        // preset 1 -> coldest -> 0 percent
        PercentType preset1 = new TradfriColor(24933, 24691, null).getColorTemperature();
        assertEquals(0, preset1.intValue());
        // preset 2 -> middle -> 50 percent
        PercentType preset2 = new TradfriColor(30138, 26909, null).getColorTemperature();
        assertEquals(50, preset2.intValue());
        // preset 3 -> warmest -> 100 percent
        PercentType preset3 = new TradfriColor(33137, 27211, null).getColorTemperature();
        assertEquals(100, preset3.intValue());
        // preset 3 -> warmest -> 100 percent
        PercentType colder = new TradfriColor(22222, 23333, null).getColorTemperature();
        assertEquals(0, colder.intValue());
        // preset 3 -> warmest -> 100 percent
        PercentType temp3 = new TradfriColor(34000, 34000, null).getColorTemperature();
        assertEquals(100, temp3.intValue());
        // mixed case 1
        PercentType mixed1 = new TradfriColor(0, 1000000, null).getColorTemperature();
        assertEquals(0, mixed1.intValue());
        // mixed case 1
        PercentType mixed2 = new TradfriColor(1000000, 0, null).getColorTemperature();
        assertEquals(100, mixed2.intValue());
    }
}
