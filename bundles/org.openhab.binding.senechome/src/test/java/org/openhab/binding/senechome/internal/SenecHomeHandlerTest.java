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
package org.openhab.binding.senechome.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.thing.Thing;

/**
 * Test for senec value parsing. All test data are from "original" senec (using vars.html).
 *
 * @author Erwin Guib - Initial Contribution
 */
@ExtendWith(MockitoExtension.class)
class SenecHomeHandlerTest {

    protected static Object[][] data() {
        return new Object[][] {
                // unsigned
                { "u1_0002", BigDecimal.valueOf(2) }, //
                { "u1_07DB", BigDecimal.valueOf(2011) }, //
                { "u3_0000194C", BigDecimal.valueOf(6476) }, //
                { "u3_817E00E0", BigDecimal.valueOf(2172518624L) }, //
                { "u6_0000000000000001", BigDecimal.valueOf(1) }, //
                { "u6_00000000000C75D9", BigDecimal.valueOf(816601) }, //
                { "u8_64", BigDecimal.valueOf(100) }, //
                // int
                { "i1_00FA", BigDecimal.valueOf(250) }, //
                { "i3_00000078", BigDecimal.valueOf(120) }, //
                { "i3_609F8480", BigDecimal.valueOf(1621066880) }, //
                { "i3_FFFFFFFF", BigDecimal.valueOf(-1) }, //
                { "i8_18", BigDecimal.valueOf(24) }, //
                // string (unknown)
                { "st_HMI: 3.15.32 PU: 4.1.89", BigDecimal.valueOf(0) } };
    }

    protected static Object[][] floatData() {
        return new Object[][] {
                // float
                { "fl_41C80000", BigDecimal.valueOf(25), 0 }, //
                { "fl_4247632F", BigDecimal.valueOf(49.85), 2 }, //
                { "fl_C5AB6F0B", BigDecimal.valueOf(-5485.88), 2 }, //
                { "fl_4248CCCD", BigDecimal.valueOf(50.2), 1 }, //
        };
    }

    @Mock
    Thing mockThing;

    @Mock
    HttpClient mockHttpClient;

    SenecHomeHandler cut = new SenecHomeHandler(mockThing, mockHttpClient);

    @ParameterizedTest
    @MethodSource("data")
    void getSenecValue(String value, Object expectedResult) {
        Assertions.assertEquals(expectedResult, cut.getSenecValue(value));
    }

    @ParameterizedTest
    @MethodSource("floatData")
    void getSenecValueFloat(String value, Object expectedResult, int scale) {
        Assertions.assertEquals(expectedResult, cut.getSenecValue(value).setScale(scale, RoundingMode.HALF_UP));
    }
}
