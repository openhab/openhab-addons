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
package org.openhab.binding.loxone.internal.controls;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for (@link LxControlLeftRightDigital}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlLeftRightDigitalTest extends LxControlUpDownDigitalTest {
    @Override
    @BeforeEach
    public void setup() {
        upChannel = " / Left";
        downChannel = " / Right";
        setupControl("0fd08ca6-01a6-d72a-efff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0b734138-033e-02d4-ffff403fb0c34b9e", "Second Floor Scene");
    }

    @Override
    @Test
    public void testControlCreation() {
        testControlCreation(LxControlLeftRightDigital.class, 1, 0, 2, 2, 0);
    }
}
