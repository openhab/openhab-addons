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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
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
import org.openhab.core.util.Statistics;
import org.openhab.transform.basicprofiles.internal.config.StateFilterProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accepts updates to state as long as conditions are met. Support for sending fixed state if conditions are *not*
 * met.
 *
 * @author Arne Seime - Initial contribution
 * @author Jimmy Tanagra - Expanded the comparison types
 * @author Jimmy Tanagra - Added support for functions
 */
@NonNullByDefault
public class StateFilterProfile implements StateProfile {

    private static final String OPERATOR_NAME_PATTERN = Stream.of(StateCondition.ComparisonType.values())
            .map(StateCondition.ComparisonType::name)
            // We want to match the longest operator first, e.g. `GTE` before `GT`
            .sorted(Comparator.comparingInt(String::length).reversed())
            // Require a leading space only when it is preceded by a non-space character, e.g. `Item1 GTE 0`
            // so we can have conditions against input data without needing a leading space, e.g. `GTE 0`
            .collect(Collectors.joining("|", "(?:(?<=\\S)\\s+|^\\s*)(?:", ")\\s"));

    private static final String OPERATOR_SYMBOL_PATTERN = Stream.of(StateCondition.ComparisonType.values())
            .map(StateCondition.ComparisonType::symbol)
            // We want to match the longest operator first, e.g. `<=` before `<`
            .sorted(Comparator.comparingInt(String::length).reversed()) //
            .collect(Collectors.joining("|", "(?:", ")"));

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            // - Without the non-greedy operator in the first capture group,
            // it will match `Item<` when encountering `Item<>X` condition
            // - Symbols may be more prevalently used, so check them first
            "(.*?)(" + OPERATOR_SYMBOL_PATTERN + "|" + OPERATOR_NAME_PATTERN + ")(.*)", Pattern.CASE_INSENSITIVE);

    // Function pattern to match `$NAME` or `$NAME(5)`.
    // The number represents an optional window size that applies to the function.
    private final static Pattern FUNCTION_PATTERN = Pattern.compile("\\$(\\w+)(?:\\s*\\(\\s*(\\d+)\\s*\\))?\\s*");

    private final static int DEFAULT_WINDOW_SIZE = 5;

    private final Logger logger = LoggerFactory.getLogger(StateFilterProfile.class);

    private final ProfileCallback callback;

    private final ItemRegistry itemRegistry;

    private final List<StateCondition> conditions;

    private final @Nullable State configMismatchState;

    private @Nullable Item linkedItem = null;

    private State newState = UnDefType.UNDEF;
    private State acceptedState = UnDefType.UNDEF;
    private LinkedList<State> previousStates = new LinkedList<>();

    private final int windowSize;

    public StateFilterProfile(ProfileCallback callback, ProfileContext context, ItemRegistry itemRegistry) {
        this.callback = callback;
        this.itemRegistry = itemRegistry;

        StateFilterProfileConfig config = context.getConfiguration().as(StateFilterProfileConfig.class);
        if (config != null) {
            conditions = parseConditions(config.conditions, config.separator);
            int maxWindowSize = 0;

            if (conditions.isEmpty()) {
                logger.warn("No valid conditions defined for StateFilterProfile. Link: {}. Conditions: {}",
                        callback.getItemChannelLink(), config.conditions);
            } else {
                for (StateCondition condition : conditions) {
                    if (condition.lhsState instanceof FunctionType function) {
                        int windowSize = function.getWindowSize();
                        if (windowSize > maxWindowSize) {
                            maxWindowSize = windowSize;
                        }
                    }
                    if (condition.rhsState instanceof FunctionType function) {
                        int windowSize = function.getWindowSize();
                        if (windowSize > maxWindowSize) {
                            maxWindowSize = windowSize;
                        }
                    }
                }
            }

            windowSize = maxWindowSize;

            configMismatchState = parseState(config.mismatchState, context.getAcceptedDataTypes());
        } else {
            conditions = List.of();
            configMismatchState = null;
            windowSize = 0;
        }
    }

    private List<StateCondition> parseConditions(List<String> conditions, String separator) {
        List<StateCondition> parsedConditions = new ArrayList<>();

        conditions.stream() //
                .flatMap(c -> Stream.of(c.split(separator))) //
                .map(String::trim) //
                .filter(not(String::isBlank)) //
                .forEach(expression -> {
                    Matcher matcher = EXPRESSION_PATTERN.matcher(expression);
                    if (!matcher.matches()) {
                        logger.warn(
                                "Malformed condition expression: '{}' in link '{}'. Expected format ITEM_NAME OPERATOR ITEM_OR_STATE, where OPERATOR is one of: {}",
                                expression, callback.getItemChannelLink(),
                                StateCondition.ComparisonType.namesAndSymbols());
                        return;

                    }

                    String lhs = matcher.group(1).trim();
                    String operator = matcher.group(2).trim();
                    String rhs = matcher.group(3).trim();
                    try {
                        StateCondition.ComparisonType comparisonType = StateCondition.ComparisonType
                                .fromSymbol(operator).orElseGet(
                                        () -> StateCondition.ComparisonType.valueOf(operator.toUpperCase(Locale.ROOT)));
                        parsedConditions.add(new StateCondition(lhs, comparisonType, rhs));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid comparison operator: '{}' in link '{}'. Expected one of: {}", operator,
                                callback.getItemChannelLink(), StateCondition.ComparisonType.namesAndSymbols());
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
        newState = state;
        State resultState = checkCondition(state);
        if (resultState != null) {
            logger.debug("Received state update from handler: {}, forwarded as {}", state, resultState);
            callback.sendUpdate(resultState);
        } else {
            logger.debug("Received state update from handler: {}, not forwarded to item", state);
        }
        if (windowSize > 0 && (state instanceof DecimalType || state instanceof QuantityType)) {
            previousStates.add(state);
            if (previousStates.size() > windowSize) {
                previousStates.removeFirst();
            }
        }
    }

    @Nullable
    private State checkCondition(State state) {
        if (conditions.isEmpty()) {
            logger.warn(
                    "No valid configuration defined for StateFilterProfile (check for log messages when instantiating profile) - skipping state update. Link: '{}'",
                    callback.getItemChannelLink());
            return null;
        }

        if (conditions.stream().allMatch(c -> c.check(state))) {
            return state;
        } else {
            return configMismatchState;
        }
    }

    private @Nullable Item getLinkedItem() {
        if (linkedItem == null) {
            linkedItem = getItemOrNull(callback.getItemChannelLink().getItemName());
        }
        return linkedItem;
    }

    @Nullable
    State parseState(@Nullable String stateString, List<Class<? extends State>> acceptedDataTypes) {
        // Quoted strings are parsed as StringType
        if (stateString == null || stateString.isEmpty()) {
            return null;
        } else if (stateString.startsWith("'") && stateString.endsWith("'")) {
            return new StringType(stateString.substring(1, stateString.length() - 1));
        } else if (parseFunction(stateString) instanceof FunctionType function) {
            return function;
        } else if (TypeParser.parseState(acceptedDataTypes, stateString) instanceof State state) {
            return state;
        }
        return null;
    }

    @Nullable
    FunctionType parseFunction(String functionDefinition) {
        if (!functionDefinition.startsWith("$")) {
            return null;
        }
        logger.debug("Parsing function: '{}'", functionDefinition);
        Matcher matcher = FUNCTION_PATTERN.matcher(functionDefinition);
        if (!matcher.matches()) {
            logger.warn("Invalid function definition: '{}'", functionDefinition);
            return null;
        }
        String functionName = matcher.group(1).toUpperCase(Locale.ROOT);
        try {
            FunctionType.Function type = FunctionType.Function.valueOf(functionName);

            Optional<Integer> windowSize = Optional.empty();
            windowSize = Optional.ofNullable(matcher.group(2)).map(Integer::parseInt);
            return new FunctionType(type, windowSize);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid function name: '{}'. Expected one of: {}", functionName,
                    Stream.of(FunctionType.Function.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return null;
        }
    }

    private @Nullable Item getItemOrNull(String value) {
        try {
            return itemRegistry.getItem(value);
        } catch (ItemNotFoundException e) {
            return null;
        }
    }

    class StateCondition {
        private ComparisonType comparisonType;
        private String lhsString;
        private String rhsString;
        private @Nullable State lhsState;
        private @Nullable State rhsState;

        public StateCondition(String lhs, ComparisonType comparisonType, String rhs) {
            this.comparisonType = comparisonType;

            if (lhs.isEmpty() && rhs.endsWith("%")) {
                // Allow comparing percentages without a left hand side,
                // e.g. `> 50%` -> translate this to `$DELTA_PERCENT > 50`
                lhsString = "$DELTA_PERCENT";
                rhsString = rhs.substring(0, rhs.length() - 1).trim();
            } else {
                lhsString = lhs;
                rhsString = rhs;
            }
            // Convert quoted strings to StringType, and UnDefTypes to UnDefType
            // UnDefType gets special treatment because we don't want `UNDEF` to be parsed as a string
            // Anything else, defer parsing until we're checking the condition
            // so we can try based on the item's accepted data types
            this.lhsState = parseState(lhsString, List.of(UnDefType.class));
            this.rhsState = parseState(rhsString, List.of(UnDefType.class));
        }

        /**
         * Check if the condition is met.
         * 
         * If the lhs is empty, the condition is checked against the input state.
         *
         * @param input the state to check against
         * @return true if the condition is met, false otherwise
         */
        public boolean check(State input) {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating {} with input: {} ({}). Link: '{}'", this, input,
                        input.getClass().getSimpleName(), callback.getItemChannelLink());
            }

            try {
                // Don't overwrite the object variables. These need to be re-evaluated for each check
                State lhsState = this.lhsState;
                State rhsState = this.rhsState;
                Item lhsItem = null;
                Item rhsItem = null;

                if (rhsState == null) {
                    rhsItem = getItemOrNull(rhsString);
                } else if (rhsState instanceof FunctionType) {
                    rhsItem = getLinkedItem();
                }

                if (lhsString.isEmpty()) {
                    lhsItem = getLinkedItem();
                    lhsState = input;
                } else if (lhsState == null) {
                    lhsItem = getItemOrNull(lhsString);
                    lhsState = itemStateOrParseState(lhsItem, lhsString, rhsItem);

                    if (lhsState == null) {
                        logger.debug(
                                "The left hand side of the condition '{}' is not compatible with the right hand side '{}'",
                                lhsString, rhsString);
                        return false;
                    }
                } else if (lhsState instanceof FunctionType lhsFunction) {
                    lhsItem = getLinkedItem();
                    lhsState = lhsFunction.calculate();
                    if (lhsState == null) {
                        logger.debug("Couldn't calculate the left hand side function '{}'", lhsString);
                        return false;
                    }
                }

                if (rhsState == null) {
                    rhsState = itemStateOrParseState(rhsItem, rhsString, lhsItem);
                }

                // Don't convert QuantityType to other types, so that 1500 != 1500 W
                if (rhsState != null && !(rhsState instanceof QuantityType)) {
                    // Try to convert it to the same type as the lhs
                    // This allows comparing compatible types, e.g. PercentType vs OnOffType
                    rhsState = rhsState.as(lhsState.getClass());
                }

                if (rhsState == null) {
                    if (comparisonType == ComparisonType.NEQ || comparisonType == ComparisonType.NEQ_ALT) {
                        // They're not even type compatible, so return true for NEQ comparison
                        return true;
                    } else {
                        logger.debug("RHS: '{}' is not compatible with LHS '{}' ({})", rhsString, lhsState,
                                lhsState.getClass().getSimpleName());
                        return false;
                    }
                }

                // Using Object because we could be comparing State or String objects
                Object lhs;
                Object rhs;

                // Java Enums (e.g. OnOffType) are inherently Comparable,
                // but we don't want to allow comparisons like "ON > OFF"
                if (lhsState instanceof Comparable && !(lhsState instanceof Enum)) {
                    lhs = lhsState;
                } else {
                    // Only allow EQ and NEQ for non-comparable states
                    if (!(comparisonType == ComparisonType.EQ || comparisonType == ComparisonType.NEQ
                            || comparisonType == ComparisonType.NEQ_ALT)) {
                        logger.debug("LHS: '{}' ({}) only supports '==' and '!==' comparisons", lhsState,
                                lhsState.getClass().getSimpleName());
                        return false;
                    }
                    lhs = lhsState instanceof Enum ? lhsState : lhsState.toString();
                }

                rhs = Objects.requireNonNull(rhsState instanceof StringType ? rhsState.toString() : rhsState);

                if (logger.isDebugEnabled()) {
                    if (lhsString.isEmpty()) {
                        logger.debug("Performing a comparison between input '{}' ({}) and value '{}' ({})", lhs,
                                lhs.getClass().getSimpleName(), rhs, rhs.getClass().getSimpleName());
                    } else {
                        logger.debug("Performing a comparison between '{}' state '{}' ({}) and value '{}' ({})",
                                lhsString, lhs, lhs.getClass().getSimpleName(), rhs, rhs.getClass().getSimpleName());
                    }
                }

                boolean result = switch (comparisonType) {
                    case EQ -> lhs.equals(rhs);
                    case NEQ, NEQ_ALT -> !lhs.equals(rhs);
                    case GT -> ((Comparable) lhs).compareTo(rhs) > 0;
                    case GTE -> ((Comparable) lhs).compareTo(rhs) >= 0;
                    case LT -> ((Comparable) lhs).compareTo(rhs) < 0;
                    case LTE -> ((Comparable) lhs).compareTo(rhs) <= 0;
                };

                if (result) {
                    acceptedState = input;
                }

                return result;
            } catch (IllegalArgumentException | ClassCastException e) {
                logger.warn("Error evaluating condition: {} in link '{}': {}", this, callback.getItemChannelLink(),
                        e.getMessage());
            }
            return false;
        }

        private @Nullable State itemStateOrParseState(@Nullable Item item, String value, @Nullable Item oppositeItem) {
            if (item != null) {
                return item.getState();
            }

            if (oppositeItem != null) {
                List<Class<? extends State>> excludeStringType = oppositeItem.getAcceptedDataTypes().stream()
                        .filter(not(StringType.class::isAssignableFrom)).toList();
                return TypeParser.parseState(excludeStringType, value);
            }

            return null;
        }

        enum ComparisonType {
            EQ("=="),
            NEQ("!="),
            NEQ_ALT("<>"),
            GT(">"),
            GTE(">="),
            LT("<"),
            LTE("<=");

            private final String symbol;

            ComparisonType(String symbol) {
                this.symbol = symbol;
            }

            String symbol() {
                return symbol;
            }

            static Optional<ComparisonType> fromSymbol(String symbol) {
                for (ComparisonType type : values()) {
                    if (type.symbol.equals(symbol)) {
                        return Optional.of(type);
                    }
                }
                return Optional.empty();
            }

            static List<String> namesAndSymbols() {
                return Stream.of(values()).flatMap(entry -> Stream.of(entry.name(), entry.symbol())).toList();
            }
        }

        @Override
        public String toString() {
            return String.format("Condition('%s' %s %s '%s' %s)", lhsString,
                    Objects.requireNonNullElse(lhsState, "").toString(), comparisonType, rhsString,
                    Objects.requireNonNullElse(rhsState, "").toString());
        }
    }

    /**
     * Represents a function to be applied to the previous states.
     */
    class FunctionType implements State {
        enum Function {
            DELTA,
            DELTA_PERCENT,
            AVERAGE,
            AVG,
            MEDIAN,
            STDDEV,
            MIN,
            MAX
        }

        private final Function type;
        private final Optional<Integer> windowSize;

        public FunctionType(Function type, Optional<Integer> windowSize) {
            this.type = type;
            this.windowSize = windowSize;
        }

        public @Nullable State calculate() {
            logger.debug("Calculating function: {}", this);
            int size = previousStates.size();
            int start = windowSize.map(w -> size - w).orElse(0);
            List<State> states = start <= 0 ? previousStates : previousStates.subList(start, size);
            return switch (type) {
                case DELTA -> calculateDelta();
                case DELTA_PERCENT -> calculateDeltaPercent();
                case AVG, AVERAGE -> calculateAverage(states);
                case MEDIAN -> calculateMedian(states);
                case STDDEV -> calculateStdDev(states);
                case MIN -> calculateMin(states);
                case MAX -> calculateMax(states);
            };
        }

        @Override
        public <T extends State> @Nullable T as(@Nullable Class<T> target) {
            if (target == DecimalType.class || target == QuantityType.class) {
                return target.cast(calculate());
            }
            return null;
        }

        public int getWindowSize() {
            if (type == Function.DELTA || type == Function.DELTA_PERCENT) {
                // We don't need to keep previous states list to calculate the delta,
                // the previous state is kept in the acceptedState variable
                return 0;
            }
            return windowSize.orElse(DEFAULT_WINDOW_SIZE);
        }

        public Function getType() {
            return type;
        }

        @Override
        public String format(String _pattern) {
            return toFullString();
        }

        @Override
        public String toFullString() {
            return "$" + type.toString();
        }

        @Override
        public String toString() {
            return toFullString();
        }

        private @Nullable State calculateAverage(List<State> states) {
            if (states.isEmpty()) {
                logger.debug("Not enough states to calculate sum");
                return null;
            }
            if (newState instanceof QuantityType newStateQuantity) {
                QuantityType zero = new QuantityType(0, newStateQuantity.getUnit());
                QuantityType sum = states.stream().map(s -> (QuantityType) s).reduce(zero, QuantityType::add);
                return sum.divide(BigDecimal.valueOf(states.size()));
            }
            BigDecimal sum = states.stream().map(s -> ((DecimalType) s).toBigDecimal()).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            return new DecimalType(sum.divide(BigDecimal.valueOf(states.size()), 2, RoundingMode.HALF_EVEN));
        }

        private @Nullable State calculateMedian(List<State> states) {
            if (states.isEmpty()) {
                logger.debug("Not enough states to calculate median");
                return null;
            }
            if (newState instanceof QuantityType newStateQuantity) {
                Unit<?> unit = newStateQuantity.getUnit();
                List<BigDecimal> bdStates = states.stream()
                        .map(s -> ((QuantityType) s).toInvertibleUnit(unit).toBigDecimal()).toList();
                return Optional.ofNullable(Statistics.median(bdStates)).map(median -> new QuantityType(median, unit))
                        .orElse(null);
            }
            List<BigDecimal> bdStates = states.stream().map(s -> ((DecimalType) s).toBigDecimal()).toList();
            return Optional.ofNullable(Statistics.median(bdStates)).map(median -> new DecimalType(median)).orElse(null);
        }

        private @Nullable State calculateStdDev(List<State> states) {
            if (states.isEmpty()) {
                logger.debug("Not enough states to calculate standard deviation");
                return null;
            }
            if (newState instanceof QuantityType newStateQuantity) {
                QuantityType average = (QuantityType) calculateAverage(states);
                if (average == null) {
                    return null;
                }
                QuantityType zero = new QuantityType(0, newStateQuantity.getUnit());
                QuantityType variance = states.stream() //
                        .map(s -> {
                            QuantityType delta = ((QuantityType) s).subtract(average);
                            return (QuantityType) delta.multiply(delta.toBigDecimal()); // don't square the unit
                        }) //
                        .reduce(zero, QuantityType::add) // This reduced into a QuantityType
                        .divide(BigDecimal.valueOf(states.size()));
                return new QuantityType(variance.toBigDecimal().sqrt(MathContext.DECIMAL32), variance.getUnit());
            }
            BigDecimal average = Optional.ofNullable((DecimalType) calculateAverage(states))
                    .map(DecimalType::toBigDecimal).orElse(null);
            if (average == null) {
                return null;
            }
            BigDecimal variance = states.stream().map(s -> {
                BigDecimal delta = ((DecimalType) s).toBigDecimal().subtract(average);
                return delta.multiply(delta);
            }).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(states.size()),
                    MathContext.DECIMAL32);
            return new DecimalType(variance.sqrt(MathContext.DECIMAL32));
        }

        private @Nullable State calculateMin(List<State> states) {
            if (states.isEmpty()) {
                logger.debug("Not enough states to calculate min");
                return null;
            }
            if (newState instanceof QuantityType newStateQuantity) {
                return states.stream().map(s -> (QuantityType) s).min(QuantityType::compareTo).orElse(null);
            }
            return states.stream().map(s -> ((DecimalType) s).toBigDecimal()).min(BigDecimal::compareTo)
                    .map(DecimalType::new).orElse(null);
        }

        private @Nullable State calculateMax(List<State> states) {
            if (states.isEmpty()) {
                logger.debug("Not enough states to calculate max");
                return null;
            }
            if (newState instanceof QuantityType newStateQuantity) {
                return states.stream().map(s -> (QuantityType) s).max(QuantityType::compareTo).orElse(null);
            }
            return states.stream().map(s -> ((DecimalType) s).toBigDecimal()).max(BigDecimal::compareTo)
                    .map(DecimalType::new).orElse(null);
        }

        private @Nullable State calculateDelta() {
            if (acceptedState == UnDefType.UNDEF) {
                logger.debug("No previous data to calculate delta");
                acceptedState = newState;
                return null;
            }

            if (newState instanceof QuantityType newStateQuantity) {
                QuantityType result = newStateQuantity.subtract((QuantityType) acceptedState);
                return result.toBigDecimal().compareTo(BigDecimal.ZERO) < 0 ? result.negate() : result;
            }
            BigDecimal result = ((DecimalType) newState).toBigDecimal()
                    .subtract(((DecimalType) acceptedState).toBigDecimal());
            return result.compareTo(BigDecimal.ZERO) < 0 ? new DecimalType(result.negate()) : new DecimalType(result);
        }

        private @Nullable State calculateDeltaPercent() {
            State calculatedDelta = calculateDelta();
            if (calculatedDelta == null) {
                return null;
            }

            BigDecimal bdDelta;
            BigDecimal bdBase;
            if (acceptedState instanceof QuantityType acceptedStateQuantity) {
                // Assume that delta and base are in the same unit
                bdDelta = ((QuantityType) calculatedDelta).toBigDecimal();
                bdBase = acceptedStateQuantity.toBigDecimal();
            } else {
                bdDelta = ((DecimalType) calculatedDelta).toBigDecimal();
                bdBase = ((DecimalType) acceptedState).toBigDecimal();
            }
            BigDecimal percent = bdDelta.multiply(BigDecimal.valueOf(100)).divide(bdBase, 2, RoundingMode.HALF_EVEN);
            return new DecimalType(percent);
        }
    }
}
