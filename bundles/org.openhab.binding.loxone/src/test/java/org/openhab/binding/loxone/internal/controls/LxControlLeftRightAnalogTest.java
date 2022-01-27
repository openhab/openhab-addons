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
package org.openhab.binding.loxone.internal.controls;

import org.junit.jupiter.api.BeforeEach;

/**
 * Test class for (@link LxControlLeftRightAnalog}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlLeftRightAnalogTest extends LxControlUpDownAnalogTest {
    @Override
    @BeforeEach
    public void setup() {
        min = 1072.123;
        max = 1123.458;
        step = 23.987;
        format = "value: %.2f";
        setupControl("131b1a96-02b9-f6e9-ffff403fb0c34b9e", "0b734138-037d-034e-ffff403fb0c34b9e",
                "0fe650c2-0004-d446-ffff504f9410790f", "Left Right Analog Input");
    }
}
