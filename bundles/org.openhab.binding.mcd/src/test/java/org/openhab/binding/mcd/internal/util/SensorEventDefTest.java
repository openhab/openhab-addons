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
package org.openhab.binding.mcd.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Dengler - Initial contribution
 */
@NonNullByDefault
public class SensorEventDefTest {
    @Test
    public void testGetSensorEventDefinition() {
        assertEquals(-1, SensorEventDef.getSensorEventId("banane"));
        assertEquals(0, SensorEventDef.getSensorEventId(""));
        assertEquals(2, SensorEventDef.getSensorEventId("BEDEXIT"));
    }
}
