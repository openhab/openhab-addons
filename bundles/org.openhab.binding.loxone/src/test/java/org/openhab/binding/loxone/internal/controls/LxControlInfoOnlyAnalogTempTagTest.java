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
package org.openhab.binding.loxone.internal.controls;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for (@link LxControlInfoOnlyAnalog} - check tags for temperature category
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlInfoOnlyAnalogTempTagTest extends LxControlTest {
    @BeforeEach
    public void setup() {
        setupControl("0fec5dc3-003e-8800-ffff555fb0c34b9e", "0fe3a451-0283-2afa-ffff403fb0c34b9e",
                "0fb99a98-02df-46f1-ffff403fb0c34b9e", "Info Only Analog Temperature");
    }

    @Test
    public void testControlCreation() {
        testControlCreation(LxControlInfoOnlyAnalog.class, 2, 0, 1, 1, 1);
    }

    @Test
    public void testChannels() {
        testChannel("Number", null, null, null, null, "%.1f", true, null, Set.of("CurrentTemperature"));
    }
}
