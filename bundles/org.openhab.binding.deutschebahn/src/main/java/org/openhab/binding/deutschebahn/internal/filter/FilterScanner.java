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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Scanner for filter expression.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public final class FilterScanner {

    private static final Set<Character> OP_CHARS = new HashSet<>(Arrays.asList('&', '|', '!', '(', ')'));
    private static final Pattern CHANNEL_NAME = Pattern.compile("(trip|arrival|departure)#(\\S+)");

    /**
     * State of the scanner.
     */
    private interface State {

        /**
         * Handles the next read character.
         * 
         * @return Returns the next scanner state.
         */
        public abstract State handle(int position, char currentChar) throws FilterScannerException;

        /**
         * Called when no more input is available.
         */
        public abstract void finish(int position) throws FilterScannerException;
    }

    /**
     * Initial state of the scanner.
     */
    private final class InitialState implements State {

        @Override
        public State handle(int position, char currentChar) throws FilterScannerException {
            // Skip white spaces
            if (Character.isWhitespace(currentChar)) {
                return this;
            }

            switch (currentChar) {
                // Handle all operator tokens
                case '&':
                    result.add(new AndOperator(position));
                    return this;
                case '|':
                    result.add(new OrOperator(position));
                    return this;
                case '(':
                    result.add(new BracketOpenToken(position));
                    return this;
                case ')':
                    result.add(new BracketCloseToken(position));
                    return this;
                default:
                    final ChannelNameState channelNameState = new ChannelNameState();
                    return channelNameState.handle(position, currentChar);
            }
        }

        @Override
        public void finish(int position) {
        }
    }

    /**
     * State scanning a channel name until the equals-sign.
     */
    private final class ChannelNameState implements State {

        private final StringBuilder channelName = new StringBuilder();
        private int startPosition = -1;

        @Override
        public State handle(int position, final char currentChar) throws FilterScannerException {
            // Skip white spaces at front
            if (Character.isWhitespace(currentChar) && channelName.toString().isEmpty()) {
                return this;
            }

            if (Character.isWhitespace(currentChar)) {
                throw new FilterScannerException(position, "Channel name must not contain whitespace.");
            }

            if (currentChar == '=') {
                final String channelNameValue = this.channelName.toString();
                if (channelNameValue.isEmpty()) {
                    throw new FilterScannerException(position, "Channel name must not be empty.");
                }

                final Matcher matcher = CHANNEL_NAME.matcher(channelNameValue);
                if (!matcher.matches()) {
                    throw new FilterScannerException(position, "Invalid channel name: " + channelNameValue);
                }

                return new ExpectQuotesState(startPosition, matcher.group(1), matcher.group(2));
            }

            if (OP_CHARS.contains(currentChar)) {
                throw new FilterScannerException(position, "Channel name must not contain operation char.");
            }

            this.channelName.append(currentChar);
            if (startPosition == -1) {
                startPosition = position;
            }
            return this;
        }

        @Override
        public void finish(int position) throws FilterScannerException {
            throw new FilterScannerException(position, "Filter value is missing.");
        }
    }

    /**
     * State after channel name, wiating for quotes.
     */
    private final class ExpectQuotesState implements State {

        private final int startPosition;
        private final String channelName;
        private final String channelGroup;

        /**
         * Creates a new {@link ExpectQuotesState}.
         */
        public ExpectQuotesState(int startPosition, final String channelGroup, String channelName) {
            this.startPosition = startPosition;
            this.channelGroup = channelGroup;
            this.channelName = channelName;
        }

        @Override
        public State handle(int position, char currentChar) throws FilterScannerException {
            if (currentChar != '"') {
                throw new FilterScannerException(position, "Filter value must start with quotes");
            }
            return new FilterValueState(startPosition, channelGroup, channelName);
        }

        @Override
        public void finish(int position) throws FilterScannerException {
            throw new FilterScannerException(position, "Filter value is missing.");
        }
    }

    /**
     * State scanning the filter value until next quotes.
     */
    private final class FilterValueState implements State {

        private final int startPosition;
        private final String channelGroup;
        private final String channelName;
        private final StringBuilder filterValue;

        /**
         * Creates a new {@link FilterValueState}.
         */
        public FilterValueState(int startPosition, String channelGroup, String channelName) {
            this.startPosition = startPosition;
            this.channelGroup = channelGroup;
            this.channelName = channelName;
            this.filterValue = new StringBuilder();
        }

        @Override
        public State handle(int position, char currentChar) throws FilterScannerException {
            if (currentChar == '"') {
                finish(position);
                return new InitialState();
            }
            filterValue.append(currentChar);
            return this;
        }

        @Override
        public void finish(int position) throws FilterScannerException {
            String filterPattern = this.filterValue.toString();
            try {
                result.add(new ChannelNameEquals(startPosition, this.channelGroup, this.channelName,
                        Pattern.compile(filterPattern)));
            } catch (PatternSyntaxException e) {
                throw new FilterScannerException(position, "Filter pattern is invalid: " + filterPattern, e);
            }
        }
    }

    private List<FilterToken> result;

    /**
     * Creates a new {@link FilterScanner}.
     */
    public FilterScanner() {
        this.result = new ArrayList<>();
    }

    /**
     * Scans the given filter expression and returns the result sequence of {@link FilterToken}.
     */
    public List<FilterToken> processInput(String value) throws FilterScannerException {
        State state = new InitialState();
        for (int pos = 0; pos < value.length(); pos++) {
            char currentChar = value.charAt(pos);
            state = state.handle(pos + 1, currentChar);
        }

        state.finish(value.length());

        return this.result;
    }
}
