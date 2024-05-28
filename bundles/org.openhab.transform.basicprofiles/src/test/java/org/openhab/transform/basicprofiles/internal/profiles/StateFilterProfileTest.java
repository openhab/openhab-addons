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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
public class StateFilterProfileTest {

    private @Mock @NonNullByDefault({}) ProfileCallback mockCallback;
    private @Mock @NonNullByDefault({}) ProfileContext mockContext;
    private @Mock @NonNullByDefault({}) ItemRegistry mockItemRegistry;

    @BeforeEach
    public void setup() {
        reset(mockContext);
        reset(mockCallback);
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
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName lt Value")));
        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);
        when(mockItemRegistry.getItem(any())).thenThrow(ItemNotFoundException.class);

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
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value")));
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

    @Test
    public void testMultipleCondition_AllMatch() throws ItemNotFoundException {
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("conditions", "ItemName eq Value, ItemName2 eq Value2")));
        when(mockItemRegistry.getItem("ItemName")).thenReturn(stringItemWithState("ItemName", "Value"));
        when(mockItemRegistry.getItem("ItemName2")).thenReturn(stringItemWithState("ItemName2", "Value2"));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);

        State expectation = new StringType("NewValue");
        profile.onStateUpdateFromHandler(expectation);
        verify(mockCallback, times(1)).sendUpdate(eq(expectation));
    }

    @Test
    public void testMultipleCondition_SingleMatch() throws ItemNotFoundException {
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
        when(mockContext.getAcceptedDataTypes())
                .thenReturn(List.of(UnDefType.class, OnOffType.class, StringType.class));
        when(mockContext.getConfiguration()).thenReturn(new Configuration(Map.of("conditions", "")));

        StateFilterProfile profile = new StateFilterProfile(mockCallback, mockContext, mockItemRegistry);
        assertEquals(UnDefType.UNDEF, profile.parseState("UNDEF"));
        assertEquals(new StringType("UNDEF"), profile.parseState("'UNDEF'"));
        assertEquals(OnOffType.ON, profile.parseState("ON"));
        assertEquals(new StringType("ON"), profile.parseState("'ON'"));
    }
}
