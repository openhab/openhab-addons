/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import java.util.Optional;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.openhab.binding.bticinosmarther.internal.api.dto.Enums.MeasureUnit;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherIllegalPropertyValueException;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * The {@code Measure} class defines the dto for Smarther API measure object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Measure {

    @SerializedName("timeStamp")
    private String timestamp;
    private String value;
    private String unit;

    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the value of this measure.
     *
     * @return a string containing the measure value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the measure unit of this measure.
     *
     * @return a string containing the measure unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the measure unit of this measure.
     *
     * @return a {@link MeasureUnit} object representing the measure unit
     *
     * @throws {@link SmartherIllegalPropertyValueException}
     *             if the measure internal raw unit cannot be mapped to any valid measure unit
     */
    public MeasureUnit getMeasureUnit() throws SmartherIllegalPropertyValueException {
        return MeasureUnit.fromValue(unit);
    }

    /**
     * Returns the value and measure unit of this measure as a combined {@link State} object.
     *
     * @return the value and measure unit
     *
     * @throws {@link SmartherIllegalPropertyValueException}
     *             if the measure internal raw unit cannot be mapped to any valid measure unit
     */
    public State toState() throws SmartherIllegalPropertyValueException {
        State state = UnDefType.UNDEF;
        final Optional<Double> optValue = (StringUtil.isBlank(value)) ? Optional.empty()
                : Optional.of(Double.parseDouble(value));

        switch (MeasureUnit.fromValue(unit)) {
            case CELSIUS:
                state = optValue.<State> map(t -> new QuantityType<Temperature>(new DecimalType(t), SIUnits.CELSIUS))
                        .orElse(UnDefType.UNDEF);
                break;
            case FAHRENHEIT:
                state = optValue
                        .<State> map(t -> new QuantityType<Temperature>(new DecimalType(t), ImperialUnits.FAHRENHEIT))
                        .orElse(UnDefType.UNDEF);
                break;
            case PERCENTAGE:
                state = optValue.<State> map(t -> new QuantityType<Dimensionless>(new DecimalType(t), Units.PERCENT))
                        .orElse(UnDefType.UNDEF);
                break;
            case DIMENSIONLESS:
                state = optValue.<State> map(t -> new DecimalType(t)).orElse(UnDefType.UNDEF);
        }

        return state;
    }

    @Override
    public String toString() {
        return (StringUtil.isBlank(timestamp)) ? String.format("value=%s, unit=%s", value, unit)
                : String.format("value=%s, unit=%s, timestamp=%s", value, unit, timestamp);
    }
}
