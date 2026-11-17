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
package org.openhab.binding.fronius.internal.api.dto;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ValueUnit} is responsible for storing a value.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Jimmy Tanagra - Add conversion to QuantityType
 */
@NonNullByDefault
public class ValueUnit {
    @SerializedName("Value")
    private @Nullable Double value;
    @SerializedName("Unit")
    private @Nullable String unit = "";

    public double getValue() {
        Double value = this.value;
        return value == null ? 0 : value;
    }

    /**
     * @return whether the inverter actually provided a value; some values are always null on some models, e.g. the day
     *         and year energy on GEN24/Tauro/Verto. Without this check, the null cannot be told apart from a real zero,
     *         as Gson maps JSON null to 0 for primitives.
     */
    public boolean hasValue() {
        return value != null;
    }

    public @Nullable String getUnit() {
        return unit;
    }

    public QuantityType<?> asQuantityType() {
        Unit<?> unit = UnitUtils.parseUnit(getUnit());
        if (unit == null) {
            final Logger logger = LoggerFactory.getLogger(ValueUnit.class);
            logger.debug("The unit for ValueUnit ({})/({}) cannot be parsed", value, this.unit);
            unit = QuantityType.ONE.getUnit();
        }
        return new QuantityType<>(getValue(), unit);
    }
}
