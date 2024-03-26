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

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.argoclima.internal.device.api.protocol.IArgoSettingProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API element representing an integer in range of allowed values
 *
 * @implNote Since ECO power limit is the only value this is used for now, the {@link #UNIT} is hard-coded to percent
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
public class RangeParam extends ArgoApiElementBase {
    private static final Unit<Dimensionless> UNIT = Units.PERCENT;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final double minValue;
    private final double maxValue;
    private Optional<Number> currentValue = Optional.empty();

    /**
     * C-tor
     *
     * @param settingsProvider the settings provider (getting device state as well as schedule configuration)
     * @param min Minimum settable value
     * @param max Maximum settable value
     */
    public RangeParam(IArgoSettingProvider settingsProvider, double min, double max) {
        super(settingsProvider);
        this.minValue = min;
        this.maxValue = max;
    }

    private static State valueToState(Optional<Number> value) {
        if (value.isEmpty()) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<Dimensionless>(value.get(), UNIT);
    }

    /**
     * Normalize value to be in range of MIN..MAX
     *
     * @implNote Even though min-max ranges are floating-point, this is operating on integers, as currently there's no
     *           use of this class which goes beyond integers
     * @param newValue The value to normalize (as int)
     * @return Normalized value
     */
    private int normalizeValue(int newValue) {
        if (newValue < minValue) {
            logger.debug("Requested value: {} would exceed minimum value: {}. Setting: {}.", newValue, minValue,
                    (int) minValue);
            return (int) minValue;
        }
        if (newValue > maxValue) {
            logger.debug("Requested value: {} would exceed maximum value: {}. Setting: {}.", newValue, maxValue,
                    (int) maxValue);
            return (int) maxValue;
        }
        return newValue;
    }

    @Override
    protected void updateFromApiResponseInternal(String responseValue) {
        strToInt(responseValue).ifPresent(raw -> {
            currentValue = Optional.of(raw);
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
        return currentValue.get().toString();
    }

    @Override
    protected HandleCommandResult handleCommandInternalEx(Command command) {
        if (command instanceof Number numberCommand) {
            final int newValue = normalizeValue(numberCommand.intValue());

            if (currentValue.map(cv -> (cv.intValue() == newValue)).orElse(false)) {
                return HandleCommandResult.rejected(); // Current value is the same as requested - nothing to do
            }
            this.currentValue = Optional.of(newValue);
            return HandleCommandResult.accepted(Integer.toString(newValue), valueToState(this.currentValue));
        }

        return HandleCommandResult.rejected();
    }
}
