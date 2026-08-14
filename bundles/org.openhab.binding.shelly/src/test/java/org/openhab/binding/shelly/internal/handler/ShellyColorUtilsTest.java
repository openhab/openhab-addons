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
package org.openhab.binding.shelly.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PercentType;

/**
 * Tests {@link ShellyColorUtils}, in particular the red/green/blue/white 0..255-to-percent
 * conversion that {@link org.openhab.binding.shelly.internal.api1.Shelly1CoIoTProtocol} relies on
 * to push CoIoT color updates as percent values instead of raw 0..255 counts.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyColorUtilsTest {

    @Test
    void setRedConvertsRawValueToPercent() {
        ShellyColorUtils col = new ShellyColorUtils();

        col.setRed(255);
        assertEquals(new PercentType(100), col.percentRed);

        col.setRed(0);
        assertEquals(new PercentType(0), col.percentRed);

        col.setRed(128);
        assertEquals(new PercentType(50), col.percentRed);
    }

    @Test
    void setGreenBlueWhiteConvertRawValueToPercent() {
        ShellyColorUtils col = new ShellyColorUtils();

        col.setGreen(255);
        col.setBlue(0);
        col.setWhite(128);

        assertEquals(new PercentType(100), col.percentGreen);
        assertEquals(new PercentType(0), col.percentBlue);
        assertEquals(new PercentType(50), col.percentWhite);
    }

    @Test
    void setRedReturnsWhetherValueChanged() {
        ShellyColorUtils col = new ShellyColorUtils();

        assertEquals(true, col.setRed(10));
        assertEquals(false, col.setRed(10));
        assertEquals(true, col.setRed(20));
    }

    @Test
    void toPercentClampsOutOfRangeValues() {
        assertEquals(new PercentType(100), ShellyColorUtils.toPercent(300));
        assertEquals(new PercentType(0), ShellyColorUtils.toPercent(-10));
    }
}
