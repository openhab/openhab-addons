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
 * Unit test for {@link MultiplyTransformationService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
class MultiplyTransformationServiceTest {

    private static final Stream<Arguments> configurations() {
        return Stream.of(Arguments.of("2000", "100", "20"), //
                Arguments.of("0", "0", "8.0"), //
                Arguments.of("42 m", "21 m", "2"), //
                Arguments.of("42 m", "21", "2 m"), //
                Arguments.of("1380 A·V", "6.0 A", "230 V"), //
                Arguments.of("NULL", "NULL", "50"), //
                Arguments.of("UNDEF", "UNDEF", "50 V"));
    }

    private final TransformationService subject = new MultiplyTransformationService();

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
}
