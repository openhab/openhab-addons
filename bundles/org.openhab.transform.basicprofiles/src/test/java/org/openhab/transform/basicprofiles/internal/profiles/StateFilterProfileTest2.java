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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Basic unit tests for {@link StateFilterProfile}.
 *
 * @author Arne Seime - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class StateFilterProfileTest2 {

    private @Mock @NonNullByDefault({}) ProfileCallback mockCallback;
    private @Mock @NonNullByDefault({}) ProfileContext mockContext;
    private @Mock @NonNullByDefault({}) ItemRegistry mockItemRegistry;
    private @Mock @NonNullByDefault({}) ItemChannelLink mockItemChannelLink;

    protected static class WattUnitProvider implements UnitProvider {

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Quantity<T>> Unit<T> getUnit(Class<T> dimension) {
            return (Unit<T>) Units.WATT;
        }

        @Override
        public SystemOfUnits getMeasurementSystem() {
            return Units.getInstance();
        }

        @Override
        public Collection<Class<? extends Quantity<?>>> getAllDimensions() {
            return Set.of();
        }
    }

    private static final UnitProvider UNIT_PROVIDER = new WattUnitProvider();

    @BeforeEach
    public void setup() throws ItemNotFoundException {
        reset(mockContext);
        reset(mockCallback);
        reset(mockItemChannelLink);
        when(mockCallback.getItemChannelLink()).thenReturn(mockItemChannelLink);
        when(mockItemRegistry.getItem("")).thenThrow(ItemNotFoundException.class);
    }

    public static Stream<Arguments> testMixedStates() {
        NumberItem powerItem = new NumberItem("Number:Power", "powerItem", UNIT_PROVIDER);

        List<State> states = List.of( //
                UnDefType.UNDEF, //
                QuantityType.valueOf(99, SIUnits.METRE), //
                QuantityType.valueOf(1, Units.WATT), //
                DecimalType.valueOf("2"), //
                QuantityType.valueOf(3000, MetricPrefix.MILLI(Units.WATT))); //

        return Stream.of(
                // average function
                // Arguments.of(powerItem, "== $AVG", states, QuantityType.valueOf("2 W"), true),
                // Arguments.of(powerItem, "== $AVG", states, QuantityType.valueOf("2000 mW"), true),
                // Arguments.of(powerItem, "== $AVERAGE", states, QuantityType.valueOf("0.002 kW"), true),
                // Arguments.of(powerItem, "> $AVERAGE", states, QuantityType.valueOf("2 W"), false),
                // Arguments.of(powerItem, "> $AVERAGE", states, QuantityType.valueOf("3 W"), true),
                //
                // Arguments.of(powerItem, "== $AVERAGE", states, DecimalType.valueOf("2"), false),
                //
                // // min function
                // Arguments.of(powerItem, "== $MIN", states, QuantityType.valueOf("1 W"), true),
                // Arguments.of(powerItem, "== $MIN", states, QuantityType.valueOf("1000 mW"), true),
                //
                // Arguments.of(powerItem, "== $MIN", states, DecimalType.valueOf("1"), false),
                //
                // // max function
                // Arguments.of(powerItem, "== $MAX", states, QuantityType.valueOf("3 W"), true),
                // Arguments.of(powerItem, "== $MAX", states, QuantityType.valueOf("0.003 kW"), true),
                //
                // Arguments.of(powerItem, "== $MAX", states, DecimalType.valueOf("1"), false),

                // delta function
                Arguments.of(powerItem, "$DELTA == 1 W", states, QuantityType.valueOf("4 W"), true),
                Arguments.of(powerItem, "$DELTA == 0.001 kW", states, QuantityType.valueOf("4 W"), true),
                Arguments.of(powerItem, "1 == $DELTA", states, QuantityType.valueOf("4 W"), true),
                Arguments.of(powerItem, "1000 mW == $DELTA", states, QuantityType.valueOf("4 W"), true),

                // delta percent function
                Arguments.of(powerItem, "$DELTA_PERCENT == 50", states, QuantityType.valueOf("4.5 W"), true),
                Arguments.of(powerItem, "50 == $DELTA_PERCENT", states, QuantityType.valueOf("4.5 W"), true),

                // last no comma
                Arguments.of(powerItem, "== $MAX", states, QuantityType.valueOf("0.004 kW"), true),
                Arguments.of(powerItem, "!= $MAX", states, QuantityType.valueOf("0.004 kW"), true)
        //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testMixedStates(Item item, String condition, List<State> states, State input, boolean expected)
            throws ItemNotFoundException {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", condition)));
        when(mockItemRegistry.getItem(item.getName())).thenReturn(item);
        when(mockItemChannelLink.getItemName()).thenReturn(item.getName());

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        for (State state : states) {
            profile.onStateUpdateFromHandler(state);
        }

        reset(mockCallback);
        when(mockCallback.getItemChannelLink()).thenReturn(mockItemChannelLink);

        profile.onStateUpdateFromHandler(input);
        verify(mockCallback, times(expected ? 1 : 0)).sendUpdate(input);
    }
}
