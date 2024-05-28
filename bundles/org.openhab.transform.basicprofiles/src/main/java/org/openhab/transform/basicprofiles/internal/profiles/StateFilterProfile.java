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

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.STATE_FILTER_UID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.transform.basicprofiles.internal.config.StateFilterProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accepts updates to state as long as conditions are met. Support for sending fixed state if conditions are *not*
 * met.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class StateFilterProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(StateFilterProfile.class);

    private final ItemRegistry itemRegistry;
    private final ProfileCallback callback;
    private List<Class<? extends State>> acceptedDataTypes;

    private List<StateCondition> conditions = List.of();

    private @Nullable State configMismatchState = null;

    public StateFilterProfile(ProfileCallback callback, ProfileContext context, ItemRegistry itemRegistry) {
        this.callback = callback;
        acceptedDataTypes = context.getAcceptedDataTypes();
        this.itemRegistry = itemRegistry;

        StateFilterProfileConfig config = context.getConfiguration().as(StateFilterProfileConfig.class);
        if (config != null) {
            conditions = parseConditions(config.conditions, config.separator);
            configMismatchState = parseState(config.mismatchState);
        }
    }

    private List<StateCondition> parseConditions(@Nullable String config, String separator) {
        if (config == null) {
            return List.of();
        }

        List<StateCondition> parsedConditions = new ArrayList<>();
        try {
            String[] expressions = config.split(separator);
            for (String expression : expressions) {
                String[] parts = expression.trim().split("\s");
                if (parts.length == 3) {
                    String itemName = parts[0];
                    StateCondition.ComparisonType conditionType = StateCondition.ComparisonType
                            .valueOf(parts[1].toUpperCase(Locale.ROOT));
                    String value = parts[2];
                    parsedConditions.add(new StateCondition(itemName, conditionType, value));
                } else {
                    logger.warn("Malformed condition expression: '{}'", expression);
                }
            }

            return parsedConditions;
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot parse condition {}. Expected format ITEM_NAME <EQ|NEQ> STATE_VALUE: '{}'", config,
                    e.getMessage());
            return List.of();
        }
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
        if (!conditions.isEmpty()) {
            boolean allConditionsMet = true;
            for (StateCondition condition : conditions) {
                logger.debug("Evaluting condition: {}", condition);
                try {
                    Item item = itemRegistry.getItem(condition.itemName);
                    String itemState = item.getState().toString();

                    if (!condition.matches(itemState)) {
                        allConditionsMet = false;
                    }
                } catch (ItemNotFoundException e) {
                    logger.warn(
                            "Cannot find item '{}' in registry - check your condition expression - skipping state update",
                            condition.itemName);
                    allConditionsMet = false;
                }

            }
            if (allConditionsMet) {
                return state;
            } else {
                return configMismatchState;
            }
        } else {
            logger.warn(
                    "No configuration defined for StateFilterProfile (check for log messages when instantiating profile) - skipping state update");
        }

        return null;
    }

    @Nullable
    State parseState(@Nullable String stateString) {
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
        String itemName;

        ComparisonType comparisonType;
        String value;

        boolean quoted = false;

        public StateCondition(String itemName, ComparisonType comparisonType, String value) {
            this.itemName = itemName;
            this.comparisonType = comparisonType;
            this.value = value;
            this.quoted = value.startsWith("'") && value.endsWith("'");
            if (quoted) {
                this.value = value.substring(1, value.length() - 1);
            }
        }

        public boolean matches(String state) {
            switch (comparisonType) {
                case EQ:
                    return state.equals(value);
                case NEQ: {
                    return !state.equals(value);
                }
                default:
                    logger.warn("Unknown condition type {}. Expected 'eq' or 'neq' - skipping state update",
                            comparisonType);
                    return false;

            }
        }

        enum ComparisonType {
            EQ,
            NEQ
        }

        @Override
        public String toString() {
            return "Condition{itemName='" + itemName + "', comparisonType=" + comparisonType + ", value='" + value
                    + "'}'";
        }
    }
}
