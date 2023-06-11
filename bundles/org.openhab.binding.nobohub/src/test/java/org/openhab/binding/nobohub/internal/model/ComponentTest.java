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

package org.openhab.binding.nobohub.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Component model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ComponentTest {
    @Test
    public void testParseH02() throws NoboDataException {
        Component comp = Component.fromH02("H02 186170024143 0 Kontor 0 1 -1 -1");
        comp.setTemperature(12.3);
        assertEquals(new SerialNumber("186170024143"), comp.getSerialNumber());
        assertEquals("Kontor", comp.getName());
        assertEquals(1, comp.getZoneId());
        assertEquals(-1, comp.getTemperatureSensorForZoneId());
        assertFalse(comp.inReverse());
        assertEquals(12.3, comp.getTemperature(), 0.1);
    }

    @Test
    public void testGenerateU03() throws NoboDataException {
        Component comp = Component.fromH02("H02 186170024143 0 Kontor 0 1 -1 -1");
        assertEquals("U02 186170024143 0 Kontor 0 1 -1 -1", comp.generateCommandString("U02"));
    }
}
