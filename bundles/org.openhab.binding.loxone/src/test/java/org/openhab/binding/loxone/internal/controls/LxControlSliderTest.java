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
 * Test class for (@link LxControlSlider} - this is actually the same control as up down analog
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlSliderTest extends LxControlUpDownAnalogTest {
    @Override
    @BeforeEach
    public void setup() {
        min = 120.0;
        max = 450.0;
        step = 3.333;
        format = "%.1f";
        setupControl("131fb314-0370-c93c-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Slider Virtual Input");
    }

    @Override
    @Test
    public void testControlCreation() {
        testControlCreation(LxControlSlider.class, 1, 0, 1, 1, 2);
    }
}
