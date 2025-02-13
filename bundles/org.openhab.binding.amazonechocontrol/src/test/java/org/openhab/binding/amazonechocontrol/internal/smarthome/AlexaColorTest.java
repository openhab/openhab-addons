/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants;
import org.openhab.core.test.java.JavaTest;

/**
 * The {@link AlexaColorTest} is a
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class AlexaColorTest extends JavaTest {

    @ParameterizedTest
    @MethodSource("getColors")
    public void distanceTest(AlexaColor color) {
        Assertions.assertEquals(color.colorName, AlexaColor.getClosestColorName(color.value));
    }

    @SuppressWarnings("unused")
    private static Stream<AlexaColor> getColors() {
        return AmazonEchoControlBindingConstants.ALEXA_COLORS.stream();
    }
}
