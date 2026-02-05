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
 * Unit test for {@link AddTransformationService}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
class AddTransformationServiceTest {

    private static final Stream<Arguments> configurations() {
        return Stream.of(Arguments.of("120", "100", "20"), //
                Arguments.of("80", "100", "-20"), //
                Arguments.of("0", "0", "0.0"), //
                Arguments.of("23 °C", "21 °C", "2 °C"), //
                Arguments.of("1,3 m", "1 m", "30 cm"));
    }

    private final TransformationService subject = new AddTransformationService();

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
    public void testTransformInvalidUnits() {
        assertThrows(TransformationException.class, () -> subject.transform("2 m", "5 g"));
    }
}
