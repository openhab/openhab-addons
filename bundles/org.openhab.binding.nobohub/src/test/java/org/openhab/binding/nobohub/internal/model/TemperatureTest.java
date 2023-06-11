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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for temperature model object.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class TemperatureTest {

    @Test
    public void testParseY02() throws NoboDataException {
        Temperature temp = Temperature.fromY02("Y02 123123123123 12.345");
        assertEquals(new SerialNumber("123123123123"), temp.getSerialNumber());
        assertEquals(12.34, temp.getTemperature(), 0.1);
    }

    @Test
    public void testParseY02NATemp() throws NoboDataException {
        Temperature temp = Temperature.fromY02("Y02 123123123123 N/A");
        assertEquals(new SerialNumber("123123123123"), temp.getSerialNumber());
        assertEquals(Double.NaN, temp.getTemperature(), 0.1);
    }
}
