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
package org.openhab.binding.mqtt.generic.values;

import java.math.BigDecimal;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a number value.
 *
 * If min / max limits are set, values below / above are (almost) silently ignored.
 *
 * <p>
 * Accepts user updates and MQTT state updates from a DecimalType, IncreaseDecreaseType and UpDownType.
 * </p>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NumberValue extends Value {
    private static final String NAN = "NaN";
    private static final String NEGATIVE_NAN = "-NaN";

    private final Logger logger = LoggerFactory.getLogger(NumberValue.class);
    private final @Nullable BigDecimal min;
    private final @Nullable BigDecimal max;
    private final BigDecimal step;
    private final @Nullable Unit<?> unit;

    public NumberValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step,
            @Nullable Unit<?> unit) {
        super(CoreItemFactory.NUMBER, List.of(DecimalType.class, QuantityType.class, IncreaseDecreaseType.class,
                UpDownType.class, StringType.class));
        this.min = min;
        this.max = max;
        this.step = step == null ? BigDecimal.ONE : step;
        this.unit = unit;
    }

    protected boolean checkConditions(BigDecimal newValue) {
        BigDecimal min = this.min;
        if (min != null && newValue.compareTo(min) == -1) {
            logger.trace("Number not accepted as it is below the configured minimum");
            return false;
        }
        BigDecimal max = this.max;
        if (max != null && newValue.compareTo(max) == 1) {
            logger.trace("Number not accepted as it is above the configured maximum");
            return false;
        }

        return true;
    }

    @Override
    public String getMQTTpublishValue(Command command, @Nullable String pattern) {
        String formatPattern = pattern;
        if (formatPattern == null) {
            if (command instanceof DecimalType || command instanceof QuantityType<?>) {
                formatPattern = "%.0f";
            } else {
                formatPattern = "%s";
            }
        }

        return command.format(formatPattern);
    }

    @Override
    public Command parseCommand(Command command) throws IllegalArgumentException {
        BigDecimal newValue = null;
        if (command instanceof DecimalType decimalCommand) {
            newValue = decimalCommand.toBigDecimal();
        } else if (command instanceof IncreaseDecreaseType || command instanceof UpDownType) {
            BigDecimal oldValue = getOldValue();
            if (command == IncreaseDecreaseType.INCREASE || command == UpDownType.UP) {
                newValue = oldValue.add(step);
            } else {
                newValue = oldValue.subtract(step);
            }
        } else if (command instanceof QuantityType<?> quantityCommand) {
            newValue = getQuantityTypeAsDecimal(quantityCommand);
        } else {
            newValue = new BigDecimal(command.toString());
        }
        if (!checkConditions(newValue)) {
            throw new IllegalArgumentException(newValue + " is out of range");
        }
        // items with units specified in the label in the UI but no unit on mqtt are stored as
        // DecimalType to avoid conversions (e.g. % expects 0-1 rather than 0-100)
        Unit<?> unit = this.unit;
        if (unit != null) {
            return new QuantityType<>(newValue, unit);
        } else {
            return new DecimalType(newValue);
        }
    }

    @Override
    public Type parseMessage(Command command) throws IllegalArgumentException {
        if (command instanceof StringType) {
            if (command.toString().equalsIgnoreCase(NAN) || command.toString().equalsIgnoreCase(NEGATIVE_NAN)) {
                return UnDefType.UNDEF;
            } else if (command.toString().isEmpty()) {
                return UnDefType.NULL;
            }
        }
        return parseCommand(command);
    }

    private BigDecimal getOldValue() {
        BigDecimal val = BigDecimal.ZERO;
        if (state instanceof DecimalType decimalCommand) {
            val = decimalCommand.toBigDecimal();
        } else if (state instanceof QuantityType<?> quantityCommand) {
            val = quantityCommand.toBigDecimal();
        }
        return val;
    }

    private BigDecimal getQuantityTypeAsDecimal(QuantityType<?> qType) {
        BigDecimal val = qType.toBigDecimal();
        Unit<?> unit = this.unit;
        if (unit != null) {
            QuantityType<?> convertedType = qType.toInvertibleUnit(unit);
            if (convertedType != null) {
                val = convertedType.toBigDecimal();
            }
        }
        return val;
    }

    @Override
    public StateDescriptionFragmentBuilder createStateDescription(boolean readOnly) {
        StateDescriptionFragmentBuilder builder = super.createStateDescription(readOnly);
        BigDecimal max = this.max;
        if (max != null) {
            builder = builder.withMaximum(max);
        }
        BigDecimal min = this.min;
        if (min != null) {
            builder = builder.withMinimum(min);
        }
        if (unit != null) {
            builder.withPattern("%.0f %unit%");
        } else {
            builder.withPattern("%.0f");
        }
        return builder.withStep(step);
    }
}
