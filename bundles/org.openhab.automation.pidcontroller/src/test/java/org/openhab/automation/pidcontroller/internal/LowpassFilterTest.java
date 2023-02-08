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
package org.openhab.automation.pidcontroller.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test for LowpassFilter.
 *
 * @author Fabian Wolter - Initial contribution
 *
 */
@NonNullByDefault
class LowpassFilterTest {
    @Test
    void test0to1after1tau() {
        double output = LowpassFilter.calculate(0, 1, 1);
        assertEquals(0.63, output, 0.01);
    }

    @Test
    void test0to1after2tau() {
        double output = LowpassFilter.calculate(0, 1, 1);
        output = LowpassFilter.calculate(output, 1, 1);
        assertEquals(0.86, output, 0.01);
    }

    @Test
    void test0to1after5tau() {
        double output = LowpassFilter.calculate(0, 1, 1);
        output = LowpassFilter.calculate(output, 1, 1);
        output = LowpassFilter.calculate(output, 1, 1);
        output = LowpassFilter.calculate(output, 1, 1);
        output = LowpassFilter.calculate(output, 1, 1);
        assertEquals(0.99, output, 0.01);
    }

    @Test
    void test0to1after1tau2timeConstant() {
        double output = LowpassFilter.calculate(0, 1, 2);
        assertEquals(0.86, output, 0.01);
    }

    @Test
    void test0to1after0_1tau() {
        double output = LowpassFilter.calculate(0, 1, 0.1);
        assertEquals(0.095162582, output, 0.000000001);
    }

    @Test
    void test1to0after1tau() {
        double output = LowpassFilter.calculate(1, 0, 1);
        assertEquals(0.36, output, 0.01);
    }
}
