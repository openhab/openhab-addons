/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smaenergymeter.internal.handler;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * The {@link Channel} class holds information for the different channles defined by the SMA Energy Meter
 *
 * @author Lars Repenning - Initial contribution
 */
public class Channel {
    public int channelNo;
    public ValueType datatype;
    public int rawValue;
    public EnergyMeterValue energyMeterValue;

    public ValueType getDatatype() {
        return datatype;
    }

    public EnergyMeterValue getEnergyMeterValue() {
        return energyMeterValue;
    }

    public MeasuredUnit getUnit() {
        if (datatype == ValueType.CURRENT) {
            return energyMeterValue.getCurrentUnitOfMeasurement();
        } else {
            return energyMeterValue.getTotalUnitOfMeasurement();
        }
    }

    public double getValue() {
        return rawValue / (double) getUnit().getFactor();
    }

    public String getOhChannelName() {
        String prefix = energyMeterValue.getPhase() != 0 ? ("L" + energyMeterValue.getPhase() + "_") : "";
        return prefix + getDatatype() + "_" + energyMeterValue.getName();
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Channel) {
            return channelNo == ((Channel) obj).channelNo;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(channelNo);
    }
}
