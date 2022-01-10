/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.mapdb;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.persistence.mapdb.internal.StateTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Martin Kühl - Initial contribution
 */
@NonNullByDefault
public class StateTypeAdapterTest {
    private Gson mapper = new GsonBuilder().registerTypeHierarchyAdapter(State.class, new StateTypeAdapter()).create();

    private static final List<DecimalType> DECIMAL_TYPE_VALUES = List.of(DecimalType.ZERO, new DecimalType(1.123),
            new DecimalType(10000000));

    private static final List<HSBType> HSB_TYPE_VALUES = List.of(HSBType.BLACK, HSBType.GREEN, HSBType.WHITE,
            HSBType.fromRGB(1, 2, 3), HSBType.fromRGB(11, 22, 33), HSBType.fromRGB(0, 0, 255));

    private static final List<OnOffType> ON_OFF_TYPE_VALUES = List.of(OnOffType.ON, OnOffType.OFF);

    private static final List<PercentType> PERCENT_TYPE_VALUES = List.of(PercentType.ZERO, PercentType.HUNDRED,
            PercentType.valueOf("0.0000001"), PercentType.valueOf("12"), PercentType.valueOf("99.999"));

    private static final List<QuantityType<?>> QUANTITY_TYPE_VALUES = List.of(QuantityType.valueOf("0 W"),
            QuantityType.valueOf("1 kW"), QuantityType.valueOf(20, Units.AMPERE),
            new QuantityType<>(new BigDecimal("21.23"), SIUnits.CELSIUS),
            new QuantityType<>(new BigDecimal("75"), ImperialUnits.MILES_PER_HOUR),
            QuantityType.valueOf(1000, Units.KELVIN), QuantityType.valueOf(100, Units.METRE_PER_SQUARE_SECOND));

    private static final List<StringType> STRING_TYPE_VALUES = List.of(StringType.valueOf("test"),
            StringType.valueOf("a b c 1 2 3"), StringType.valueOf(""), StringType.valueOf("@@@###   @@@"));

    private static final List<State> VALUES = Stream.of(DECIMAL_TYPE_VALUES, HSB_TYPE_VALUES, ON_OFF_TYPE_VALUES,
            PERCENT_TYPE_VALUES, QUANTITY_TYPE_VALUES, STRING_TYPE_VALUES).flatMap(list -> list.stream())
            .collect(Collectors.toList());

    @ParameterizedTest
    @MethodSource
    public void readWriteRoundtripShouldRecreateTheWrittenState(State state) {
        String json = mapper.toJson(state);
        State actual = Objects.requireNonNull(mapper.fromJson(json, State.class));
        assertThat(actual, is(equalTo(state)));
    }

    public static Stream<State> readWriteRoundtripShouldRecreateTheWrittenState() {
        return VALUES.stream();
    }
}
