/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.internal.i18n.I18nProviderImpl;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Basic unit tests for {@link StateFilterProfile}.
 *
 * @author Arne Seime - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
public class StateFilterProfileTest {

    private @Mock @NonNullByDefault({}) ProfileCallback mockCallback;
    private @Mock @NonNullByDefault({}) ProfileContext mockContext;
    private @Mock @NonNullByDefault({}) ItemRegistry mockItemRegistry;
    private @Mock @NonNullByDefault({}) ItemChannelLink mockItemChannelLink;

    private static final UnitProvider UNIT_PROVIDER;

    static {
        ComponentContext context = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("measurementSystem", SIUnits.MEASUREMENT_SYSTEM_NAME);
        when(context.getProperties()).thenReturn(properties);
        when(context.getBundleContext()).thenReturn(bundleContext);
        UNIT_PROVIDER = new I18nProviderImpl(context);
    }

    @BeforeEach
    public void setup() throws ItemNotFoundException {
        reset(mockContext);
        reset(mockCallback);
        reset(mockItemChannelLink);
        when(mockCallback.getItemChannelLink()).thenReturn(mockItemChannelLink);
        when(mockItemRegistry.getItem("")).thenThrow(ItemNotFoundException.class);
    }

    @Test
    public void testNoConditions() {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "")));
        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = OnOffType.ON;
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(0)).sendUpdate(eq(expectation));
    }

    @Test
    public void testMalformedConditions() {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName invalid")));
        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = OnOffType.ON;
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(0)).sendUpdate(eq(expectation));
    }

    @Test
    public void testInvalidComparatorConditions() throws ItemNotFoundException {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName is Value")));
        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Value"));

        State expectation = OnOffType.ON;
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(0)).sendUpdate(eq(expectation));
    }

    @Test
    public void testInvalidItemConditions() throws ItemNotFoundException {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value")));
        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        when(mockItemRegistry.getItem(any())).thenThrow(ItemNotFoundException.class);
        State expectation = OnOffType.ON;
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(0)).sendUpdate(eq(expectation));
    }

    @Test
    public void testInvalidMultipleConditions() throws ItemNotFoundException {
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value,itemname invalid")));
        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);
        when(mockItemRegistry.getItem(any())).thenThrow(ItemNotFoundException.class);

        State expectation = OnOffType.ON;
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(0)).sendUpdate(eq(expectation));
    }

    @Test
    public void testSingleConditionMatch() throws ItemNotFoundException {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName eq 'Value'")));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Value"));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = new StringType("NewValue");
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(1)).sendUpdate(eq(expectation));
    }

    @Test
    public void testSingleConditionMatchQuoted() throws ItemNotFoundException {
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName eq 'Value'")));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Value"));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = new StringType("NewValue");
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(1)).sendUpdate(eq(expectation));
    }

    private Item stringItemWithState(String itemName, String value) {
        StringItem item = new StringItem(itemName);
        item.setState(new StringType(value));
        return item;
    }

    /*
     * private Item numberItemWithState(String itemType, String itemName, State value) {
     * NumberItem item = new NumberItem(itemType, itemName, null);
     * item.setState(value);
     * return item;
     * }
     */

    @Test
    public void testMultipleConditionAllMatch() throws ItemNotFoundException {
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", "ItemName eq 'Value', ItemName2 eq 'Value2'")));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Value"));
        when(mockItemRegistry.getItem("ItemName2")).thenReturn(stringItemWithState("ItemName2", "Value2"));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = new StringType("NewValue");
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(1)).sendUpdate(eq(expectation));
    }

    @Test
    public void testMultipleConditionSingleMatch() throws ItemNotFoundException {
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value, ItemName2 eq Value2")));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Value"));
        when(mockItemRegistry.getItem("ItemName2")).thenThrow(ItemNotFoundException.class);

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = new StringType("NewValue");
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(0)).sendUpdate(eq(expectation));
    }

    @Test
    public void testFailingConditionWithMismatchState() throws ItemNotFoundException {
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value", "mismatchState", "UNDEF")));
        when(mockContext.getAcceptedDataTypes()).thenReturn(List.of(UnDefType.class, StringType.class));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Mismatch"));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        profile.onStateUpdateFromHandler(new StringType("ToBeDiscarded"));
        verify(mockCallback, times(1)).sendUpdate(eq(UnDefType.UNDEF));
    }

    @Test
    public void testFailingConditionWithMismatchStateQuoted() throws ItemNotFoundException {
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value", "mismatchState", "'UNDEF'")));
        when(mockContext.getAcceptedDataTypes()).thenReturn(List.of(UnDefType.class, StringType.class));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Mismatch"));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        profile.onStateUpdateFromHandler(new StringType("ToBeDiscarded"));
        verify(mockCallback, times(1)).sendUpdate(eq(new StringType("UNDEF")));
    }

    @Test
    void testParseStateNonQuotes() {
        List<Class<? extends State>> acceptedDataTypes = List.of(UnDefType.class, OnOffType.class, StringType.class);

        when(mockContext.getAcceptedDataTypes()).thenReturn(acceptedDataTypes);
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "")));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);
        assertEquals(UnDefType.UNDEF, profile.parseState("UNDEF", acceptedDataTypes));
        assertEquals(new StringType("UNDEF"), profile.parseState("'UNDEF'", acceptedDataTypes));
        assertEquals(OnOffType.ON, profile.parseState("ON", acceptedDataTypes));
        assertEquals(new StringType("ON"), profile.parseState("'ON'", acceptedDataTypes));
    }

    public static Stream<Arguments> testComparingItemWithValue() {
        NumberItem powerItem = new NumberItem("Number:Power", "powerItem", UNIT_PROVIDER);
        NumberItem decimalItem = new NumberItem("decimalItem");
        StringItem stringItem = new StringItem("stringItem");
        SwitchItem switchItem = new SwitchItem("switchItem");
        DimmerItem dimmerItem = new DimmerItem("dimmerItem");
        ContactItem contactItem = new ContactItem("contactItem");
        RollershutterItem rollershutterItem = new RollershutterItem("rollershutterItem");

        QuantityType q_1500W = QuantityType.valueOf("1500 W");
        DecimalType d_1500 = DecimalType.valueOf("1500");
        StringType s_foo = StringType.valueOf("foo");
        StringType s_NULL = StringType.valueOf("NULL");
        StringType s_UNDEF = StringType.valueOf("UNDEF");
        // StringType s_OPEN = StringType.valueOf("OPEN");

        return Stream.of( //
                // Test various spacing combinations
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem==OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem ==OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem== OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem == OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem  ==   OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "  contactItem==OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem==OPEN  ", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contact Item==OPEN  ", false), //

                // Test swapping lhs and rhs
                Arguments.of(contactItem, OpenClosedType.OPEN, "OPEN==contactItem", true), //
                Arguments.of(decimalItem, d_1500, "10 < decimalItem", true), //
                Arguments.of(decimalItem, d_1500, "1501 > decimalItem", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "1 > dimmerItem", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "0 < dimmerItem", false), //
                Arguments.of(powerItem, q_1500W, "2 kW > powerItem", true), //

                // We should be able to check item state is/isn't UNDEF/NULL

                // First, when the item state is actually an UnDefType
                // An unquoted value UNDEF/NULL should be treated as an UnDefType
                // Only equality comparisons against the matching UnDefType will return true
                // Any other comparisons should return false
                Arguments.of(stringItem, UnDefType.UNDEF, "stringItem == UNDEF", true), //
                Arguments.of(dimmerItem, UnDefType.UNDEF, "dimmerItem == UNDEF", true), //
                Arguments.of(dimmerItem, UnDefType.NULL, "dimmerItem == NULL", true), //
                Arguments.of(dimmerItem, UnDefType.NULL, "dimmerItem == UNDEF", false), //
                Arguments.of(decimalItem, UnDefType.NULL, "decimalItem > 10", false), //
                Arguments.of(decimalItem, UnDefType.NULL, "decimalItem < 10", false), //
                Arguments.of(decimalItem, UnDefType.NULL, "decimalItem == 10", false), //
                Arguments.of(decimalItem, UnDefType.NULL, "decimalItem >= 10", false), //
                Arguments.of(decimalItem, UnDefType.NULL, "decimalItem <= 10", false), //

                // A quoted value (String) isn't UnDefType
                Arguments.of(stringItem, UnDefType.UNDEF, "stringItem == 'UNDEF'", false), //
                Arguments.of(stringItem, UnDefType.UNDEF, "stringItem != 'UNDEF'", true), //
                Arguments.of(stringItem, UnDefType.NULL, "stringItem == 'NULL'", false), //
                Arguments.of(stringItem, UnDefType.NULL, "stringItem != 'NULL'", true), //

                // When the item state is not an UnDefType
                // UnDefType is special. When unquoted and comparing against a StringItem,
                // don't treat it as a string
                Arguments.of(stringItem, s_NULL, "stringItem == 'NULL'", true), // Comparing String to String
                Arguments.of(stringItem, s_NULL, "stringItem == NULL", false), // String state != UnDefType
                Arguments.of(stringItem, s_NULL, "stringItem != NULL", true), //
                Arguments.of(stringItem, s_UNDEF, "stringItem == 'UNDEF'", true), // Comparing String to String
                Arguments.of(stringItem, s_UNDEF, "stringItem == UNDEF", false), // String state != UnDefType
                Arguments.of(stringItem, s_UNDEF, "stringItem != UNDEF", true), //

                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem == UNDEF", false), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem != UNDEF", true), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem == NULL", false), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem != NULL", true), //

                // Check for OPEN/CLOSED
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem == OPEN", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem != 'OPEN'", true), // String != Enum
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem != CLOSED", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem == CLOSED", false), //
                Arguments.of(contactItem, OpenClosedType.CLOSED, "contactItem == CLOSED", true), //
                Arguments.of(contactItem, OpenClosedType.CLOSED, "contactItem != OPEN", true), //

                // ON/OFF
                Arguments.of(switchItem, OnOffType.ON, "switchItem == ON", true), //
                Arguments.of(switchItem, OnOffType.ON, "switchItem != ON", false), //
                Arguments.of(switchItem, OnOffType.ON, "switchItem != OFF", true), //
                Arguments.of(switchItem, OnOffType.ON, "switchItem != UNDEF", true), //
                Arguments.of(switchItem, UnDefType.UNDEF, "switchItem == UNDEF", true), //
                Arguments.of(switchItem, OnOffType.ON, "switchItem == 'ON'", false), // it's not a string
                Arguments.of(switchItem, OnOffType.ON, "switchItem != 'ON'", true), // incompatible types

                // Enum types != String
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem == 'OPEN'", false), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem != 'CLOSED'", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem != 'OPEN'", true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "contactItem == 'CLOSED'", false), //
                Arguments.of(contactItem, OpenClosedType.CLOSED, "contactItem == 'CLOSED'", false), //
                Arguments.of(contactItem, OpenClosedType.CLOSED, "contactItem != 'CLOSED'", true), //

                // non UnDefType checks
                // String constants must be quoted
                Arguments.of(stringItem, s_foo, "stringItem == 'foo'", true), //
                Arguments.of(stringItem, s_foo, "stringItem == foo", false), //
                Arguments.of(stringItem, s_foo, "stringItem != foo", true), // not quoted -> not a string
                Arguments.of(stringItem, s_foo, "stringItem <> foo", true), //
                Arguments.of(stringItem, s_foo, "stringItem != 'foo'", false), //
                Arguments.of(stringItem, s_foo, "stringItem <> 'foo'", false), //

                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem == 100", true), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem >= 100", true), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem > 50", true), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem >= 50", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem < 50", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem >= 50", false), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem >= 0", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem < 0", false), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem <= 0", true), //

                // Numeric vs Strings aren't comparable
                Arguments.of(rollershutterItem, PercentType.HUNDRED, "rollershutterItem == '100'", false), //
                Arguments.of(rollershutterItem, PercentType.HUNDRED, "rollershutterItem != '100'", true), //
                Arguments.of(rollershutterItem, PercentType.HUNDRED, "rollershutterItem > '10'", false), //
                Arguments.of(powerItem, q_1500W, "powerItem == '1500 W'", false), // QuantityType vs String => fail
                Arguments.of(decimalItem, d_1500, "decimalItem == '1500'", false), //

                // Compatible type castings are supported
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem == OFF", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem == ON", false), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem != ON", true), //
                Arguments.of(dimmerItem, PercentType.ZERO, "dimmerItem != OFF", false), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem == ON", true), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem == OFF", false), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem != ON", false), //
                Arguments.of(dimmerItem, PercentType.HUNDRED, "dimmerItem != OFF", true), //

                // UpDownType gets converted to PercentType for comparison
                Arguments.of(rollershutterItem, PercentType.HUNDRED, "rollershutterItem == DOWN", true), //
                Arguments.of(rollershutterItem, PercentType.HUNDRED, "rollershutterItem == UP", false), //
                Arguments.of(rollershutterItem, PercentType.HUNDRED, "rollershutterItem != UP", true), //
                Arguments.of(rollershutterItem, PercentType.ZERO, "rollershutterItem == UP", true), //
                Arguments.of(rollershutterItem, PercentType.ZERO, "rollershutterItem != DOWN", true), //

                Arguments.of(decimalItem, d_1500, "decimalItem eq 1500", true), //
                Arguments.of(decimalItem, d_1500, "decimalItem == 1500", true), //

                Arguments.of(powerItem, q_1500W, "powerItem eq 1500", false), // no unit => fail
                Arguments.of(powerItem, q_1500W, "powerItem == 1500", false), // no unit => fail
                Arguments.of(powerItem, q_1500W, "powerItem eq 1500 cm", false), // wrong unit
                Arguments.of(powerItem, q_1500W, "powerItem == 1500 cm", false), // wrong unit

                Arguments.of(powerItem, q_1500W, "powerItem eq 1500 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem eq 1.5 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem eq 2 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem == 1500 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem == 1.5 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem == 2 kW", false), //

                Arguments.of(powerItem, q_1500W, "powerItem neq 500 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem neq 1500", true), // Not the same type, so not equal
                Arguments.of(powerItem, q_1500W, "powerItem neq 1500 W", false), //
                Arguments.of(powerItem, q_1500W, "powerItem neq 1.5 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem != 500 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem != 1500", true), // not the same type
                Arguments.of(powerItem, q_1500W, "powerItem != 1500 W", false), //
                Arguments.of(powerItem, q_1500W, "powerItem != 1.5 kW", false), //

                Arguments.of(powerItem, q_1500W, "powerItem GT 100 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem GT 1 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem GT 2 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem > 100 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem > 1 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem > 2 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem GTE 1500 W", true), //
                Arguments.of(powerItem, q_1500W, "powerItem GTE 1 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem GTE 1.5 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem GTE 2 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem GTE 2000 mW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem GTE 20", false), // no unit
                Arguments.of(powerItem, q_1500W, "powerItem >= 1.5 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem >= 2 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem LT 2 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem < 2 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem LTE 2 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem <= 2 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem <= 1 kW", false), //
                Arguments.of(powerItem, q_1500W, "powerItem LTE 1.5 kW", true), //
                Arguments.of(powerItem, q_1500W, "powerItem <= 1.5 kW", true) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testComparingItemWithValue(GenericItem item, State state, String condition, boolean expected)
            throws ItemNotFoundException {
        String itemName = item.getName();
        item.setState(state);

        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", condition)));
        when(mockItemRegistry.getItem(itemName)).thenReturn(item);

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State inputData = new StringType("NewValue");
        profile.onStateUpdateFromHandler(inputData);
        verify(mockCallback, times(expected ? 1 : 0)).sendUpdate(eq(inputData));
    }

    public static Stream<Arguments> testComparingItemWithOtherItem() {
        NumberItem powerItem = new NumberItem("Number:Power", "powerItem", UNIT_PROVIDER);
        NumberItem powerItem2 = new NumberItem("Number:Power", "powerItem2", UNIT_PROVIDER);
        NumberItem decimalItem = new NumberItem("decimalItem");
        NumberItem decimalItem2 = new NumberItem("decimalItem2");
        StringItem stringItem = new StringItem("stringItem");
        StringItem stringItem2 = new StringItem("stringItem2");
        ContactItem contactItem = new ContactItem("contactItem");
        ContactItem contactItem2 = new ContactItem("contactItem2");

        QuantityType q_1500W = QuantityType.valueOf("1500 W");
        QuantityType q_1_5kW = QuantityType.valueOf("1.5 kW");
        QuantityType q_10kW = QuantityType.valueOf("10 kW");

        DecimalType d_1500 = DecimalType.valueOf("1500");
        DecimalType d_2000 = DecimalType.valueOf("2000");
        StringType s_1500 = StringType.valueOf("1500");
        StringType s_foo = StringType.valueOf("foo");
        StringType s_NULL = StringType.valueOf("NULL");

        return Stream.of( //
                Arguments.of(stringItem, s_foo, "==", stringItem2, s_foo, true), //
                Arguments.of(stringItem, s_foo, "!=", stringItem2, s_foo, false), //
                Arguments.of(stringItem, s_foo, "==", stringItem2, s_NULL, false), //
                Arguments.of(stringItem, s_foo, "!=", stringItem2, s_NULL, true), //

                Arguments.of(decimalItem, d_1500, "==", decimalItem2, d_1500, true), //
                Arguments.of(decimalItem, d_1500, "==", decimalItem2, d_1500, true), //

                // UNDEF/NULL are equals regardless of item type
                Arguments.of(decimalItem, UnDefType.UNDEF, "==", stringItem, UnDefType.UNDEF, true), //
                Arguments.of(decimalItem, UnDefType.NULL, "==", stringItem, UnDefType.NULL, true), //
                Arguments.of(decimalItem, UnDefType.NULL, "==", stringItem, UnDefType.UNDEF, false), //

                Arguments.of(contactItem, OpenClosedType.OPEN, "==", contactItem2, OpenClosedType.OPEN, true), //
                Arguments.of(contactItem, OpenClosedType.OPEN, "==", contactItem2, OpenClosedType.CLOSED, false), //

                Arguments.of(decimalItem, d_1500, "==", decimalItem2, d_1500, true), //
                Arguments.of(decimalItem, d_1500, "<", decimalItem2, d_2000, true), //
                Arguments.of(decimalItem, d_1500, ">", decimalItem2, d_2000, false), //
                Arguments.of(decimalItem, d_1500, ">", stringItem, s_1500, false), //
                Arguments.of(powerItem, q_1500W, "<", powerItem2, q_10kW, true), //
                Arguments.of(powerItem, q_1500W, ">", powerItem2, q_10kW, false), //
                Arguments.of(powerItem, q_1500W, "==", powerItem2, q_1_5kW, true), //
                Arguments.of(powerItem, q_1500W, ">=", powerItem2, q_1_5kW, true), //
                Arguments.of(powerItem, q_1500W, ">", powerItem2, q_1_5kW, false), //

                // Incompatible types
                Arguments.of(decimalItem, d_1500, "==", stringItem, s_1500, false), //
                Arguments.of(powerItem, q_1500W, "==", decimalItem, d_1500, false), // DecimalType != QuantityType
                Arguments.of(decimalItem, d_1500, "==", powerItem, q_1500W, false) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testComparingItemWithOtherItem(GenericItem item, State state, String operator, GenericItem item2,
            State state2, boolean expected) throws ItemNotFoundException {
        String itemName = item.getName();
        item.setState(state);

        String itemName2 = item2.getName();
        item2.setState(state2);

        if (item.equals(item2)) {
            // For test writers:
            // When using the same items, it doesn't make sense for their states to be different
            assertEquals(state, state2);
        }

        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", itemName + operator + itemName2)));
        when(mockItemRegistry.getItem(itemName)).thenReturn(item);
        when(mockItemRegistry.getItem(itemName2)).thenReturn(item2);

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State inputData = new StringType("NewValue");
        profile.onStateUpdateFromHandler(inputData);
        verify(mockCallback, times(expected ? 1 : 0)).sendUpdate(eq(inputData));
    }

    public static Stream<Arguments> testComparingInputStateWithValue() {
        NumberItem powerItem = new NumberItem("Number:Power", "ItemName", UNIT_PROVIDER);
        NumberItem decimalItem = new NumberItem("ItemName");
        StringItem stringItem = new StringItem("ItemName");
        DimmerItem dimmerItem = new DimmerItem("ItemName");

        QuantityType q_1500W = QuantityType.valueOf("1500 W");
        DecimalType d_1500 = DecimalType.valueOf("1500");
        StringType s_foo = StringType.valueOf("foo");

        return Stream.of( //
                // We should be able to check that input state is/isn't UNDEF/NULL

                // First, when the input state is actually an UnDefType
                // An unquoted value UNDEF/NULL should be treated as an UnDefType
                Arguments.of(stringItem, UnDefType.UNDEF, "==", "UNDEF", true), //
                Arguments.of(dimmerItem, UnDefType.NULL, "==", "NULL", true), //
                Arguments.of(dimmerItem, UnDefType.NULL, "==", "UNDEF", false), //

                // A quoted value (String) isn't UnDefType
                Arguments.of(stringItem, UnDefType.UNDEF, "==", "'UNDEF'", false), //
                Arguments.of(stringItem, UnDefType.UNDEF, "!=", "'UNDEF'", true), //
                Arguments.of(stringItem, UnDefType.NULL, "==", "'NULL'", false), //
                Arguments.of(stringItem, UnDefType.NULL, "!=", "'NULL'", true), //

                // String values must be quoted
                Arguments.of(stringItem, s_foo, "==", "'foo'", true), //
                Arguments.of(stringItem, s_foo, "!=", "'foo'", false), //
                Arguments.of(stringItem, s_foo, "==", "'bar'", false), //
                // Unquoted string values are not compatible
                // always returns false
                Arguments.of(stringItem, s_foo, "==", "foo", false), //
                Arguments.of(stringItem, s_foo, "!=", "foo", true), // not quoted -> not equal to string

                Arguments.of(decimalItem, d_1500, "==", "1500", true), //
                Arguments.of(decimalItem, d_1500, "!=", "1500", false), //
                Arguments.of(decimalItem, d_1500, "==", "1000", false), //
                Arguments.of(decimalItem, d_1500, "!=", "1000", true), //
                Arguments.of(decimalItem, d_1500, ">", "1000", true), //
                Arguments.of(decimalItem, d_1500, ">=", "1000", true), //
                Arguments.of(decimalItem, d_1500, ">=", "1500", true), //
                Arguments.of(decimalItem, d_1500, "<", "1600", true), //
                Arguments.of(decimalItem, d_1500, "<=", "1600", true), //
                Arguments.of(decimalItem, d_1500, "<", "1000", false), //
                Arguments.of(decimalItem, d_1500, "<=", "1000", false), //
                Arguments.of(decimalItem, d_1500, "<", "1500", false), //
                Arguments.of(decimalItem, d_1500, "<=", "1500", true), //

                // named operators - must have a trailing space
                Arguments.of(decimalItem, d_1500, "LT ", "2000", true), //
                Arguments.of(decimalItem, d_1500, "LTE ", "1500", true), //
                Arguments.of(decimalItem, d_1500, " LTE ", "1500", true), //
                Arguments.of(decimalItem, d_1500, " LTE ", "1500", true), //

                Arguments.of(powerItem, q_1500W, "==", "1500 W", true), //
                Arguments.of(powerItem, q_1500W, "==", "'1500 W'", false), // QuantityType != String
                Arguments.of(powerItem, q_1500W, "==", "1.5 kW", true), //
                Arguments.of(powerItem, q_1500W, ">", "2000 mW", true) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testComparingInputStateWithValue(GenericItem linkedItem, State inputState, String operator,
            String value, boolean expected) throws ItemNotFoundException {
        String linkedItemName = linkedItem.getName();

        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", operator + value)));
        when(mockItemRegistry.getItem(linkedItemName)).thenReturn(linkedItem);
        when(mockItemChannelLink.getItemName()).thenReturn(linkedItemName);

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        profile.onStateUpdateFromHandler(inputState);
        verify(mockCallback, times(expected ? 1 : 0)).sendUpdate(eq(inputState));
    }

    @ParameterizedTest
    @MethodSource("testComparingItemWithOtherItem")
    public void testComparingInputStateWithItem(GenericItem linkedItem, State inputState, String operator,
            GenericItem item, State state, boolean expected) throws ItemNotFoundException {
        String linkedItemName = linkedItem.getName();

        String itemName = item.getName();

        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", operator + itemName)));
        when(mockItemRegistry.getItem(itemName)).thenReturn(item);
        when(mockItemRegistry.getItem(linkedItemName)).thenReturn(linkedItem);
        when(mockItemChannelLink.getItemName()).thenReturn(linkedItemName);

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);
        item.setState(UnDefType.UNDEF);

        profile.onStateUpdateFromHandler(inputState);
        reset(mockCallback);
        when(mockCallback.getItemChannelLink()).thenReturn(mockItemChannelLink);

        item.setState(state);
        profile.onStateUpdateFromHandler(inputState);
        verify(mockCallback, times(expected ? 1 : 0)).sendUpdate(eq(inputState));
    }

    public static Stream<Arguments> testFunctions() {
        NumberItem powerItem = new NumberItem("Number:Power", "powerItem", UNIT_PROVIDER);
        NumberItem decimalItem = new NumberItem("decimalItem");
        List<Number> numbers = List.of(1, 2, 3, 4, 5);
        List<QuantityType> quantities = numbers.stream().map(n -> new QuantityType(n, Units.WATT)).toList();
        List<DecimalType> decimals = numbers.stream().map(DecimalType::new).toList();

        return Stream.of( //
                // test custom window size
                Arguments.of(decimalItem, "$AVERAGE(3) == 4", decimals, DecimalType.valueOf("5"), true), //
                Arguments.of(decimalItem, "$AVERAGE(4) == 3.5", decimals, DecimalType.valueOf("5"), true), //

                // default window size is 5
                Arguments.of(decimalItem, "10 <= $DELTA", decimals, DecimalType.valueOf("10"), false), //
                Arguments.of(decimalItem, "10 <= $DELTA", decimals, DecimalType.valueOf("11"), true), //
                Arguments.of(decimalItem, "1 <= $DELTA", decimals, DecimalType.valueOf("5.5"), false), //
                Arguments.of(decimalItem, "1 <= $DELTA", decimals, DecimalType.valueOf("6"), true), //
                Arguments.of(decimalItem, "$DELTA >= 1", decimals, DecimalType.valueOf("10"), true), //
                Arguments.of(decimalItem, "$DELTA >= 1", decimals, DecimalType.valueOf("5.5"), false), //

                Arguments.of(decimalItem, "$DELTA_PERCENT >= 10", decimals, DecimalType.valueOf("4.6"), false), //
                Arguments.of(decimalItem, "$DELTA_PERCENT >= 10", decimals, DecimalType.valueOf("4.5"), true), //
                Arguments.of(decimalItem, "$DELTA_PERCENT >= 10", decimals, DecimalType.valueOf("5.4"), false), //
                Arguments.of(decimalItem, "$DELTA_PERCENT >= 10", decimals, DecimalType.valueOf("5.5"), true), //
                Arguments.of(decimalItem, "$DELTA_PERCENT >= 10", decimals, DecimalType.valueOf("6"), true), //

                Arguments.of(decimalItem, ">= 10 %", decimals, DecimalType.valueOf("4.6"), false), //
                Arguments.of(decimalItem, ">= 10%", decimals, DecimalType.valueOf("4.6"), false), //
                Arguments.of(decimalItem, ">= 10%", decimals, DecimalType.valueOf("4.5"), true), //
                Arguments.of(decimalItem, ">= 10%", decimals, DecimalType.valueOf("5.4"), false), //
                Arguments.of(decimalItem, ">= 10%", decimals, DecimalType.valueOf("5.5"), true), //
                Arguments.of(decimalItem, ">= 10%", decimals, DecimalType.valueOf("6"), true), //

                // The following will only accept new data if it is within 10% of the previously accepted data.
                // so the second and subsequent initial data (i.e.: 2, 3, 4, 5) will be rejected.
                // The new data is compared against the first (1)
                Arguments.of(decimalItem, "$DELTA_PERCENT < 10", decimals, DecimalType.valueOf("1.09"), true), //
                Arguments.of(decimalItem, "$DELTA_PERCENT < 10", decimals, DecimalType.valueOf("1.11"), false), //
                Arguments.of(decimalItem, "$DELTA_PERCENT < 10", decimals, DecimalType.valueOf("0.91"), true), //
                Arguments.of(decimalItem, "$DELTA_PERCENT < 10", decimals, DecimalType.valueOf("0.89"), false), //

                Arguments.of(decimalItem, "< 10%", decimals, DecimalType.valueOf("1.09"), true), //
                Arguments.of(decimalItem, "< 10%", decimals, DecimalType.valueOf("1.11"), false), //
                Arguments.of(decimalItem, "< 10%", decimals, DecimalType.valueOf("0.91"), true), //
                Arguments.of(decimalItem, "< 10%", decimals, DecimalType.valueOf("0.89"), false), //

                Arguments.of(decimalItem, "1 == $MIN", decimals, DecimalType.valueOf("20"), true), //
                Arguments.of(decimalItem, "0 < $MIN", decimals, DecimalType.valueOf("20"), true), //
                Arguments.of(decimalItem, "$MIN > 0", decimals, DecimalType.valueOf("20"), true), //

                Arguments.of(decimalItem, "< $MIN", decimals, DecimalType.valueOf("20"), false), //
                Arguments.of(decimalItem, "< $MIN", decimals, DecimalType.valueOf("-1"), true), //
                Arguments.of(decimalItem, "> $MIN", decimals, DecimalType.valueOf("-1"), false), //
                Arguments.of(decimalItem, "> $MIN", decimals, DecimalType.valueOf("2"), true), //

                Arguments.of(decimalItem, "1 == $MAX", decimals, DecimalType.valueOf("20"), false), //
                Arguments.of(decimalItem, "0 < $MAX", decimals, DecimalType.valueOf("20"), true), //
                Arguments.of(decimalItem, "6 < $MAX", decimals, DecimalType.valueOf("20"), false), //
                Arguments.of(decimalItem, "6 > $MAX", decimals, DecimalType.valueOf("1"), true), //

                Arguments.of(decimalItem, "< $MAX", decimals, DecimalType.valueOf("20"), false), //
                Arguments.of(decimalItem, "< $MAX", decimals, DecimalType.valueOf("-1"), true), //
                Arguments.of(decimalItem, "> $MAX", decimals, DecimalType.valueOf("-1"), false), //
                Arguments.of(decimalItem, "> $MAX", decimals, DecimalType.valueOf("20"), true), //

                Arguments.of(decimalItem, "$MEDIAN < 4", decimals, DecimalType.valueOf("2"), true), //
                Arguments.of(decimalItem, "$MEDIAN(3) < 4", decimals, DecimalType.valueOf("2"), false), //

                Arguments.of(decimalItem, "$STDDEV(3) > 0.8", decimals, DecimalType.valueOf("2"), true), //
                Arguments.of(decimalItem, "$STDDEV > 1.4", decimals, DecimalType.valueOf("2"), true), //
                Arguments.of(decimalItem, "$STDDEV < 1.5", decimals, DecimalType.valueOf("2"), true), //
                // Make sure STDDEV's unit is correct
                Arguments.of(powerItem, "$STDDEV < 1.5 W", quantities, QuantityType.valueOf("2 W"), true), //
                Arguments.of(powerItem, "$STDDEV < 1.5 W²", quantities, QuantityType.valueOf("2 W"), false), //

                Arguments.of(decimalItem, "< $AVERAGE", decimals, DecimalType.valueOf("2"), true), //
                Arguments.of(powerItem, "== $AVERAGE", quantities, QuantityType.valueOf("3 W"), true), //
                Arguments.of(powerItem, "== $AVERAGE", quantities, DecimalType.valueOf("3"), false), //
                Arguments.of(powerItem, "> $AVERAGE", quantities, QuantityType.valueOf("2 W"), false), //
                Arguments.of(powerItem, "> $AVERAGE", quantities, QuantityType.valueOf("4 W"), true), //

                Arguments.of(decimalItem, "2 < $AVERAGE", decimals, DecimalType.valueOf("10"), true), //
                Arguments.of(decimalItem, "$AVERAGE > 2", decimals, DecimalType.valueOf("10"), true), //
                Arguments.of(powerItem, "3 W == $AVERAGE", quantities, QuantityType.valueOf("100 W"), true), //
                Arguments.of(powerItem, "3 == $AVERAGE", quantities, QuantityType.valueOf("100 W"), false) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFunctions(Item item, String condition, List<State> states, State input, boolean expected)
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
