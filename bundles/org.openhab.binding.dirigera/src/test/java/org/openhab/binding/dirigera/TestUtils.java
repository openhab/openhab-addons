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
package org.openhab.binding.dirigera;

import static org.openhab.binding.dirigera.internal.Constants.BINDING_ID;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * {@link TestUtils} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestUtils {

    @Test
    void test() {
        String ip = "1.2.3.4";
        int splitIndex = ip.lastIndexOf(".") + 1;
        System.out.println(ip.substring(0, splitIndex));
    }

    @Test
    void testThingsTypeUID() {
        ThingTypeUID ttUID = new ThingTypeUID(BINDING_ID, "gateway");
        System.out.println(ttUID);
    }

    @Test
    void testHSB() {
        PercentType pt = new PercentType(50);
        HSBType hsb = new HSBType(new DecimalType(50), pt, pt);
        System.out.println("H " + hsb.getHue() + " S " + hsb.getSaturation().doubleValue() + " B "
                + hsb.getBrightness().doubleValue());
    }
}
