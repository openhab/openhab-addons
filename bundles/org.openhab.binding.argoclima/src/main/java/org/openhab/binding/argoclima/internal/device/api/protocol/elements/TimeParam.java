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

import java.time.LocalTime;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.binding.argoclima.internal.exception.ArgoConfigurationException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Time element (accepting values in HH:MM) - eg. for schedule timers on/off
 *
 * @see CurrentTimeParam
 * @see DelayMinutesParam
 * @implNote These other "time" params could technically be sharing common codebase, though for simplicity sake it was
 *           easier to implement them as unrelated (possible future refactor oppty)
 *
 * @implNote This class could use {@link LocalTime} for internal storage, but raw int has been chosen instead to cut on
 *           back and forth conversions, dealing with seconds etc... (and is simple-enough)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class TimeParam extends ArgoApiElementBase {
    /**
     * Kind of schedule parameter (on or off)
     */
    public enum TimeParamType {
        ON,
        OFF
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeParam.class);
    private static final int MIN_VALUE = 0; // 0:00
    private static final int MAX_VALUE = 23 * 60 + 59; // 23:59
    private final TimeParamType paramType;
    private Optional<Integer> currentValue = Optional.empty();

    /**
     * C-tor (allows full range of values: {@code 0:00 <> 25:59})
     *
     * @implNote Even though the Argo HVAC supports 3 schedule timers, when sent to a device, there's only one
     *           on/off/weekday option, hence value of this setting changes indirectly (when changing Schedule timer
     *           cycle)
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     * @param paramType The kind of parameter (ON or OFF time). This element requires this knowledge to be able to
     *            retrieve default value from settings (based off of currently selected timer value)
     */
    public TimeParam(IArgoSettingProvider settingsProvider, TimeParamType paramType) {
        super(settingsProvider);
        this.paramType = paramType;
    }

    /**
     * Gets the raw time value from hours and minutes (normalized to be in range of [{@link #MIN_VALUE} ,
     * {@link #MAX_VALUE}]
     *
     * @param hour Hour to convert (0..23)
     * @param minute Minute to convert (0..59)
     * @return The Argo API raw value for the time
     */
    public static int fromHhMm(int hour, int minute) {
        return normalizeTime(hour * 60 + minute);
    }

    /**
     * Converts the raw value to framework-compatible {@link State}
     *
     * @implNote While the data is technically TIME, and could be represented as
     *           {@link org.openhab.core.library.types.DateTimeType DateTimeType}, the OH framework doesn't seem to
     *           provide a class for time of day only (w/o Date component).
     *           A next best semantically-correct way of representing this value would be by
     *           {@link org.openhab.core.library.types.QuantityType QuantityType&lt;Time&gt;(..., Units.MINUTE)}}, yet
     *           this displays somewhat weirdly (as it is more suited for duration, not time of day).
     *           <p>
     *           Hence the value is represented as a {@link org.openhab.core.library.types.StringType StringType}, which
     *           makes it display "normally", and is OK for this use case, as these schedule parameters are actually
     *           **NOT** mapped to any channel (and instead sourced from config), thus not causing any awkward usage for
     *           the user
     *
     * @param value Value to convert
     * @return Converted value (or empty, on conversion failure)
     */
    private static State valueToState(Optional<Integer> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }
        return new StringType(rawValueToHHMMString(value.orElseThrow()));
    }

    private static int normalizeTime(int newValue) {
        if (newValue < MIN_VALUE) {
            LOGGER.debug("Requested value: {} would exceed minimum value: {}. Setting: {}.", newValue, MIN_VALUE,
                    MIN_VALUE);
            return MIN_VALUE;
        }
        if (newValue > MAX_VALUE) {
            LOGGER.debug("Requested value: {} would exceed maximum value: {}. Setting: {}.", newValue, MAX_VALUE,
                    MAX_VALUE);
            return MAX_VALUE;
        }
        return newValue;
    }

    private static String rawValueToHHMMString(int rawValue) {
        int hh = rawValue / 60;
        int mm = rawValue % 60;
        return String.format("%02d:%02d", hh, mm);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The currently used context of this class (on/off schedule time) has WRITE-ONLY elements, hence this
     *           method is unlikely to ever be called
     */
    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        strToInt(responseValue).ifPresent(raw -> {
            this.currentValue = Optional.of(normalizeTime(raw));
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
        return rawValueToHHMMString(currentValue.get().intValue());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Timer on/off values are always sent to the device together with other values (as long as there are other updates,
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
            return Integer.toString(currentValue.orElseThrow());
        }

        // OOPS - We have a schedule timer active already, but no value (and have to provide something). Let's fetch it
        // from the configuration
        var timerId = activeScheduleTimer.orElseThrow();

        try {
            LocalTime configuredValue;
            if (paramType == TimeParamType.ON) {
                configuredValue = settingsProvider.getScheduleProvider().getScheduleOnTime(timerId);
            } else {
                configuredValue = settingsProvider.getScheduleProvider().getScheduleOffTime(timerId);
            }
            // let's initialize our value from the config's one (lazily)
            currentValue = Optional.of(fromHhMm(configuredValue.getHour(), configuredValue.getMinute()));
            return Integer.toString(currentValue.orElseThrow());
        } catch (ArgoConfigurationException e) {
            LOGGER.debug("Retrieving default configured value for {} timer failed. Error: {}", paramType,
                    e.getMessage());
            return defaultResult;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Gets the local time value from Numbers as well as HH:MM string representation.
     *
     * @see #valueToState
     */
    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        int newRawValue;

        if (command instanceof Number numberCommand) {
            newRawValue = numberCommand.intValue(); // Raw value, not unit-aware

            if (command instanceof QuantityType<?> quantityTypeCommand) { // let's try to get it with unit
                                                                          // (opportunistically)
                var inMinutes = quantityTypeCommand.toUnit(Units.MINUTE);
                if (null != inMinutes) {
                    newRawValue = inMinutes.intValue();
                }
            }
        } else if (command instanceof StringType stringTypeCommand) {
            var asTime = LocalTime.parse(stringTypeCommand.toFullString());
            newRawValue = fromHhMm(asTime.getHour(), asTime.getMinute());
        } else {
            return HandleCommandResult.rejected(); // unsupported type of command
        }

        newRawValue = normalizeTime(newRawValue);

        // Not checking if current value is the same as requested (this is a send-always value, so no real need)
        this.currentValue = Optional.of(newRawValue);
        // Accept the command (and if it was sent when no timer was active, make it deferred)
        return HandleCommandResult.accepted(Integer.toString(newRawValue), valueToState(Optional.of(newRawValue)))
                .setDeferred(isScheduleTimerEnabled().isEmpty());
    }
}
