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

package org.openhab.binding.ism8.internal.util;

import java.util.Objects;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ism8.server.IDataPoint;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ism8DomainMap} class holds static methods for domain mapping
 *
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public final class Ism8DomainMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ism8DomainMap.class);

    public static State toOpenHABState(IDataPoint dataPoint) {
        Object value = dataPoint.getValueObject();
        if (value == null) {
            return UnDefType.NULL;
        }

        Unit<?> unit = dataPoint.getUnit();
        if (SIUnits.CELSIUS.equals(unit)) {
            return new QuantityType<Temperature>((Double) value, SIUnits.CELSIUS);
        } else if (Units.KELVIN.equals(unit)) {
            return new QuantityType<Temperature>((Double) value, Units.KELVIN);
        } else if (Units.CUBICMETRE_PER_HOUR.equals(unit)) {
            return new QuantityType<VolumetricFlowRate>((Double) value, Units.CUBICMETRE_PER_HOUR);
        } else if (Units.BAR.equals(unit)) {
            return new QuantityType<Pressure>((Double) value, Units.BAR);
        } else if (Units.PERCENT.equals(unit)) {
            return new QuantityType<Dimensionless>((Double) value, Units.PERCENT);
        } else if (Units.ONE.equals(unit)) {
            return new QuantityType<Dimensionless>((Double) value, Units.ONE);
        } else if (value instanceof Boolean) {
            return OnOffType.from((boolean) value);
        } else if (value instanceof Byte) {
            return new QuantityType<Dimensionless>((byte) value, Units.ONE);
        }

        LOGGER.debug("Failed to map DataPoint id: {} val: {}, to UoM state. Performing fallback.", dataPoint.getId(),
                dataPoint.getValueText());

        return new QuantityType<>(value.toString());
    }

    public static byte[] toISM8WriteData(IDataPoint dataPoint, Command command) {
        if (command instanceof QuantityType) {
            Unit<?> expectedUnit = dataPoint.getUnit();
            if (expectedUnit != null) {
                QuantityType<?> state = Objects.requireNonNull(((QuantityType<?>) command).toUnit(expectedUnit));
                return dataPoint.createWriteData(state.doubleValue());
            }
            return dataPoint.createWriteData(command);
        } else if (command instanceof OnOffType) {
            return dataPoint.createWriteData(command);
        }
        return new byte[0];
    }
}
