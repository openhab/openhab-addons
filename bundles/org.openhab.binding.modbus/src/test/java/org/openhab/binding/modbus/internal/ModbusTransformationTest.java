/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;

/**
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ModbusTransformationTest {
    @Test
    public void testCommandConversionDoesNotAcceptContactStates() {
        assertEquals(Optional.empty(), ModbusTransformation.tryConvertToCommand("OPEN"));
        assertEquals(Optional.of(OnOffType.ON), ModbusTransformation.tryConvertToCommand("ON"));
    }

    @Test
    public void testContactStateTransformation() {
        ModbusTransformation transformation = new ModbusTransformation(List.of("default"));
        assertEquals(OpenClosedType.OPEN,
                transformation.transformState(List.of(OpenClosedType.class), OpenClosedType.OPEN));
    }

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
