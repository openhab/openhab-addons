/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.deutschebahn.internal.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.deutschebahn.internal.AttributeSelection;
import org.openhab.binding.deutschebahn.internal.EventAttribute;
import org.openhab.binding.deutschebahn.internal.EventAttributeSelection;
import org.openhab.binding.deutschebahn.internal.EventType;
import org.openhab.binding.deutschebahn.internal.TripLabelAttribute;

/**
 * Tests for {@link FilterParser}
 *
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public class FilterParserTest {

    private static void checkAttributeFilter(TimetableStopPredicate predicate, ChannelNameEquals channelEquals,
            EventType eventType, EventAttribute<?, ?> eventAttribute) {
        checkAttributeFilter(predicate, channelEquals, new EventAttributeSelection(eventType, eventAttribute));
    }

    private static void checkAttributeFilter(TimetableStopPredicate predicate, ChannelNameEquals channelEquals,
            AttributeSelection attributeSelection) {
        assertThat(predicate, is(instanceOf(TimetableStopByStringEventAttributeFilter.class)));
        TimetableStopByStringEventAttributeFilter attributeFilter = (TimetableStopByStringEventAttributeFilter) predicate;
        assertThat(attributeFilter.getFilter(), is(channelEquals.getFilterValue()));
        assertThat(attributeFilter.getAttributeSelection(), is(attributeSelection));
    }

    private static OrPredicate assertOr(TimetableStopPredicate predicate) {
        assertThat(predicate, is(instanceOf(OrPredicate.class)));
        return (OrPredicate) predicate;
    }

    private static AndPredicate assertAnd(TimetableStopPredicate predicate) {
        assertThat(predicate, is(instanceOf(AndPredicate.class)));
        return (AndPredicate) predicate;
    }

    @Test
    public void testParseSimple() throws FilterParserException {
        final List<FilterToken> input = new ArrayList<>();
        ChannelNameEquals channelEquals = new ChannelNameEquals("trip", "number", Pattern.compile("20"));
        input.add(channelEquals);
        final TimetableStopPredicate result = FilterParser.parse(input);
        checkAttributeFilter(result, channelEquals, TripLabelAttribute.N);
    }

    @Test
    public void testParseAnd() throws FilterParserException {
        final List<FilterToken> input = new ArrayList<>();
        ChannelNameEquals channelEquals01 = new ChannelNameEquals("trip", "number", Pattern.compile("20"));
        input.add(channelEquals01);
        input.add(OperatorToken.AND);
        ChannelNameEquals channelEquals02 = new ChannelNameEquals("trip", "number", Pattern.compile("30"));
        input.add(channelEquals02);
        final TimetableStopPredicate result = FilterParser.parse(input);
        final AndPredicate andPredicate = assertAnd(result);

        checkAttributeFilter(andPredicate.getFirst(), channelEquals01, TripLabelAttribute.N);
        checkAttributeFilter(andPredicate.getSecond(), channelEquals02, TripLabelAttribute.N);
    }

    @Test
    public void testParseOr() throws FilterParserException {
        final List<FilterToken> input = new ArrayList<>();
        ChannelNameEquals channelEquals01 = new ChannelNameEquals("trip", "number", Pattern.compile("20"));
        input.add(channelEquals01);
        input.add(OperatorToken.OR);
        ChannelNameEquals channelEquals02 = new ChannelNameEquals("trip", "number", Pattern.compile("30"));
        input.add(channelEquals02);
        final TimetableStopPredicate result = FilterParser.parse(input);
        final OrPredicate orPredicate = assertOr(result);

        checkAttributeFilter(orPredicate.getFirst(), channelEquals01, TripLabelAttribute.N);
        checkAttributeFilter(orPredicate.getSecond(), channelEquals02, TripLabelAttribute.N);
    }

    @Test
    public void testParseWithBrackets() throws FilterParserException {
        final List<FilterToken> input = new ArrayList<>();
        ChannelNameEquals channelEquals01 = new ChannelNameEquals("trip", "number", Pattern.compile("20"));
        input.add(channelEquals01);
        input.add(OperatorToken.AND);
        input.add(OperatorToken.BRACKET_OPEN);

        ChannelNameEquals channelEquals02 = new ChannelNameEquals("departure", "line", Pattern.compile("RE10"));
        input.add(channelEquals02);
        input.add(OperatorToken.OR);
        ChannelNameEquals channelEquals03 = new ChannelNameEquals("departure", "line", Pattern.compile("RE20"));
        input.add(channelEquals03);
        input.add(OperatorToken.BRACKET_CLOSE);

        final TimetableStopPredicate result = FilterParser.parse(input);
        final AndPredicate andPredicate = assertAnd(result);

        checkAttributeFilter(andPredicate.getFirst(), channelEquals01, TripLabelAttribute.N);
        final OrPredicate orPredicate = assertOr(andPredicate.getSecond());

        checkAttributeFilter(orPredicate.getFirst(), channelEquals02, EventType.DEPARTURE, EventAttribute.L);
        checkAttributeFilter(orPredicate.getSecond(), channelEquals03, EventType.DEPARTURE, EventAttribute.L);
    }

    @Test
    public void testParseWithMultipleBrackets() throws FilterParserException {
        final List<FilterToken> input = new ArrayList<>();
        input.add(OperatorToken.BRACKET_OPEN);
        input.add(OperatorToken.BRACKET_OPEN);
        ChannelNameEquals channelEquals01 = new ChannelNameEquals("trip", "number", Pattern.compile("20"));
        input.add(channelEquals01);
        input.add(OperatorToken.AND);
        ChannelNameEquals channelEquals02 = new ChannelNameEquals("departure", "line", Pattern.compile("RE22"));
        input.add(channelEquals02);
        input.add(OperatorToken.BRACKET_CLOSE);
        input.add(OperatorToken.OR);
        input.add(OperatorToken.BRACKET_OPEN);
        ChannelNameEquals channelEquals03 = new ChannelNameEquals("trip", "number", Pattern.compile("30"));
        input.add(channelEquals03);
        input.add(OperatorToken.AND);
        ChannelNameEquals channelEquals04 = new ChannelNameEquals("departure", "line", Pattern.compile("RE33"));
        input.add(channelEquals04);
        input.add(OperatorToken.BRACKET_CLOSE);
        input.add(OperatorToken.BRACKET_CLOSE);

        final TimetableStopPredicate result = FilterParser.parse(input);
        final OrPredicate orPredicate = assertOr(result);

        final AndPredicate firstAnd = assertAnd(orPredicate.getFirst());
        checkAttributeFilter(firstAnd.getFirst(), channelEquals01, TripLabelAttribute.N);
        checkAttributeFilter(firstAnd.getSecond(), channelEquals02, EventType.DEPARTURE, EventAttribute.L);

        final AndPredicate secondAnd = assertAnd(orPredicate.getSecond());
        checkAttributeFilter(secondAnd.getFirst(), channelEquals03, TripLabelAttribute.N);
        checkAttributeFilter(secondAnd.getSecond(), channelEquals04, EventType.DEPARTURE, EventAttribute.L);
    }

    @Test
    public void testParseErrors() {
        final ChannelNameEquals channelEquals = new ChannelNameEquals("trip", "number", Pattern.compile("20"));
        try {
            FilterParser.parse(Collections.emptyList());
            fail();
        } catch (FilterParserException e) {
        }

        try {
            FilterParser.parse(Arrays.asList(OperatorToken.AND));
            fail();
        } catch (FilterParserException e) {
        }

        try {
            FilterParser.parse(Arrays.asList(OperatorToken.OR));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(OperatorToken.BRACKET_OPEN));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(OperatorToken.BRACKET_CLOSE));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(OperatorToken.BRACKET_OPEN, OperatorToken.BRACKET_CLOSE));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(OperatorToken.BRACKET_OPEN, OperatorToken.OR));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(OperatorToken.BRACKET_OPEN, OperatorToken.AND));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, OperatorToken.AND, OperatorToken.BRACKET_OPEN));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, OperatorToken.AND, OperatorToken.BRACKET_CLOSE));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, OperatorToken.OR, OperatorToken.BRACKET_OPEN));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, OperatorToken.OR, OperatorToken.BRACKET_CLOSE));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, OperatorToken.AND));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, OperatorToken.OR));
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(Arrays.asList(channelEquals, channelEquals));
            fail();
        } catch (FilterParserException e) {
        }
    }
}
