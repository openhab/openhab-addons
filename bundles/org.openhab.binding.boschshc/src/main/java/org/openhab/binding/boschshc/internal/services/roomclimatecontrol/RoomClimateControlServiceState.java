/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.roomclimatecontrol;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.boschshc.internal.services.BoschSHCServiceState;

import tec.uom.se.unit.Units;

/**
 * State for {@link RoomClimateControlService} to get and set the desired temperature of a room.
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class RoomClimateControlServiceState extends BoschSHCServiceState {

    private static final String Type = "climateControlState";

    public RoomClimateControlServiceState() {
        super(Type);
    }

    /**
     * Constructor.
     * 
     * @param setpointTemperature Desired temperature (in degree celsius).
     */
    public RoomClimateControlServiceState(double setpointTemperature) {
        super(Type);
        this.setpointTemperature = setpointTemperature;
    }

    /**
     * Desired temperature (in degree celsius).
     * 
     * @apiNote Min: 5.0, Max: 30.0.
     * @apiNote Can be set in 0.5 steps.
     */
    private double setpointTemperature;

    /**
     * Desired temperature state to set for a thing.
     * 
     * @return Desired temperature state to set for a thing.
     */
    public State getSetpointTemperatureState() {
        return new QuantityType<Temperature>(this.setpointTemperature, Units.CELSIUS);
    }
}
