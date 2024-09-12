/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.roomclimatecontrol.dto;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlService;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;

/**
 * State for {@link RoomClimateControlService} to get and set the desired temperature of a room.
 *
 * @author Christian Oeing - Initial contribution
 */
public class RoomClimateControlServiceState extends BoschSHCServiceState {

    private static final String CLIMATE_CONTROL_STATE_TYPE = "climateControlState";

    public RoomClimateControlServiceState() {
        super(CLIMATE_CONTROL_STATE_TYPE);
    }

    /**
     * Constructor.
     *
     * @param setpointTemperature Desired temperature (in degree celsius).
     */
    public RoomClimateControlServiceState(double setpointTemperature) {
        super(CLIMATE_CONTROL_STATE_TYPE);
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
        return new QuantityType<@NonNull Temperature>(this.setpointTemperature, SIUnits.CELSIUS);
    }
}
