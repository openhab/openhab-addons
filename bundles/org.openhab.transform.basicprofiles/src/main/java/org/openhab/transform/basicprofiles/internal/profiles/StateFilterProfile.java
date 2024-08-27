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

import static java.util.function.Predicate.not;
import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.STATE_FILTER_UID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;
import org.openhab.transform.basicprofiles.internal.config.StateFilterProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accepts updates to state as long as conditions are met. Support for sending fixed state if conditions are *not*
 * met.
 *
 * @author Arne Seime - Initial contribution
 * @author Jimmy Tanagra - Expanded the comparison types
 */
@NonNullByDefault
public class StateFilterProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(StateFilterProfile.class);

    private final ProfileCallback callback;

    private final List<StateCondition> conditions;

    private final @Nullable State configMismatchState;

    public StateFilterProfile(ProfileCallback callback, ProfileContext context, ItemRegistry itemRegistry) {
        this.callback = callback;

        StateFilterProfileConfig config = context.getConfiguration().as(StateFilterProfileConfig.class);
        if (config != null) {
            conditions = parseConditions(config.conditions, config.separator, itemRegistry);
            if (conditions.isEmpty()) {
                logger.warn("No valid conditions defined for StateFilterProfile. Link: {}. Conditions: {}",
                        callback.getItemChannelLink(), config.conditions);
            }
            configMismatchState = parseState(config.mismatchState, context.getAcceptedDataTypes());
        } else {
            conditions = List.of();
            configMismatchState = null;
        }
    }

    private List<StateCondition> parseConditions(List<String> conditions, String separator, ItemRegistry itemRegistry) {
        List<StateCondition> parsedConditions = new ArrayList<>();

        String operatorPattern = Stream.of(StateCondition.ComparisonType.values())
                .flatMap(type -> Stream.of(" " + type.name() + " ", type.getSymbol()))
                .sorted((a, b) -> Integer.compare(b.length(), a.length())) // We want to match the longest operator
                                                                           // first, e.g. `<=` before `<`
                .collect(Collectors.joining("|"));

        Pattern expressionPattern = Pattern.compile("(.*)(" + operatorPattern + ")(.*)", Pattern.CASE_INSENSITIVE);

        conditions.stream() //
                .flatMap(c -> Stream.of(c.split(separator))) //
                .map(String::trim) //
                .filter(not(String::isBlank)) //
                .forEach(expression -> {
                    Matcher matcher = expressionPattern.matcher(expression);
                    if (!matcher.matches()) {
                        logger.warn(
                                "Malformed condition expression: '{}'. Expected format ITEM_NAME OPERATOR ITEM_OR_STATE, where OPERATOR is one of: {}",
                                expression, StateCondition.ComparisonType.valuesAndSymbols());
                        return;
                    }

                    String itemName = matcher.group(1).trim();
                    String operator = matcher.group(2).trim();
                    String value = matcher.group(3).trim();
                    try {
                        StateCondition.ComparisonType comparisonType = Objects.requireNonNullElseGet(
                                StateCondition.ComparisonType.fromSymbol(operator),
                                () -> StateCondition.ComparisonType.valueOf(operator.toUpperCase(Locale.ROOT)));
                        parsedConditions.add(new StateCondition(itemName, comparisonType, value, itemRegistry));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid comparison operator: '{}'. Expected one of: {}", operator,
                                StateCondition.ComparisonType.valuesAndSymbols());
                    }
                });

        return parsedConditions;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return STATE_FILTER_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do nothing
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand(command);
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand(command);
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        State resultState = checkCondition(state);
        if (resultState != null) {
            logger.debug("Received state update from handler: {}, forwarded as {}", state, resultState);
            callback.sendUpdate(resultState);
        } else {
            logger.debug("Received state update from handler: {}, not forwarded to item", state);
        }
    }

    @Nullable
    private State checkCondition(State state) {
        if (conditions.isEmpty()) {
            logger.warn(
                    "No valid configuration defined for StateFilterProfile (check for log messages when instantiating profile) - skipping state update");
            return null;
        }

        String linkedItemName = callback.getItemChannelLink().getItemName();

        if (conditions.stream().allMatch(c -> c.check(linkedItemName, state))) {
            return state;
        } else {
            return configMismatchState;
        }
    }

    @Nullable
    static State parseState(@Nullable String stateString, List<Class<? extends State>> acceptedDataTypes) {
        // Quoted strings are parsed as StringType
        if (stateString == null) {
            return null;
        } else if (stateString.startsWith("'") && stateString.endsWith("'")) {
            return new StringType(stateString.substring(1, stateString.length() - 1));
        } else {
            return TypeParser.parseState(acceptedDataTypes, stateString);
        }
    }

    class StateCondition {
        private String itemName;
        private ComparisonType comparisonType;
        private String value;
        private @Nullable State parsedValue;

        private ItemRegistry itemRegistry;

        public StateCondition(String itemName, ComparisonType comparisonType, String value, ItemRegistry itemRegistry) {
            this.itemRegistry = itemRegistry;
            this.itemName = itemName;
            this.comparisonType = comparisonType;
            this.value = value;
            // Convert quoted strings to StringType, and UnDefTypes to UnDefType
            // UnDefType gets special treatment because we don't want `UNDEF` to be parsed as a string
            // Anything else, defer parsing until we're checking the condition
            // so we can try based on the item's accepted data types
            this.parsedValue = parseState(value, List.of(UnDefType.class));
        }

        /**
         * Check if the condition is met.
         * 
         * If the itemName is not empty, the condition is checked against the item's state.
         * Otherwise, the condition is checked against the input state.
         *
         * @param input the state to check against
         * @return true if the condition is met, false otherwise
         */
        public boolean check(String linkedItemName, State input) {
            try {
                State state;
                Item item = null;

                logger.debug("Evaluating {} with input: {} ({})", this, input, input.getClass().getSimpleName());
                if (itemName.isEmpty()) {
                    item = itemRegistry.getItem(linkedItemName);
                    state = input;
                } else {
                    item = itemRegistry.getItem(itemName);
                    state = item.getState();
                }

                // Using Object because we could be comparing State or String objects
                Object lhs;
                Object rhs;

                // Java Enums (e.g. OnOffType) are Comparable, but we want to treat them as not Comparable
                if (state instanceof Comparable && !(state instanceof Enum)) {
                    lhs = state;
                } else {
                    // Only allow EQ and NEQ for non-comparable states
                    if (!(comparisonType == ComparisonType.EQ || comparisonType == ComparisonType.NEQ)) {
                        logger.debug("Condition state: '{}' ({}) only supports '==' and '!==' comparisons", state,
                                state.getClass().getSimpleName());
                        return false;
                    }
                    lhs = state instanceof Enum ? state : state.toString();
                }

                if (parsedValue == null) {
                    // don't parse bare strings as StringType, because they are identifiers,
                    // e.g. referring to other items
                    List<Class<? extends State>> acceptedValueTypes = item.getAcceptedDataTypes().stream()
                            .filter(not(StringType.class::isAssignableFrom)).toList();
                    parsedValue = TypeParser.parseState(acceptedValueTypes, value);
                    // Don't convert QuantityType to other types
                    if (parsedValue != null && !(parsedValue instanceof QuantityType)) {
                        // Try to convert it to the same type as the state
                        // This allows comparing compatible types, e.g. PercentType vs OnOffType
                        parsedValue = parsedValue.as(state.getClass());
                    }

                    // If the values can't be converted to a type, check to see if it's an Item name
                    if (parsedValue == null) {
                        try {
                            Item valueItem = itemRegistry.getItem(value);
                            if (valueItem != null) { // ItemRegistry.getItem can return null in tests
                                parsedValue = valueItem.getState();
                                // Don't convert QuantityType to other types
                                if (!(parsedValue instanceof QuantityType)) {
                                    parsedValue = parsedValue.as(state.getClass());
                                }
                                logger.debug("Condition value: '{}' is an item state: '{}' ({})", value, parsedValue,
                                        parsedValue == null ? "null" : parsedValue.getClass().getSimpleName());
                            }
                        } catch (ItemNotFoundException ignore) {
                        }
                    }

                    if (parsedValue == null) {
                        if (comparisonType == ComparisonType.NEQ) {
                            // They're not even type compatible, so return true for NEQ comparison
                            return true;
                        } else {
                            logger.debug("Condition value: '{}' is not compatible with state '{}' ({})", value, state,
                                    state.getClass().getSimpleName());
                            return false;
                        }
                    }
                }

                rhs = Objects.requireNonNull(parsedValue instanceof StringType ? parsedValue.toString() : parsedValue);

                if (logger.isDebugEnabled()) {
                    if (itemName.isEmpty()) {
                        logger.debug("Performing a comparison between input '{}' ({}) and value '{}' ({})", lhs,
                                lhs.getClass().getSimpleName(), rhs, rhs.getClass().getSimpleName());
                    } else {
                        logger.debug("Performing a comparison between item '{}' state '{}' ({}) and value '{}' ({})",
                                itemName, lhs, lhs.getClass().getSimpleName(), rhs, rhs.getClass().getSimpleName());
                    }
                }

                return switch (comparisonType) {
                    case EQ -> lhs.equals(rhs);
                    case NEQ -> !lhs.equals(rhs);
                    case GT -> ((Comparable) lhs).compareTo(rhs) > 0;
                    case GTE -> ((Comparable) lhs).compareTo(rhs) >= 0;
                    case LT -> ((Comparable) lhs).compareTo(rhs) < 0;
                    case LTE -> ((Comparable) lhs).compareTo(rhs) <= 0;
                };
            } catch (ItemNotFoundException | IllegalArgumentException | ClassCastException e) {
                logger.warn("Error evaluating condition: {}: {}", this, e.getMessage());
            }
            return false;
        }

        enum ComparisonType {
            EQ("=="),
            NEQ("!="),
            GT(">"),
            GTE(">="),
            LT("<"),
            LTE("<=");

            private final String symbol;

            ComparisonType(String symbol) {
                this.symbol = symbol;
            }

            String getSymbol() {
                return symbol;
            }

            static @Nullable ComparisonType fromSymbol(String symbol) {
                for (ComparisonType type : values()) {
                    if (type.symbol.equals(symbol)) {
                        return type;
                    }
                }
                return null;
            }

            static List<String> valuesAndSymbols() {
                return Stream.of(values()).flatMap(entry -> Stream.of(entry.name(), entry.getSymbol())).toList();
            }
        }

        @Override
        public String toString() {
            Object state = null;

            try {
                state = itemRegistry.getItem(itemName).getState();
            } catch (ItemNotFoundException ignored) {
            }

            String stateClass = state == null ? "null" : state.getClass().getSimpleName();
            return "Condition(itemName='" + itemName + "', state='" + state + "' (" + stateClass + "), comparisonType="
                    + comparisonType + ", value='" + value + "')";
        }
    }
}
