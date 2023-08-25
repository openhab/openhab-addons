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
package org.openhab.io.hueemulation.internal.automation;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.handler.BaseModuleHandler;
import org.openhab.core.automation.handler.ConditionHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueGroupEntry;
import org.openhab.io.hueemulation.internal.dto.HueLightEntry;
import org.openhab.io.hueemulation.internal.dto.HueRuleEntry;
import org.openhab.io.hueemulation.internal.dto.HueRuleEntry.Operator;
import org.openhab.io.hueemulation.internal.dto.HueSensorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This condition is parameterized with Hue rule condition arguments. A Hue rule works
 * on the Hue datastore and considers lights / groups / sensors that are available there.
 * <p>
 * Implementation details: The predicate function for the condition is computed in the constructor of
 * this condition. It will only be called and evaluated for isSatisfied.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueRuleConditionHandler extends BaseModuleHandler<Condition> implements ConditionHandler {

    private final Logger logger = LoggerFactory.getLogger(HueRuleConditionHandler.class);

    public static final String MODULE_TYPE_ID = "hue.ruleCondition";
    public static final String CALLBACK_CONTEXT_NAME = "CALLBACK";
    public static final String MODULE_CONTEXT_NAME = "MODULE";

    public static final String CFG_ADDRESS = "address";
    public static final String CFG_OP = "operator";
    public static final String CFG_VALUE = "value";

    private static final String TIME_FORMAT = "HH:mm:ss";

    protected final HueRuleEntry.Condition config;
    protected String itemUID;

    protected Predicate<State> predicate;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(TIME_FORMAT);
    private final Pattern timePattern = Pattern.compile("W(.*)/T(.*)/T(.*)");
    // weekdays range from Monday to Sunday (1-7). The first entry is not used
    private final boolean[] weekDaysAllowed = { false, false, false, false, false, false, false, false };

    @SuppressWarnings({ "null", "unused" })
    public HueRuleConditionHandler(Condition module, HueDataStore ds) {
        super(module);
        config = module.getConfiguration().as(HueRuleEntry.Condition.class);

        // pattern: "/sensors/2/state/buttonevent" or "/config/localtime"
        String[] validation = config.address.split("/");
        String uid = validation[2];

        if ("groups".equals(validation[1]) && "action".equals(validation[3])) {
            HueGroupEntry entry = ds.groups.get(uid);
            if (entry == null) {
                throw new IllegalStateException("Group does not exist: " + uid);
            }
            itemUID = entry.groupItem.getUID();
        } else if ("lights".equals(validation[1]) && "state".equals(validation[3])) {
            HueLightEntry entry = ds.lights.get(uid);
            if (entry == null) {
                throw new IllegalStateException("Light does not exist: " + uid);
            }
            itemUID = entry.item.getUID();
        } else if ("sensors".equals(validation[1]) && "state".equals(validation[3])) {
            HueSensorEntry entry = ds.sensors.get(uid);
            if (entry == null) {
                throw new IllegalStateException("Sensor does not exist: " + uid);
            }
            itemUID = entry.item.getUID();
        } else if ("config".equals(validation[1]) && "localtime".equals(validation[2])) {
            // Item not used
            itemUID = "";
        } else {
            throw new IllegalStateException("Can only handle groups and lights");
        }

        if (itemUID == null) {
            throw new IllegalStateException("Can only handle groups and lights");
        }

        final String value = config.value;
        switch (config.operator) {
            case eq:
                if (value == null) {
                    throw new IllegalStateException("Equal operator requires a value!");
                }
                predicate = state -> {
                    if (state instanceof Number) {
                        return Integer.valueOf(value) == ((Number) state).intValue();
                    } else if (state instanceof OnOffType) {
                        return Boolean.valueOf(value) == (((OnOffType) state) == OnOffType.ON);
                    } else if (state instanceof OpenClosedType) {
                        return Boolean.valueOf(value) == (((OpenClosedType) state) == OpenClosedType.OPEN);
                    }
                    return state.toFullString().equals(value);
                };
                break;
            case gt:
                if (value == null) {
                    throw new IllegalStateException("GreaterThan operator requires a value!");
                } else {
                    final Integer integer = Integer.valueOf(value);

                    predicate = state -> {
                        if (state instanceof Number) {
                            return integer < ((Number) state).intValue();
                        } else {
                            return false;
                        }
                    };
                }
                break;
            case lt:
                if (value == null) {
                    throw new IllegalStateException("LowerThan operator requires a value!");
                } else {
                    final Integer integer = Integer.valueOf(value);

                    predicate = state -> {
                        if (state instanceof Number) {
                            return integer > ((Number) state).intValue();
                        } else {
                            return false;
                        }
                    };
                }
                break;
            case in:
            case not_in:
                if (value == null) {
                    throw new IllegalStateException("InRange operator requires a value!");
                }

                Matcher m = timePattern.matcher(!value.startsWith("W") ? "W127/" + value : value);
                if (!m.matches() || m.groupCount() < 3) {
                    throw new IllegalStateException(
                            "Time pattern incorrect for in/not_in hue rule condition: " + value);
                }

                final LocalTime timeStart = LocalTime.from(timeFormatter.parse(m.group(2)));
                final LocalTime timeEnd = LocalTime.from(timeFormatter.parse(m.group(3)));

                // Monday = 64, Tuesday = 32, Wednesday = 16, Thursday = 8, Friday = 4, Saturday = 2, Sunday = 1
                int weekdaysBinaryEncoded = Integer.valueOf(m.group(1));
                List<String> cronWeekdays = new ArrayList<>();
                for (int bin = 64, c = 1; bin > 0; bin /= 2, c += 1) {
                    if (weekdaysBinaryEncoded / bin == 1) {
                        weekdaysBinaryEncoded = weekdaysBinaryEncoded % bin;
                        weekDaysAllowed[c] = true;
                    }
                }

                predicate = state -> {
                    LocalDateTime now = getNow();
                    int dow = now.get(ChronoField.DAY_OF_WEEK);
                    LocalTime localTime = now.toLocalTime();
                    return weekDaysAllowed[dow] && localTime.isAfter(timeStart) && localTime.isBefore(timeEnd)
                            && config.operator == Operator.in;
                };
                break;
            default:
                predicate = s -> true;
                break;
        }
    }

    // For test injection
    protected LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    @NonNullByDefault({})
    @Override
    public boolean isSatisfied(Map<String, Object> context) {
        switch (config.operator) {
            case in:
            case not_in:
                return predicate.test(OnOffType.ON);
            default:
        }

        State state = (State) context.get("newState");
        State oldState = (State) context.get("oldState");

        if (state == null || oldState == null) {
            logger.warn("Expected a state and oldState input or an in/not_in operator!");
            return false;
        }

        switch (config.operator) {
            case ddx: // Item changes always satisfies the "hue change" and "hue change delay" condition
            case dx:
                return true;
            case eq:
            case gt:
            case lt:
                return predicate.test(state);
            case not_stable: // state changed?
                return (!state.toFullString().equals(oldState.toFullString()));
            case stable: // state stable?
                return (state.toFullString().equals(oldState.toFullString()));
            case unknown:
            default:
                logger.warn("Operator {} not handled! ", config.operator.name());
                return false;
        }
    }
}
