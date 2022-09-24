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
 * The {@link EnergyMeterData} class holds information for the different channles defined by the SMA Energy Meter
 *
 * @author Lars Repenning - Initial contribution
 */
public class EnergyMeterData {
    public int channelNo;
    public ValueType datatype;
    public int rawValue;
    public EnergyMeterChannel energyMeterChannel;

    public ValueType getDatatype() {
        return datatype;
    }

    public EnergyMeterChannel getEnergyMeterValue() {
        return energyMeterChannel;
    }

    public MeasuredUnit getUnit() {
        if (datatype == ValueType.CURRENT) {
            return energyMeterChannel.getCurrentUnitOfMeasurement();
        } else {
            return energyMeterChannel.getTotalUnitOfMeasurement();
        }
    }

    public double getValue() {
        return rawValue / (double) getUnit().getFactor();
    }

    public String getOhChannelName() {
        String prefix = energyMeterChannel.getPhase() != 0 ? ("L" + energyMeterChannel.getPhase() + "_") : "";
        return prefix + getDatatype() + "_" + energyMeterChannel.getName();
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnergyMeterData) {
            return channelNo == ((EnergyMeterData) obj).channelNo;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(channelNo);
    }
}
