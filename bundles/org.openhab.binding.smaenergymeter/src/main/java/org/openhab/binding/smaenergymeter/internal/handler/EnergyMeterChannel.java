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

import java.util.Objects;

/**
 * The {@link EnergyMeterChannel} class holds information for the
 *
 * @author Lars Repenning - Initial contribution
 */
public class EnergyMeterChannel {
    private int channel;
    private MeasuredUnit currentUnitOfMeasurement;
    private MeasuredUnit totalUnitOfMeasurement;
    private int phase;

    private String name;

    public EnergyMeterChannel(int channel, String name, MeasuredUnit currentUnitOfMeasurement,
            MeasuredUnit totalUnitOfMeasurement, int phase) {
        this.channel = channel;
        this.currentUnitOfMeasurement = currentUnitOfMeasurement;
        this.totalUnitOfMeasurement = totalUnitOfMeasurement;
        this.phase = phase;
        this.name = name;
    }

    public int getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public MeasuredUnit getCurrentUnitOfMeasurement() {
        return currentUnitOfMeasurement;
    }

    public MeasuredUnit getTotalUnitOfMeasurement() {
        return totalUnitOfMeasurement;
    }

    public int getPhase() {
        return phase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EnergyMeterChannel that = (EnergyMeterChannel) o;
        return channel == that.channel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }
}
