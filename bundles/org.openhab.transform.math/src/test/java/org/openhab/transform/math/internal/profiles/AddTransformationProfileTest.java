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
package org.openhab.transform.math.internal.profiles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Entry;
import org.openhab.core.types.TimeSeries.Policy;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.transform.math.internal.AddTransformationService;

/**
 * Basic unit tests for {@link DivideTransformationProfile}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
class AddTransformationProfileTest {

    private static final String UNKNOWN_ITEM_NAME = "unknownItem";
    private static final String TEST_ITEM_NAME = "testItem";

    private static final Stream<Arguments> configurations() {
        return Stream.of(Arguments.of(20, DecimalType.valueOf("100"), null, null, DecimalType.valueOf("120")), //
                Arguments.of(20, DecimalType.valueOf("100"), null, DecimalType.valueOf("-20"),
                        DecimalType.valueOf("120")), //
                Arguments.of(20, DecimalType.valueOf("100"), TEST_ITEM_NAME, UnDefType.NULL,
                        DecimalType.valueOf("120")), //
                Arguments.of(20, DecimalType.valueOf("100"), TEST_ITEM_NAME, UnDefType.UNDEF,
                        DecimalType.valueOf("120")), //
                Arguments.of(20, DecimalType.valueOf("100"), UNKNOWN_ITEM_NAME, DecimalType.valueOf("-20"),
                        DecimalType.valueOf("120")), //
                Arguments.of(0, DecimalType.valueOf("100"), TEST_ITEM_NAME, DecimalType.valueOf("-20"),
                        DecimalType.valueOf("80")), //
                Arguments.of(0, QuantityType.valueOf("21 °C"), TEST_ITEM_NAME, QuantityType.valueOf("2 °C"),
                        QuantityType.valueOf("23 °C")), //
                Arguments.of(0, QuantityType.valueOf("1 m"), TEST_ITEM_NAME, QuantityType.valueOf("30 cm"),
                        QuantityType.valueOf("1.3 m")), //
                Arguments.of(0, QuantityType.valueOf("2 m"), TEST_ITEM_NAME, QuantityType.valueOf("5 g"),
                        QuantityType.valueOf("2 m")));
    }

    @BeforeEach
    public void setup() {
        // initialize parser with ImperialUnits, otherwise units like °F are unknown
        @SuppressWarnings("unused")
        Unit<Temperature> fahrenheit = ImperialUnits.FAHRENHEIT;
    }

    @ParameterizedTest
    @MethodSource("configurations")
    public void testOnCommandFromHandler(Integer divisor, Command cmd, @Nullable String itemName,
            @Nullable State itemState, Command expectedResult) throws ItemNotFoundException {
        ProfileCallback callback = mock(ProfileCallback.class);
        AddTransformationProfile profile = createProfile(callback, divisor, itemName, itemState);

        profile.onCommandFromHandler(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).sendCommand(capture.capture());

        Command result = capture.getValue();
        assertThat(result, is(expectedResult));
    }

    @ParameterizedTest
    @MethodSource("configurations")
    public void testOnStateUpdateFromHandler(Integer divisor, State state, @Nullable String itemName,
            @Nullable State itemState, State expectedResult) throws ItemNotFoundException {
        ProfileCallback callback = mock(ProfileCallback.class);
        AddTransformationProfile profile = createProfile(callback, divisor, itemName, itemState);

        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        State result = capture.getValue();
        assertThat(result, is(expectedResult));
    }

    @ParameterizedTest
    @MethodSource("configurations")
    public void testTimeSeriesFromHandlerParameterized(Integer divisor, State state, @Nullable String itemName,
            @Nullable State itemState, State expectedResult) throws ItemNotFoundException {
        ProfileCallback callback = mock(ProfileCallback.class);
        AddTransformationProfile profile = createProfile(callback, divisor, itemName, itemState);

        TimeSeries ts = new TimeSeries(Policy.ADD);
        Instant now = Instant.now();
        ts.add(now, state);

        profile.onTimeSeriesFromHandler(ts);

        ArgumentCaptor<TimeSeries> capture = ArgumentCaptor.forClass(TimeSeries.class);
        verify(callback, times(1)).sendTimeSeries(capture.capture());

        TimeSeries result = capture.getValue();
        assertThat(result.getStates().count(), is(ts.getStates().count()));
        Entry firstEntry = result.getStates().findFirst().get();
        assertNotNull(firstEntry);
        assertThat(firstEntry.timestamp(), is(now));
        assertThat(firstEntry.state(), is(expectedResult));
    }

    private AddTransformationProfile createProfile(ProfileCallback callback, Integer addend, @Nullable String itemName,
            @Nullable State state) throws ItemNotFoundException {
        ProfileContext mockedProfileContext = mock(ProfileContext.class);
        ItemRegistry mockedItemRegistry = mock(ItemRegistry.class);
        UnitProvider mockedUnitProvider = mock(UnitProvider.class);
        Configuration config = new Configuration();
        config.put(AddTransformationProfile.ADDEND_PARAM, addend);
        if (itemName != null && state != null) {
            config.put(AbstractArithmeticMathTransformationProfile.ITEM_NAME_PARAM, itemName);
            GenericItem item;
            if (state instanceof QuantityType<?> quantityType) {
                when(mockedUnitProvider.getUnit(any())).thenAnswer(i -> quantityType.getUnit());
                item = new NumberItem("Number:" + UnitUtils.getDimensionName(quantityType.getUnit()), TEST_ITEM_NAME,
                        mockedUnitProvider);
            } else {
                item = new NumberItem(TEST_ITEM_NAME);
            }
            item.setState(state);
            when(mockedItemRegistry.getItem(TEST_ITEM_NAME)).thenReturn(item);
            when(mockedItemRegistry.getItem(UNKNOWN_ITEM_NAME)).thenThrow(ItemNotFoundException.class);
        }
        when(mockedProfileContext.getConfiguration()).thenReturn(config);

        return new AddTransformationProfile(callback, mockedProfileContext, new AddTransformationService(),
                mockedItemRegistry);
    }
}
