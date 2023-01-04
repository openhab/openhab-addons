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
package org.openhab.binding.fronius.internal.api;

import javax.measure.Unit;

import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link InverterRealtimeResponse} is responsible for storing
 * a value
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Jimmy Tanagra - Add conversion to QuantityType
 */
public class ValueUnit {

    @SerializedName("Value")
    private double value;
    @SerializedName("Unit")
    private String unit = "";

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return this.unit == null ? "" : this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public QuantityType<?> asQuantityType() {
        Unit<?> unit = UnitUtils.parseUnit(getUnit());
        if (unit == null) {
            final Logger logger = LoggerFactory.getLogger(ValueUnit.class);
            logger.debug("The unit for ValueUnit ({})/({}) cannot be parsed", value, this.unit);
            unit = QuantityType.ONE.getUnit();
        }
        return new QuantityType<>(value, unit);
    }
}
