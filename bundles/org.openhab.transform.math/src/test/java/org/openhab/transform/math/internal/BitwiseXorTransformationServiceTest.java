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
package org.openhab.transform.math.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;

/**
 * Unit test for {@link BitwiseXorTransformationService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Jan N. Klug - Adapted to bitwise transformation
 */
@NonNullByDefault
class BitwiseXorTransformationServiceTest {
    private final TransformationService subject = new BitwiseXorTransformationService();

    @Test
    public void testTransformHex() throws TransformationException {
        String result = subject.transform("0x30", "0x10");
        assertEquals("32", result);
    }

    @Test
    public void testTransformDecimalSource() throws TransformationException {
        String result = subject.transform("0x34", "206");
        assertEquals("250", result);
    }

    @Test
    public void testTransformBinarySource() throws TransformationException {
        String result = subject.transform("0x34", "0b11100110");
        assertEquals("210", result);
    }

    @Test
    public void testTransformDecimalFunction() throws TransformationException {
        String result = subject.transform("1", "0xff");
        assertEquals("254", result);
    }

    @Test
    public void testTransformBinaryFunction() throws TransformationException {
        String result = subject.transform("0b00000010", "0xff");
        assertEquals("253", result);
    }

    @Test
    public void testTransformInvalidSource() {
        assertThrows(TransformationException.class, () -> subject.transform("0x90", "a"));
    }

    @Test
    public void testTransformInvalidFunction() {
        assertThrows(TransformationException.class, () -> subject.transform("*", "20"));
    }
}
