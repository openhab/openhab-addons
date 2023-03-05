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

    private static final class FilterTokenSequenceBuilder {

        private final List<FilterToken> tokens = new ArrayList<>();
        private int position = 0;

        private int getPos() {
            this.position++;
            return this.position;
        }

        public List<FilterToken> build() {
            return this.tokens;
        }

        public FilterTokenSequenceBuilder and() {
            this.tokens.add(new AndOperator(getPos()));
            return this;
        }

        public FilterTokenSequenceBuilder or() {
            this.tokens.add(new OrOperator(getPos()));
            return this;
        }

        public FilterTokenSequenceBuilder bracketOpen() {
            this.tokens.add(new BracketOpenToken(getPos()));
            return this;
        }

        public FilterTokenSequenceBuilder bracketClose() {
            this.tokens.add(new BracketCloseToken(getPos()));
            return this;
        }

        public ChannelNameEquals channelFilter(String channelGroup, String channelName, String pattern) {
            ChannelNameEquals channelNameEquals = new ChannelNameEquals(getPos(), channelGroup, channelName,
                    Pattern.compile(pattern));
            this.tokens.add(channelNameEquals);
            return channelNameEquals;
        }

        public FilterTokenSequenceBuilder channelFilter(ChannelNameEquals equals) {
            this.tokens.add(equals);
            return this;
        }
    }

    private static FilterTokenSequenceBuilder builder() {
        return new FilterTokenSequenceBuilder();
    }

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
        ChannelNameEquals channelEquals = new ChannelNameEquals(1, "trip", "number", Pattern.compile("20"));
        input.add(channelEquals);
        final TimetableStopPredicate result = FilterParser.parse(input);
        checkAttributeFilter(result, channelEquals, TripLabelAttribute.N);
    }

    @Test
    public void testParseAnd() throws FilterParserException {
        final FilterTokenSequenceBuilder b = builder();
        final ChannelNameEquals channelEquals01 = b.channelFilter("trip", "number", "20");
        b.and();
        final ChannelNameEquals channelEquals02 = b.channelFilter("trip", "number", "30");
        final TimetableStopPredicate result = FilterParser.parse(b.build());
        final AndPredicate andPredicate = assertAnd(result);

        checkAttributeFilter(andPredicate.getFirst(), channelEquals01, TripLabelAttribute.N);
        checkAttributeFilter(andPredicate.getSecond(), channelEquals02, TripLabelAttribute.N);
    }

    @Test
    public void testParseOr() throws FilterParserException {
        final FilterTokenSequenceBuilder b = builder();
        final ChannelNameEquals channelEquals01 = b.channelFilter("trip", "number", "20");
        b.or();
        final ChannelNameEquals channelEquals02 = b.channelFilter("trip", "number", "30");
        final TimetableStopPredicate result = FilterParser.parse(b.build());
        final OrPredicate orPredicate = assertOr(result);

        checkAttributeFilter(orPredicate.getFirst(), channelEquals01, TripLabelAttribute.N);
        checkAttributeFilter(orPredicate.getSecond(), channelEquals02, TripLabelAttribute.N);
    }

    @Test
    public void testParseWithBrackets() throws FilterParserException {
        final FilterTokenSequenceBuilder b = new FilterTokenSequenceBuilder();
        final ChannelNameEquals channelEquals01 = b.channelFilter("trip", "number", "20");
        b.and();
        b.bracketOpen();
        final ChannelNameEquals channelEquals02 = b.channelFilter("departure", "line", "RE10");
        b.or();
        final ChannelNameEquals channelEquals03 = b.channelFilter("departure", "line", "RE20");
        b.bracketClose();
        final List<FilterToken> input = b.build();

        final TimetableStopPredicate result = FilterParser.parse(input);
        final AndPredicate andPredicate = assertAnd(result);

        checkAttributeFilter(andPredicate.getFirst(), channelEquals01, TripLabelAttribute.N);
        final OrPredicate orPredicate = assertOr(andPredicate.getSecond());

        checkAttributeFilter(orPredicate.getFirst(), channelEquals02, EventType.DEPARTURE, EventAttribute.L);
        checkAttributeFilter(orPredicate.getSecond(), channelEquals03, EventType.DEPARTURE, EventAttribute.L);
    }

    @Test
    public void testParseWithMultipleBrackets() throws FilterParserException {
        final FilterTokenSequenceBuilder b = builder();
        b.bracketOpen();
        b.bracketOpen();
        final ChannelNameEquals channelEquals01 = b.channelFilter("trip", "number", "20");
        b.and();
        final ChannelNameEquals channelEquals02 = b.channelFilter("departure", "line", "RE22");
        b.bracketClose();
        b.or();
        b.bracketOpen();
        final ChannelNameEquals channelEquals03 = b.channelFilter("trip", "number", "30");
        b.and();
        final ChannelNameEquals channelEquals04 = b.channelFilter("departure", "line", "RE33");
        b.bracketClose();
        b.bracketClose();

        final List<FilterToken> input = b.build();

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
        final ChannelNameEquals channelEquals = new ChannelNameEquals(1, "trip", "number", Pattern.compile("20"));
        try {
            FilterParser.parse(Collections.emptyList());
            fail();
        } catch (FilterParserException e) {
        }

        try {
            FilterParser.parse(builder().and().build());
            fail();
        } catch (FilterParserException e) {
        }

        try {
            FilterParser.parse(builder().or().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().bracketOpen().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().bracketClose().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().bracketOpen().bracketClose().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().bracketOpen().and().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().bracketOpen().and().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().channelFilter(channelEquals).and().bracketOpen().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().channelFilter(channelEquals).and().bracketClose().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().channelFilter(channelEquals).or().bracketOpen().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().channelFilter(channelEquals).or().bracketClose().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().channelFilter(channelEquals).and().build());
            fail();
        } catch (FilterParserException e) {
        }
        try {
            FilterParser.parse(builder().channelFilter(channelEquals).or().build());
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
