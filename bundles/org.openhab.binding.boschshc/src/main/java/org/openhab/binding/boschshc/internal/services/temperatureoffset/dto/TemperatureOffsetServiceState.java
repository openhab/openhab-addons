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

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * DTO for temperature offset states.
 * 
 * @author David Pace - Initial contribution
 *
 */
public class TemperatureOffsetServiceState extends BoschSHCServiceState {

    public TemperatureOffsetServiceState() {
        super("temperatureOffsetState");
    }

    public double offset;

    // Note: the following members are Double objects intentionally in order to make them nullable.
    // Those members must NOT be serialized for outgoing status updates, otherwise they will be rejected
    // by the Smart Home Controller, but they are needed for incoming service state updates
    public Double stepSize;
    public Double minOffset;
    public Double maxOffset;

    public QuantityType<Temperature> getOffsetState() {
        return new QuantityType<>(offset, SIUnits.CELSIUS);
    }
}
