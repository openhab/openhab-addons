/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.ec3k;

import org.openhab.binding.jeelink.internal.RollingReadingAverage;

/**
 * Computes a rolling average of readings.
 *
 * @author Volker Bier - Initial contribution
 */
public class Ec3kRollingReadingAverage extends RollingReadingAverage<Ec3kReading> {
    public Ec3kRollingReadingAverage(int bufferSize) {
        super(new Ec3kReading[bufferSize]);
    }

    @Override
    protected Ec3kReading add(Ec3kReading value1, Ec3kReading value2) {
        if (value2 != null) {
            return new Ec3kReading(value2.getSensorId(), value1.getCurrentWatt() + value2.getCurrentWatt(),
                    value2.getMaxWatt(), value2.getConsumptionTotal(), value2.getApplianceTime(),
                    value2.getSensorTime(), value2.getResets());
        }

        return new Ec3kReading(value1.getSensorId(), value1.getCurrentWatt(), value1.getMaxWatt(),
                value1.getConsumptionTotal(), value1.getApplianceTime(), value1.getSensorTime(), value1.getResets());
    }

    @Override
    protected Ec3kReading substract(Ec3kReading from, Ec3kReading value) {
        float newCurrWatt = from.getCurrentWatt();

        if (value != null) {
            newCurrWatt -= value.getCurrentWatt();
        }

        return new Ec3kReading(from.getSensorId(), newCurrWatt, from.getMaxWatt(), from.getConsumptionTotal(),
                from.getApplianceTime(), from.getSensorTime(), from.getResets());
    }

    @Override
    protected Ec3kReading divide(Ec3kReading value, int number) {
        return new Ec3kReading(value.getSensorId(), value.getCurrentWatt() / number, value.getMaxWatt(),
                value.getConsumptionTotal(), value.getApplianceTime(), value.getSensorTime(), value.getResets());
    }
}
