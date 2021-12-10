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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parses an {@link FilterToken}-Sequence into a {@link TimetableStopPredicate}.
 * 
 * @author Sönke Küper - Initial contribution.
 */
@NonNullByDefault
public final class FilterParser {

    /**
     * Parser's state.
     */
    private abstract static class State implements FilterTokenVisitor<State> {

        @Nullable
        private State previousState;

        public State(@Nullable State previousState) {
            this.previousState = previousState;
        }

        private final State handle(FilterToken token) throws FilterParserException {
            return token.accept(this);
        }

        protected abstract State handleChildResult(TimetableStopPredicate predicate) throws FilterParserException;

        @Override
        public final State handle(ChannelNameEquals channelEquals) throws FilterParserException {
            final TimetableStopByStringEventAttributeFilter predicate = channelEquals.mapToPredicate();
            return this.handleChildResult(predicate);
        }

        protected final State publishResultToPrevious(TimetableStopPredicate predicate) throws FilterParserException {
            return this.getPreviousState().handleChildResult(predicate);
        }

        protected State getPreviousState() throws FilterParserException {
            final State previousStateValue = this.previousState;
            if (previousStateValue == null) {
                throw new FilterParserException("Invalid filter");
            } else {
                return previousStateValue;
            }
        }

        /**
         * Returns the result.
         */
        public abstract TimetableStopPredicate getResult() throws FilterParserException;
    }

    /**
     * Initial state for the parser.
     */
    private static final class InitialState extends State {

        @Nullable
        private TimetableStopPredicate result;

        public InitialState() {
            super(null);
        }

        @Override
        public State handle(OperatorToken operator) throws FilterParserException {
            final TimetableStopPredicate currentResult = this.result;
            this.result = null;
            switch (operator) {
                case AND:
                    if (currentResult == null) {
                        throw new FilterParserException("Invalid filter.");
                    }
                    return new AndState(this, currentResult);
                case OR:
                    if (currentResult == null) {
                        throw new FilterParserException("Invalid filter.");
                    }
                    return new OrState(this, currentResult);
                case BRACKET_OPEN:
                    return new SubQueryState(this);
                case BRACKET_CLOSE:
                    throw new FilterParserException("Invalid filter.");
                default:
                    throw new FilterParserException("Unknown operator: " + operator);
            }
        }

        @Override
        protected State handleChildResult(TimetableStopPredicate predicate) throws FilterParserException {
            if (this.result == null) {
                this.result = predicate;
                return this;
            } else {
                throw new FilterParserException("Invalid filter.");
            }
        }

        @Override
        public TimetableStopPredicate getResult() throws FilterParserException {
            final TimetableStopPredicate currentResult = this.result;
            if (currentResult != null) {
                return currentResult;
            }
            throw new FilterParserException("Invalid filter");
        }
    }

    /**
     * State while parsing an conjunction.
     */
    private static final class AndState extends State {

        private final TimetableStopPredicate first;

        public AndState(State previousState, final TimetableStopPredicate first) {
            super(previousState);
            this.first = first;
        }

        @Override
        public State handle(OperatorToken operator) throws FilterParserException {
            switch (operator) {
                case AND:
                case OR:
                case BRACKET_CLOSE:
                    throw new FilterParserException("Invalid second argument for '&' operator.");
                case BRACKET_OPEN:
                    return new SubQueryState(this);
                default:
                    throw new FilterParserException("Unknown operator: " + operator);
            }
        }

        @Override
        protected State handleChildResult(TimetableStopPredicate predicate) throws FilterParserException {
            return this.publishResultToPrevious(new AndPredicate(first, predicate));
        }

        @Override
        public TimetableStopPredicate getResult() throws FilterParserException {
            throw new FilterParserException("Invalid filter");
        }
    }

    /**
     * State while parsing an disjunction.
     */
    private static final class OrState extends State {

        private final TimetableStopPredicate first;

        public OrState(State previousState, final TimetableStopPredicate first) {
            super(previousState);
            this.first = first;
        }

        @Override
        public State handle(OperatorToken operator) throws FilterParserException {
            switch (operator) {
                case AND:
                case OR:
                case BRACKET_CLOSE:
                    throw new FilterParserException("Invalid second argument for '|' operator.");
                case BRACKET_OPEN:
                    return new SubQueryState(this);
                default:
                    throw new FilterParserException("Unknown operator: " + operator);
            }
        }

        @Override
        protected State handleChildResult(TimetableStopPredicate second) throws FilterParserException {
            return this.publishResultToPrevious(new OrPredicate(first, second));
        }

        @Override
        public TimetableStopPredicate getResult() throws FilterParserException {
            throw new FilterParserException("Invalid filter");
        }
    }

    /**
     * State while parsing an Subquery.
     */
    private static final class SubQueryState extends State {

        @Nullable
        private TimetableStopPredicate currentResult;

        public SubQueryState(State previousState) {
            super(previousState);
        }

        @Override
        public State handle(OperatorToken operator) throws FilterParserException {
            TimetableStopPredicate result = this.currentResult;

            switch (operator) {
                case AND:
                    if (result == null) {
                        throw new FilterParserException("Operator '&' must not be first element in subquery.");
                    }
                    return new AndState(this, result);
                case OR:
                    if (result == null) {
                        throw new FilterParserException("Operator '|' must not be first element in subquery.");
                    }
                    return new OrState(this, result);
                case BRACKET_CLOSE:
                    if (result == null) {
                        throw new FilterParserException("Subquery must not be empty.");
                    }
                    return publishResultToPrevious(result);
                case BRACKET_OPEN:
                    return new SubQueryState(this);
                default:
                    throw new FilterParserException("Unknown operator: " + operator);
            }
        }

        @Override
        protected State handleChildResult(TimetableStopPredicate predicate) {
            this.currentResult = predicate;
            return this;
        }

        @Override
        public TimetableStopPredicate getResult() throws FilterParserException {
            throw new FilterParserException("Invalid filter");
        }
    }

    private FilterParser() {
    }

    /**
     * Parses the given {@link FilterToken} into an {@link TimetableStopPredicate}.
     */
    public static TimetableStopPredicate parse(final List<FilterToken> tokens) throws FilterParserException {
        State state = new InitialState();
        for (FilterToken token : tokens) {
            state = state.handle(token);
        }
        return state.getResult();
    }
}
