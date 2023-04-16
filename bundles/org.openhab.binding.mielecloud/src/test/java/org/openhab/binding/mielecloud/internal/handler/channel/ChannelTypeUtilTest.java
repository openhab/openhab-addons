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
package org.openhab.binding.mielecloud.internal.handler.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.mielecloud.internal.webservice.api.Quantity;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ChannelTypeUtilTest {
    private static Stream<Arguments> quantityToStateConversionArguments() {
        return Stream.of(Arguments.of(Optional.empty(), UnDefType.UNDEF),
                Arguments.of(Optional.of(new Quantity(10.0, "Gold")), UnDefType.UNDEF),
                Arguments.of(Optional.of(new Quantity(3.0, null)), new QuantityType<>(3.0, Units.ONE)),
                Arguments.of(Optional.of(new Quantity(1.0 / 3.0, "l")), new QuantityType<>(0.333, Units.LITRE)),
                Arguments.of(Optional.of(new Quantity(20.123, "kWh")), new QuantityType<>(20.123, Units.KILOWATT_HOUR)),
                Arguments.of(Optional.of(new Quantity(0.5, "l")), new QuantityType<>(0.5, Units.LITRE)));
    }

    @ParameterizedTest
    @MethodSource("quantityToStateConversionArguments")
    void quantityCanBeConvertedToState(Optional<Quantity> input, State expected) {
        // when:
        var state = ChannelTypeUtil.quantityToState(input);

        // then:
        assertEquals(expected, state);
    }
}
