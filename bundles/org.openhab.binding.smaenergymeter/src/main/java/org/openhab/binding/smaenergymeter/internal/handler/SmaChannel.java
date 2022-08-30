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
package org.openhab.binding.smaenergymeter.internal.handler;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The {@link SmaChannel} class holds information for the different SMA channels
 *
 * @author Lars Repenning - Initial contribution
 */
public class SmaChannel {
    public int channelNo;
    public Type datatype;
    public int rawValue;
    public EnergyMeterValue measuredUnit;

    public Type getDatatype() {
        return datatype;
    }

    public EnergyMeterValue getMeasuredUnit() {
        return measuredUnit;
    }

    public Unit getUnit() {
        if (datatype == Type.CURRENT) {
            return measuredUnit.getCurrentUnitOfMeasurement();
        } else {
            return measuredUnit.getTotalUnitOfMeasurement();
        }
    }

    public double getValue() {
        return rawValue / (double) getUnit().getScaling();
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SmaChannel) {
            return channelNo == ((SmaChannel) obj).channelNo;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(channelNo);
    }
}
