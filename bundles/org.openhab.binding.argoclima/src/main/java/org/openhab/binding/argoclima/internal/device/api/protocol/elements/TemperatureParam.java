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

import java.util.Optional;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The element for controlling/receiving temperature
 * <p>
 * Device API always communicates in degrees Celsius, even if the display unit (configurable) is Fahrenheit.
 * <p>
 * While the settable temperature seems to be by 0.5 °C (at least this is what the remote API does), the reported temp.
 * is by 0.1 °C and technically the device accepts setting values with such precision. This is not practiced though, not
 * to introduce unknown side-effects
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class TemperatureParam extends ArgoApiElementBase {
    private final double minValue;
    private final double maxValue;
    private final double step;
    private Optional<Double> currentValue = Optional.empty();

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     * @param min Minimum value of this timer (in minutes)
     * @param max Maximum value of this timer (in minutes)
     * @param step Minimum step of the timer (values will be rounded to nearest step, increments/decrements will move by
     *            step). Step dictates the resolution of this param
     */
    public TemperatureParam(IArgoSettingProvider settingsProvider, double min, double max, double step) {
        super(settingsProvider);
        this.minValue = min;
        this.maxValue = max;
        this.step = step;
    }

    /**
     * Converts the raw value to framework-compatible {@link State} (always in degrees Celsius
     *
     * @param value Value to convert
     * @return Converted value (or empty, on conversion failure)
     */
    private static State valueToState(Optional<Double> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<Temperature>(value.get(), SIUnits.CELSIUS);
    }

    /**
     * @see {@link ArgoApiElementBase#adjustRangeWithAmplification}
     */
    private double adjustRangeWithAmplification(double newValue) {
        var normalized = ArgoApiElementBase
                .adjustRangeWithAmplification(newValue, currentValue, minValue, maxValue, step, " °C").doubleValue();
        return Math.round(normalized * 10.0) / 10.0; // single-digit precision
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The raw API uses integers and degrees Celsius. Temperature is multiplied by 10.
     * @implNote Deliberately not normalizing incoming value (if the device reported it, let's consider it valid, even
     *           if it is out of range!)
     */
    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        strToInt(responseValue).ifPresent(raw -> {
            this.currentValue = Optional.of(raw / 10.0);
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
        return currentValue.get().toString() + " °C";
    }

    /**
     * {@inheritDoc}
     *
     * @implNote The raw API uses integers and degrees Celsius. Temperature is multiplied by 10.
     */
    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        double newRawValue;

        if (command instanceof Number numberCommand) {
            newRawValue = numberCommand.doubleValue(); // Raw value, not unit-aware

            if (command instanceof QuantityType<?> quantityTypeCommand) { // let's try to get it with unit
                                                                          // (opportunistically)
                var inCelsius = quantityTypeCommand.toUnit(SIUnits.CELSIUS);
                if (null != inCelsius) {
                    newRawValue = inCelsius.doubleValue();
                }
            }
        } else {
            return HandleCommandResult.rejected(); // unsupported type of command
        }

        newRawValue = adjustRangeWithAmplification(newRawValue);

        this.currentValue = Optional.of(newRawValue);
        // Accept the command
        return HandleCommandResult.accepted(Integer.toUnsignedString((int) (newRawValue * 10.0)),
                valueToState(Optional.of(newRawValue)));
    }
}
