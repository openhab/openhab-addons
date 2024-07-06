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
package org.openhab.binding.argoclima.internal.device.api.protocol.elements;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.binding.argoclima.internal.device.api.types.Weekday;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.binding.argoclima.internal.utils.StringUtils;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Weekdays element (accepting sets of days for schedule to run on)
 *
 * @see TimeParam
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class WeekdayParam extends ArgoApiElementBase {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Optional<EnumSet<Weekday>> currentValue = Optional.empty();

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     */
    public WeekdayParam(IArgoSettingProvider settingsProvider) {
        super(settingsProvider);
    }

    /**
     * Converts the internal {@code EnumSet}-based storage to raw ARGO API value ("flags enum" - represented as
     * int/bitmap)
     *
     * @param values The set of days to convert
     * @implNote This impl. assumes all the values are in range of the underlying enum type (no craziness such as
     *           casting 1000 to Weekday)
     * @return Int representation of the set of weekdays
     */
    public static int toRawValue(EnumSet<Weekday> values) {
        int ret = 0;
        for (Weekday val : values) {
            ret |= val.getIntValue();
        }
        return ret;
    }

    /**
     * Unpacks the Argo API integer with flags-packed weekdays into EnumSet
     *
     * @implNote This is not checking if the int value is not having values outside of the Enum values (these will be
     *           silently skipped on conversion!). Could do a bitmask-based sanity check, but... Occam's Razor ;)
     *
     * @param value The raw value to convert
     * @return Unpacked value
     */
    public static EnumSet<Weekday> fromRawValue(int value) {
        EnumSet<Weekday> ret = EnumSet.noneOf(Weekday.class);
        for (Weekday val : EnumSet.allOf(Weekday.class)) {
            if ((val.getIntValue() & value) != 0) {
                ret.add(val);
            }
        }
        return ret;
    }

    /**
     * Converts the raw value to framework-compatible {@link State}
     *
     * @implNote While the raw data is technically an integer, and could be represented as
     *           {@link org.openhab.core.library.types.DecimalType DecimalType}, a {@code String} was chosen for better
     *           readability
     *           <p>
     *           This parameter is actually **NOT** mapped to any channel (and instead sourced from config), thus not
     *           causing any awkward usage for the user
     *
     * @param value Value to convert
     * @return Converted value (or empty, on conversion failure)
     */
    private static State valueToState(Optional<EnumSet<Weekday>> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }
        return new StringType(value.orElseThrow().toString());
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The currently used context of this class (schedule timer) has WRITE-ONLY elements, hence this
     *           method is unlikely to ever be called
     */
    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        strToInt(responseValue).ifPresent(raw -> {
            this.currentValue = Optional.of(fromRawValue(raw));
        });
    }

    @Override
    public State toState() {
        return valueToState(currentValue);
    }

    @Override
    public String toString() {
        if (currentValue.isEmpty()) {
            return "???";
        }
        return Objects.requireNonNull(currentValue.orElseThrow().toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Timer weekday values are always sent to the device together with other values (as long as there are other
     * updates,
     * and any schedule timer is currently active)
     */
    @Override
    public boolean isAlwaysSent() {
        return isScheduleTimerEnabled().isPresent();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Specialized implementation allowing to get a value from default config provider (if it wansn't set before)
     * Since the value is write-only and framework's value may be N/A we need to re-fetch it in such case.
     */
    @Override
    public String getDeviceApiValue() {
        var defaultResult = super.getDeviceApiValue();
        var activeScheduleTimer = isScheduleTimerEnabled();

        if (!ArgoDeviceStatus.NO_VALUE.equals(defaultResult) || activeScheduleTimer.isEmpty()) {
            return defaultResult; // There's already a pending command recognized by binding, or schedule timer is off -
                                  // we're good to go with the default
        }

        if (currentValue.isPresent()) {
            // We have a value, and schedule timer is enabled, so let's send it
            // Consideration: Only send those as long as the pending command is *schedule timer change*, not *any
            // change*?... Seems to not be required though so... YAGNI
            return Integer.toString(toRawValue(currentValue.get()));
        }

        // OOPS - We have a schedule timer active already, but no value (and have to provide something). Let's fetch it
        // from the configuration
        var timerId = activeScheduleTimer.orElseThrow();

        try {
            EnumSet<Weekday> configuredValue = settingsProvider.getScheduleProvider().getScheduleDayOfWeek(timerId);

            // let's initialize our value from the config's one (lazily)
            currentValue = Optional.of(configuredValue);
            return Integer.toString(toRawValue(currentValue.get()));
        } catch (ArgoConfigurationException e) {
            logger.debug("Retrieving configured weekdays value for timer failed. Error: {}", e.getMessage());
            return defaultResult;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Gets the local time value from Numbers as well as comma-separated String representation such as
     * {@code [SUN, MON, TUE, WED, THU, FRI, SAT]}
     *
     * @see #valueToState
     */
    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        EnumSet<Weekday> newValue;

        if (command instanceof Number numberCommand) {
            var rawValue = numberCommand.intValue();
            newValue = fromRawValue(rawValue);
        } else if (command instanceof StringType stringTypeCommand) {
            var toParse = StringUtils.strip(stringTypeCommand.toFullString(), "[]{}()");
            EnumSet<Weekday> parsed = EnumSet.noneOf(Weekday.class);
            for (String s : toParse.split(",")) {
                parsed.add(Weekday.valueOf(s.strip()));
            }
            newValue = parsed;
        } else {
            return HandleCommandResult.rejected(); // unsupported type of command
        }

        // Not checking if current value is the same as requested (this is a send-always value, so no real need)
        this.currentValue = Optional.of(newValue);
        // Accept the command (and if it was sent when no timer was active, make it deferred)
        return HandleCommandResult.accepted(Integer.toString(toRawValue(newValue)), valueToState(Optional.of(newValue)))
                .setDeferred(isScheduleTimerEnabled().isEmpty());
    }
}
