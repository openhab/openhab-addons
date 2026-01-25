/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.temperatureoffset.dto;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * DTO for temperature offset states.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class TemperatureOffsetServiceState extends BoschSHCServiceState {

    public TemperatureOffsetServiceState() {
        super("temperatureOffsetState");
    }

    public double offset;
    public double stepSize;
    public double minOffset;
    public double maxOffset;

    public QuantityType<Temperature> getOffsetState() {
        return new QuantityType<>(offset, SIUnits.CELSIUS);
    }
}
