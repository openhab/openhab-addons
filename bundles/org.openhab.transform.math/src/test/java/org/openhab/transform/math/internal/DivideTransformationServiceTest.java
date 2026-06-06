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

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;

/**
 * Unit test for {@link DivideTransformationService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Jan N. Klug - adapted to DivideTransformation
 */
@NonNullByDefault
class DivideTransformationServiceTest {

    private static final Stream<Arguments> configurations() {
        return Stream.of(Arguments.of("100", "-2000", "-20"), //
                Arguments.of("0.3333333333333333333333333333333333", "1", "3"), //
                Arguments.of("0", "0", "8.0"), //
                Arguments.of("6 W/V", "1380 W", "230 V"));
    }

    private final TransformationService subject = new DivideTransformationService();

    @ParameterizedTest
    @MethodSource("configurations")
    public void testTransform(String expected, String source, String value) throws TransformationException {
        assertEquals(expected, subject.transform(value, source));
    }

    @Test
    public void testTransformInvalidSource() {
        assertThrows(TransformationException.class, () -> subject.transform("20", "*"));
    }

    @Test
    public void testTransformInvalidFunction() {
        assertThrows(TransformationException.class, () -> subject.transform("*", "90"));
    }

    @Test
    public void testTransformDivideByZero() {
        assertThrows(TransformationException.class, () -> subject.transform("0", "1"));
    }
}
