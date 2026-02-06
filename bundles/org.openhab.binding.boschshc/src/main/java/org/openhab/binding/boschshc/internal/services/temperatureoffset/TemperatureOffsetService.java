/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.temperatureoffset;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.BoschSHCService;
import org.openhab.binding.boschshc.internal.services.temperatureoffset.dto.TemperatureOffsetServiceState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;

/**
 * Service to configure temperature offsets for thermostats.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class TemperatureOffsetService extends BoschSHCService<TemperatureOffsetServiceState> {

    public static final String TEMPERATURE_OFFSET_SERVICE_NAME = "TemperatureOffset";

    public static final BigDecimal MINIMUM_OFFSET = BigDecimal.valueOf(-5);
    public static final BigDecimal MAXIMUM_OFFSET = BigDecimal.valueOf(5);

    public TemperatureOffsetService() {
        super(TEMPERATURE_OFFSET_SERVICE_NAME, TemperatureOffsetServiceState.class);
    }

    @Override
    public TemperatureOffsetServiceState handleCommand(Command command) throws BoschSHCException {
        if (command instanceof DecimalType numberCommand) {
            return createNewTemperatureOffsetState(numberCommand.toBigDecimal());
        } else if (command instanceof QuantityType<?> quantityCommand) {
            @Nullable
            QuantityType<Temperature> relativeValue = getRelativeTemperatureValue(quantityCommand);
            if (relativeValue != null) {
                return createNewTemperatureOffsetState(relativeValue.toBigDecimal());
            }
        }
        return super.handleCommand(command);
    }

    @Nullable
    private QuantityType<Temperature> getRelativeTemperatureValue(QuantityType<?> quantityCommand) {
        // check if the given quantity has a temperature unit
        if (!quantityCommand.getUnit().getSystemUnit().equals(Units.KELVIN)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        QuantityType<Temperature> temperatureCommand = (QuantityType<Temperature>) quantityCommand;
        return temperatureCommand.toUnitRelative(SIUnits.CELSIUS);
    }

    private TemperatureOffsetServiceState createNewTemperatureOffsetState(BigDecimal offset) {
        TemperatureOffsetServiceState state = new TemperatureOffsetServiceState();
        state.offset = sanitizeOffsetValue(offset);
        return state;
    }

    static double sanitizeOffsetValue(BigDecimal bigDecimal) {
        if (bigDecimal.compareTo(MINIMUM_OFFSET) < 0) {
            return MINIMUM_OFFSET.doubleValue();
        } else if (bigDecimal.compareTo(MAXIMUM_OFFSET) > 0) {
            return MAXIMUM_OFFSET.doubleValue();
        }

        // accept at most one decimal digit
        if (bigDecimal.scale() > 1) {
            // round the value in case more than one decimal digits are provided
            return bigDecimal.setScale(1, RoundingMode.HALF_UP).doubleValue();
        }

        return bigDecimal.doubleValue();
    }
}
