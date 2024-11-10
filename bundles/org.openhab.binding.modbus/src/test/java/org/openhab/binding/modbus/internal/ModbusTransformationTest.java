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
package org.openhab.binding.modbus.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ModbusTransformationTest {
    @Test
    public void testTransformationEmpty() {
        ModbusTransformation transformation = new ModbusTransformation(List.of(""));
        assertFalse(transformation.isIdentityTransform());
        assertEquals("", transformation.transform("xx"));
    }

    @Test
    public void testTransformationNull() {
        ModbusTransformation transformation = new ModbusTransformation(null);
        assertFalse(transformation.isIdentityTransform());
        assertEquals("", transformation.transform("xx"));
    }

    @Test
    public void testTransformationDefault() {
        ModbusTransformation transformation = new ModbusTransformation(List.of("deFault"));
        assertTrue(transformation.isIdentityTransform());
        assertEquals("xx", transformation.transform("xx"));
    }

    @Test
    public void testTransformationConstant() {
        ModbusTransformation transformation = new ModbusTransformation(List.of("constant"));
        assertFalse(transformation.isIdentityTransform());
        assertEquals("constant", transformation.transform("xx"));
    }

    @Test
    public void testTransformationFailed() {
        ModbusTransformation transformation = new ModbusTransformation(List.of("NONEXISTENT(test)"));
        assertFalse(transformation.isIdentityTransform());
        assertEquals("", transformation.transform("xx"));
    }
}
