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
package org.openhab.binding.sensorcommunity.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sensorcommunity.internal.utils.NumberUtils;

/**
 * The {@link NumberTest} Test rounding and converting Numbers
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class NumberTest {

    @Test
    public void testRoundingUp() {
        double d1 = 1.95;
        double d1r2 = NumberUtils.round(d1, 2);
        assertEquals("1.95", Double.toString(d1r2), "Double 1.95, 2 places");
        // System.out.println("D1R2 " + d1r2);
        double d1r1 = NumberUtils.round(d1, 1);
        // System.out.println("D1R1 " + d1r1);
        assertEquals("2.0", Double.toString(d1r1), "Double 1.95, 1 place");
    }

    @Test
    public void testRoundingDown() {
        double d1 = 1.94;
        double d1r2 = NumberUtils.round(d1, 2);
        assertEquals("1.94", Double.toString(d1r2), "Double 1.94, 2 places");
        // System.out.println("D1R2 " + d1r2);
        double d1r1 = NumberUtils.round(d1, 1);
        // System.out.println("D1R1 " + d1r1);
        assertEquals("1.9", Double.toString(d1r1), "Double 1.94, 1 place");
    }

    @Test
    public void testStringNumbers() {
        String d1 = "1.94";
        double d1r2 = NumberUtils.round(d1, 2);
        assertEquals("1.94", Double.toString(d1r2), "Double 1.94, 2 places");
        // System.out.println("D1R2 " + d1r2);
        double d1r1 = NumberUtils.round(d1, 1);
        // System.out.println("D1R1 " + d1r1);
        assertEquals("1.9", Double.toString(d1r1), "Double 1.94, 1 place");
    }
}
