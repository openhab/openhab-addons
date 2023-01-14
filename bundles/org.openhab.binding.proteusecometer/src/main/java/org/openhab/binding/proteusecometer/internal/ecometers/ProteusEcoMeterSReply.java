/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.proteusecometer.internal.ecometers;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The reply of Proteus EcoMeter S
 *
 * @author Matthias Herrmann - Initial contribution
 *
 */
@NonNullByDefault
public class ProteusEcoMeterSReply {
    public final double tempInFahrenheit;
    public final int sensorLevelInCm;
    public final int usableLevelInLiter;
    public final int totalCapacityInLiter;

    public ProteusEcoMeterSReply(final double tempInFahrenheit, final int sensorLevelInCm, final int usableLevelInLiter,
            final int totalCapacityInLiter) {
        this.tempInFahrenheit = tempInFahrenheit;
        this.sensorLevelInCm = sensorLevelInCm;
        this.usableLevelInLiter = usableLevelInLiter;
        this.totalCapacityInLiter = totalCapacityInLiter;
    }

    @Override
    public String toString() {
        return "ProteusEcoMeterSReply [sensorLevelInCm=" + sensorLevelInCm + ", tempInFahrenheit=" + tempInFahrenheit
                + ", totalCapacityInLiter=" + totalCapacityInLiter + ", usableLevelInLiter=" + usableLevelInLiter + "]";
    }
}
