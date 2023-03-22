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
package org.openhab.binding.boschshc.internal.services.humiditylevel.dto;

import javax.measure.quantity.Dimensionless;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * State for {@link HumidityLevelService} to get and set the desired temperature of a room.
 *
 * @author Christian Oeing - Initial contribution
 */
public class HumidityLevelServiceState extends BoschSHCServiceState {

    public HumidityLevelServiceState() {
        super("humidityLevelState");
    }

    /**
     * Current measured humidity.
     */
    public double humidity;

    public State getHumidityState() {
        return new QuantityType<Dimensionless>(this.humidity, Units.PERCENT);
    }
}
