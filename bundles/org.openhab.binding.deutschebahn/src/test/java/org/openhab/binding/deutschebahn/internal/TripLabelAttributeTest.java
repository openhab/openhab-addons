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
package org.openhab.binding.deutschebahn.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TripLabel;
import org.openhab.binding.deutschebahn.internal.timetable.dto.TripType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

/**
 * Tests Mapping from {@link TripLabel} attribute values to openhab state values.
 * 
 * @author Sönke Küper - initial contribution.
 */
@NonNullByDefault
@SuppressWarnings("unchecked")
public class TripLabelAttributeTest {

    private <VALUE_TYPE, STATE_TYPE extends State> void doTestTripAttribute( //
            String channelName, //
            @Nullable String expectedChannelName, //
            Consumer<TripLabel> setValue, //
            VALUE_TYPE expectedValue, //
            @Nullable STATE_TYPE expectedState, //
            boolean performSetterTest) { //
        final TripLabelAttribute<VALUE_TYPE, STATE_TYPE> attribute = (TripLabelAttribute<VALUE_TYPE, STATE_TYPE>) TripLabelAttribute
                .getByChannelName(channelName);
        assertThat(attribute, is(not(nullValue())));
        assertThat(attribute.getChannelTypeName(), is(expectedChannelName == null ? channelName : expectedChannelName));
        assertThat(attribute.getValue(new TripLabel()), is(nullValue()));
        assertThat(attribute.getState(new TripLabel()), is(nullValue()));

        // Create an trip label and set the attribute value.
        final TripLabel labelWithValueSet = new TripLabel();
        setValue.accept(labelWithValueSet);

        // then try get value and state.
        assertThat(attribute.getValue(labelWithValueSet), is(expectedValue));
        assertThat(attribute.getState(labelWithValueSet), is(expectedState));

        // Try set Value in new Event
        final TripLabel copyTarget = new TripLabel();
        attribute.setValue(copyTarget, expectedValue);
        if (performSetterTest) {
            assertThat(attribute.getValue(copyTarget), is(expectedValue));
        }
    }

    @Test
    public void testGetNonExistingChannel() {
        assertThat(TripLabelAttribute.getByChannelName("unkownChannel"), is(nullValue()));
    }

    @Test
    public void testCategory() {
        final String category = "ICE";
        doTestTripAttribute("category", null, (TripLabel e) -> e.setC(category), category, new StringType(category),
                true);
    }

    @Test
    public void testNumber() {
        final String number = "4567";
        doTestTripAttribute("number", null, (TripLabel e) -> e.setN(number), number, new StringType(number), true);
    }

    @Test
    public void testOwner() {
        final String owner = "W3";
        doTestTripAttribute("owner", null, (TripLabel e) -> e.setO(owner), owner, new StringType(owner), true);
    }

    @Test
    public void testFilterFlages() {
        final String filter = "a";
        doTestTripAttribute("filter-flags", null, (TripLabel e) -> e.setF(filter), filter, new StringType(filter),
                true);
    }

    @Test
    public void testTripType() {
        final TripType type = TripType.E;
        doTestTripAttribute("trip-type", null, (TripLabel e) -> e.setT(type), type, new StringType("e"), true);
    }
}
