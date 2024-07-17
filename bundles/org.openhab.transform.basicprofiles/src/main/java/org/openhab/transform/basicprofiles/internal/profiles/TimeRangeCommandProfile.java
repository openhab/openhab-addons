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

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.TIME_RANGE_COMMAND_UID;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.transform.basicprofiles.internal.config.TimeRangeCommandProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an enhanced implementation of a follow profile which converts {@link OnOffType} to a {@link PercentType}.
 * The value of the percent type can be different between a specific time of the day.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class TimeRangeCommandProfile implements StateProfile {

    private static final Pattern HHMM_PATTERN = Pattern.compile("^([0-1][0-9]|2[0-3])(:[0-5][0-9])$");
    private static final String TIME_SEPARATOR = ":";

    private static final String PARAM_IN_RANGE_VALUE = "inRangeValue";
    private static final String PARAM_OUT_OF_RANGE_VALUE = "outOfRangeValue";
    public static final String PARAM_START = "start";
    public static final String PARAM_END = "end";

    public static final String CONFIG_RESTORE_VALUE_OFF = "OFF";
    private static final String CONFIG_RESTORE_VALUE_PREVIOUS = "PREVIOUS";
    private static final String CONFIG_RESTORE_VALUE_NOTHING = "NOTHING";

    private final Logger logger = LoggerFactory.getLogger(TimeRangeCommandProfile.class);
    private final ProfileCallback callback;
    private final TimeZoneProvider timeZoneProvider;

    private final PercentType inRangeValue;
    private final PercentType outOfRangeValue;
    private final long startTimeInMinutes;
    private final long endTimeInMinutes;
    private final String restoreValue;

    private @Nullable PercentType previousState;
    private @Nullable PercentType restoreState;

    public TimeRangeCommandProfile(ProfileCallback callback, ProfileContext context,
            TimeZoneProvider timeZoneProvider) {
        this.callback = callback;
        this.timeZoneProvider = timeZoneProvider;

        TimeRangeCommandProfileConfig config = context.getConfiguration().as(TimeRangeCommandProfileConfig.class);
        logger.debug(
                "Configuring profile with parameters: [{inRangeValue='{}', outOfRangeValue='{}', start='{}', end='{}', restoreValue='{}']",
                config.inRangeValue, config.outOfRangeValue, config.start, config.end, config.restoreValue);

        inRangeValue = parsePercentTypeConfigValue(config.inRangeValue, PARAM_IN_RANGE_VALUE);
        outOfRangeValue = parsePercentTypeConfigValue(config.outOfRangeValue, PARAM_OUT_OF_RANGE_VALUE);

        startTimeInMinutes = parseTimeConfigValue(config.start, PARAM_START);
        endTimeInMinutes = parseTimeConfigValue(config.end, PARAM_END);
        // We have to do this here, otherwise the comparison in getOnValue() does not work. A range beyond midnight
        // (e.g. start="23:00", end="01:00") is not yet supported.
        if (endTimeInMinutes <= startTimeInMinutes) {
            logger.warn("Parameter '{}' ({}) is earlier than or equal to parameter '{}' ({}).", PARAM_END, config.end,
                    PARAM_START, config.start);
            throw new IllegalArgumentException("Invalid time range parameter");
        }

        restoreValue = config.restoreValue;
    }

    private PercentType parsePercentTypeConfigValue(int value, String field) {
        try {
            return new PercentType(value);
        } catch (IllegalArgumentException e) {
            logger.warn("Cannot parse profile configuration '{}' to percent, use a value between 0 and 100!", field);
            throw new IllegalArgumentException("Invalid profile configuration '" + field + "'", e);
        }
    }

    private long parseTimeConfigValue(String value, String field) {
        try {
            return getMinutesFromTime(value);
        } catch (PatternSyntaxException | NumberFormatException | ArithmeticException e) {
            logger.warn("Cannot parse profile configuration '{}' to hour and minutes, use pattern hh:mm!", field);
            throw new IllegalArgumentException("Invalid profile configuration '" + field + "'", e);
        }
    }

    /**
     * Parses a hh:mm string and returns the minutes.
     */
    private long getMinutesFromTime(String configTime)
            throws PatternSyntaxException, NumberFormatException, ArithmeticException {
        String time = configTime;
        if (!(time = time.trim()).isBlank()) {
            if (!HHMM_PATTERN.matcher(time).matches()) {
                throw new NumberFormatException();
            } else {
                String[] splittedConfigTime = time.split(TIME_SEPARATOR);
                if (splittedConfigTime.length < 2) {
                    throw new NumberFormatException();
                }
                int hour = Integer.parseInt(splittedConfigTime[0]);
                int minutes = Integer.parseInt(splittedConfigTime[1]);
                return Duration.ofMinutes(minutes).plusHours(hour).toMinutes();
            }
        }
        return 0;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return TIME_RANGE_COMMAND_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        if (!(state instanceof OnOffType)) {
            logger.debug("The given state '{}' cannot be transformed to an OnOffType.", state);
            return;
        }

        PercentType newCommand = OnOffType.OFF.equals(state) ? getOffValue() : getOnValue();
        if (newCommand != null) {
            logger.debug("Forward new command '{}' to the respective thing handler.", newCommand);
            if (CONFIG_RESTORE_VALUE_PREVIOUS.equals(restoreValue)) {
                logger.debug("'restoreValue' ({}): Remember previous state '{}'.", restoreValue, previousState);
                restoreState = previousState;
            }
            callback.handleCommand(newCommand);
        }
    }

    @Override
    public void onCommandFromHandler(Command command) {
        if (!(command instanceof OnOffType)) {
            logger.debug("The given command '{}' cannot be handled. It is not an OnOffType.", command);
            return;
        }

        PercentType newCommand = OnOffType.OFF.equals(command) ? getOffValue() : getOnValue();
        if (newCommand != null) {
            logger.debug("Send new command '{}' to the framework.", newCommand);
            if (CONFIG_RESTORE_VALUE_PREVIOUS.equals(restoreValue)) {
                logger.debug("'restoreValue' ({}): Remember previous state '{}'.", restoreValue, previousState);
                restoreState = previousState;
            }
            callback.sendCommand(newCommand);
        }
    }

    private @Nullable PercentType getOffValue() {
        switch (restoreValue) {
            case CONFIG_RESTORE_VALUE_OFF:
                return PercentType.ZERO;
            case CONFIG_RESTORE_VALUE_NOTHING:
                logger.debug("'restoreValue' ({}): Do nothing.", restoreValue);
                return null;
            case CONFIG_RESTORE_VALUE_PREVIOUS:
                return restoreState;
            default:
                // try to transform config parameter 'restoreValue' to a valid PercentType
                try {
                    return PercentType.valueOf(restoreValue);
                } catch (IllegalArgumentException e) {
                    logger.warn("The given parameter 'restoreValue' ({}) cannot be transformed to a valid PercentType.",
                            restoreValue);
                    return null;
                }
        }
    }

    private PercentType getOnValue() {
        ZonedDateTime now = Instant.now().atZone(timeZoneProvider.getTimeZone());
        ZonedDateTime today = now.truncatedTo(ChronoUnit.DAYS);
        return now.isAfter(today.plusMinutes(startTimeInMinutes)) && now.isBefore(today.plusMinutes(endTimeInMinutes))
                ? inRangeValue
                : outOfRangeValue;
    }

    @Override
    public void onCommandFromItem(Command command) {
        // no-op
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        PercentType pState = state.as(PercentType.class);
        if (pState != null) {
            logger.debug("Item state changed, set 'previousState' to '{}'.", pState);
            previousState = pState;
        }
    }
}
