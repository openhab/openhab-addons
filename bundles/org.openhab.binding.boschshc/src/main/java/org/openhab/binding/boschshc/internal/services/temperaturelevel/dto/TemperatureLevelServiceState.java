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
package org.openhab.binding.boschshc.internal.services.temperaturelevel.dto;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;

/**
 * TemperatureLevel service state.
 *
 * @author Christian Oeing - Initial contribution
 */
public class TemperatureLevelServiceState extends BoschSHCServiceState {

    public TemperatureLevelServiceState() {
        super("temperatureLevelState");
    }

    /**
     * Current temperature (in degree celsius)
     */
    private double temperature;

    /**
     * Current temperature state to set for a thing.
     *
     * @return Current temperature state to use for a thing.
     */
    public State getTemperatureState() {
        return new QuantityType<@NonNull Temperature>(this.temperature, SIUnits.CELSIUS);
    }
}
