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

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.ArgoDeviceStatus;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.binding.argoclima.internal.device.api.types.ArgoDeviceSettingType;
import org.openhab.binding.argoclima.internal.device.api.types.TimerType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Delay timer element (accepting values in minutes and constrained in both range and precision)
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class DelayMinutesParam extends ArgoApiElementBase {
    private final int minValue;
    private final int maxValue;
    private final int step;
    private Optional<Integer> currentValue;

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     * @param min Minimum value of this timer (in minutes)
     * @param max Maximum value of this timer (in minutes)
     * @param step Minimum step of the timer (values will be rounded to nearest step, increments/decrements will move by
     *            step)
     * @param initialValue The initial value of this setting, in minutes (since the value is write-only, need to provide
     *            a value for the increments/decrements to work)
     */
    public DelayMinutesParam(IArgoSettingProvider settingsProvider, int min, int max, int step,
            Optional<Integer> initialValue) {
        super(settingsProvider);
        this.minValue = min;
        this.maxValue = max;
        this.step = step;
        this.currentValue = initialValue;
    }

    /**
     * Converts the raw value to framework-compatible {@link State}
     *
     * @param value Value to convert
     * @return Converted value (or {@code UNDEF} on conversion failure)
     */
    private static State valueToState(Optional<Integer> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }

        return new QuantityType<Time>(value.get(), Units.MINUTE);
    }

    /**
     * @see {@link ArgoApiElementBase#adjustRange}
     */
    private int adjustRange(int newValue) {
        return ArgoApiElementBase.adjustRange(newValue, minValue, maxValue, Optional.of(step), " min").intValue();
    }

    /**
     * @see {@link ArgoApiElementBase#adjustRangeWithAmplification}
     */
    private int adjustRangeWithAmplification(int newValue) {
        return ArgoApiElementBase.adjustRangeWithAmplification(newValue, currentValue, minValue, maxValue, step, " min")
                .intValue();
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
            currentValue = Optional.of(adjustRange(raw));
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
        return currentValue.get().toString() + " min";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Timer delay value is always sent to the device together with Timer=Delay command
     * (so that the clock resets)
     */
    @Override
    public boolean isAlwaysSent() {
        return isDelayTimerBeingActivated();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The delay timer value should be send whenever there's an active change (command) to a delay timer (technically
     * flipping from Delay timer back to the Delay timer, w/o changing the delay value should re-arm the timer)
     */
    @Override
    public String getDeviceApiValue() {
        var defaultResult = super.getDeviceApiValue();

        if (!ArgoDeviceStatus.NO_VALUE.equals(defaultResult) || currentValue.isEmpty()
                || !isDelayTimerBeingActivated()) {
            return defaultResult; // There's already a pending command recognized by binding, or delay timer is has no
                                  // pending command -
                                  // we're good to go with the default
        }

        // There's a pending change to Delay timer -> let's send our value then
        return Integer.toString(currentValue.orElseThrow());
    }

    /**
     * Checks if Delay timer is currently being commanded to become active on the device (pending commands!)
     *
     * @return True, if delay timer is currently being activated on the device, False otherwise
     */
    private boolean isDelayTimerBeingActivated() {
        var setting = settingsProvider.getSetting(ArgoDeviceSettingType.ACTIVE_TIMER);
        var currentTimerValue = EnumParam.fromType(setting.getState(), TimerType.class);

        var isDelayCurrentlySet = currentTimerValue.map(t -> t.equals(TimerType.DELAY_TIMER)).orElse(false);

        return isDelayCurrentlySet && setting.hasInFlightCommand();
    }

    /**
     * Checks if Delay timer is active already (or being commanded to do so)
     * <p>
     * Used to defer timer value updates in case there's no timer action ongoing (no need to send the timer value to the
     * device)
     *
     * @return True, if delay timer is currently active on the device, False otherwise
     */
    private final boolean isDelayTimerCurrentlyActive() {
        var currentTimer = EnumParam
                .fromType(settingsProvider.getSetting(ArgoDeviceSettingType.ACTIVE_TIMER).getState(), TimerType.class);

        return currentTimer.map(t -> t.equals(TimerType.DELAY_TIMER)).orElse(false);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Since this method rounds to next step and some updates may be missed, we're forcing any direction
     *           movements to move a full step through {@link #adjustRangeWithAmplification(int)}
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
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseTypeCommand) {
            var base = this.currentValue.orElse(adjustRange((this.minValue + this.maxValue) / 2));
            if (IncreaseDecreaseType.INCREASE.equals(increaseDecreaseTypeCommand)) {
                base += step;
            } else if (IncreaseDecreaseType.DECREASE.equals(increaseDecreaseTypeCommand)) {
                base -= step;
            }
            newRawValue = base;
        } else {
            return HandleCommandResult.rejected(); // unsupported type of command
        }

        newRawValue = adjustRangeWithAmplification(newRawValue);

        // Not checking if current value is the same as requested (delay timer set resets the clock)
        this.currentValue = Optional.of(newRawValue);

        // Accept the command (and if it was sent when no timer was active, make it deferred)
        return HandleCommandResult.accepted(Integer.toString(newRawValue), valueToState(Optional.of(newRawValue)))
                .setDeferred(!isDelayTimerCurrentlyActive());
    }
}
