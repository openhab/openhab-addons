/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.UnDefType;
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
    private final Logger logger = LoggerFactory.getLogger(NumberValue.class);
    private final @Nullable BigDecimal min;
    private final @Nullable BigDecimal max;
    private final BigDecimal step;

    public NumberValue(@Nullable BigDecimal min, @Nullable BigDecimal max, @Nullable BigDecimal step) {
        super(CoreItemFactory.NUMBER, Stream.of(DecimalType.class, IncreaseDecreaseType.class, UpDownType.class)
                .collect(Collectors.toList()));
        this.min = min;
        this.max = max;
        this.step = step == null ? new BigDecimal(1.0) : step;
    }

    protected boolean checkConditions(BigDecimal newValue, DecimalType oldvalue) {
        if (min != null && newValue.compareTo(min) == -1) {
            logger.trace("Number not accepted as it is below the configured minimum");
            return false;
        }
        if (max != null && newValue.compareTo(max) == 1) {
            logger.trace("Number not accepted as it is above the configured maximum");
            return false;
        }

        return true;
    }

    @Override
    public void update(Command command) throws IllegalArgumentException {
        DecimalType oldvalue = (state == UnDefType.UNDEF) ? new DecimalType() : (DecimalType) state;
        BigDecimal newValue;
        if (command instanceof DecimalType) {
            if (!checkConditions(((DecimalType) command).toBigDecimal(), oldvalue)) {
                return;
            }
            state = (DecimalType) command;
        } else if (command instanceof IncreaseDecreaseType || command instanceof UpDownType) {
            if (command == IncreaseDecreaseType.INCREASE || command == UpDownType.UP) {
                newValue = oldvalue.toBigDecimal().add(step);
            } else {
                newValue = oldvalue.toBigDecimal().subtract(step);
            }
            if (!checkConditions(newValue, oldvalue)) {
                return;
            }
            state = new DecimalType(newValue);
        } else {
            newValue = new BigDecimal(command.toString());
            if (!checkConditions(newValue, oldvalue)) {
                return;
            }
            state = new DecimalType(newValue);
        }
    }

    @Override
    public StateDescription createStateDescription(String unit, boolean readOnly) {
        return new StateDescription(min, max, step, "%s " + unit.replace("%", "%%"), readOnly, Collections.emptyList());
    }
}
