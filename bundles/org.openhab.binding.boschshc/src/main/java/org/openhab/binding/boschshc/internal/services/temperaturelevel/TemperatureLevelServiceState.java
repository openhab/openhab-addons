/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.temperaturelevel;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

import tec.uom.se.unit.Units;

/** 
 * TemperatureLevel service state.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class TemperatureLevelServiceState extends BoschSHCServiceState {

    public TemperatureLevelServiceState() {
        super("temperatureLevelState");
    }

    /**
     * Current temperature (in degree celsius)
     */
    public double temperature;

    /**
     * Current temperature state to set for a thing.
     * 
     * @return Current temperature state to use for a thing.
     */
    public State getTemperatureState() {
        return new QuantityType<Temperature>(this.temperature, Units.CELSIUS);
    }
}
