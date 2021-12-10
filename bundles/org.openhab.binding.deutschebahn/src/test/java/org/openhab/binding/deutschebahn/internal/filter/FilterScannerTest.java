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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FilterScanner}
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public class FilterScannerTest {

    private static void assertAttributeEquals(FilterToken token, String expectedChannelGroup,
            String expectedChannelName, String expectedFilter) {
        assertThat(token, is(instanceOf(ChannelNameEquals.class)));
        ChannelNameEquals actual = (ChannelNameEquals) token;
        assertThat(actual.getChannelGroup(), is(expectedChannelGroup));
        assertThat(actual.getChannelName(), is(expectedChannelName));
        assertThat(actual.getFilterValue().toString(), is(expectedFilter));
    }

    private static void assertOperator(FilterToken token, OperatorToken expected) {
        assertThat(token, is(expected));
    }

    private static List<FilterToken> processInput(String input, int expectedCount) throws FilterScannerException {
        final List<FilterToken> tokens = new FilterScanner().processInput(input);
        assertThat(tokens, hasSize(expectedCount));
        return tokens;
    }

    @Test
    public void testSimpleAttributEquals() throws FilterScannerException {
        String input = "trip#number=\"20\"";
        List<FilterToken> tokens = processInput(input, 1);
        assertAttributeEquals(tokens.get(0), "trip", "number", "20");
    }

    @Test
    public void testAttributeEqualsWithWhitespace() throws FilterScannerException {
        String input = "departure#planned-path=\"Hannover Hbf\"";
        List<FilterToken> tokens = processInput(input, 1);
        assertAttributeEquals(tokens.get(0), "departure", "planned-path", "Hannover Hbf");
    }

    @Test
    public void testInvalidAttributEquals() {
        try {
            new FilterScanner().processInput("trip#number=20");
            fail();
        } catch (FilterScannerException e) {
        }

        try {
            new FilterScanner().processInput("trip#number");
            fail();
        } catch (FilterScannerException e) {
        }

        try {
            new FilterScanner().processInput("trip#number=");
            fail();
        } catch (FilterScannerException e) {
        }

        try {
            new FilterScanner().processInput("=abc");
            fail();
        } catch (FilterScannerException e) {
        }

        try {
            new FilterScanner().processInput("train#number=\"abc\"");
            fail();
        } catch (FilterScannerException e) {
        }
    }

    @Test
    public void testComplexExample() throws FilterScannerException {
        String input = "trip#category=\"RE\" & (departure#line=\"17\" | departure#line=\"57\") & departure#planned-path=\"Cologne\"";
        List<FilterToken> tokens = processInput(input, 9);
        assertAttributeEquals(tokens.get(0), "trip", "category", "RE");
        assertOperator(tokens.get(1), OperatorToken.AND);
        assertOperator(tokens.get(2), OperatorToken.BRACKET_OPEN);
        assertAttributeEquals(tokens.get(3), "departure", "line", "17");
        assertOperator(tokens.get(4), OperatorToken.OR);
        assertAttributeEquals(tokens.get(5), "departure", "line", "57");
        assertOperator(tokens.get(6), OperatorToken.BRACKET_CLOSE);
        assertOperator(tokens.get(7), OperatorToken.AND);
        assertAttributeEquals(tokens.get(8), "departure", "planned-path", "Cologne");
    }
}
