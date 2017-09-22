/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.co2signal.internal.json;

/**
 * The {@link CO2SignalJsonData} is responsible for storing
 * the "data" node from the co2signal.com JSON response
 *
 * @author Jens Viebig - Initial contribution
 */
public class CO2SignalJsonData {

    private double carbonIntensity;
    private double fossilFuelPercentage;

    public CO2SignalJsonData() {
    }

    public double getCarbonIntensity() {
        return carbonIntensity;
    }

    public double getFossilFuelPercentage() {
        return fossilFuelPercentage;
    }
}
