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
package org.openhab.binding.vallox.internal.se.mapper;

import java.util.Arrays;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.vallox.internal.se.ValloxSEConstants;

/**
 * Class for temperature channels.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class TemperatureChannel extends ValloxChannel {

    /**
     * Create new instance.
     *
     * @param variable channel as byte
     */
    public TemperatureChannel(int variable) {
        super(variable);
    }

    @Override
    public State convertToState(byte value) {
        int index = Byte.toUnsignedInt(value);
        return new QuantityType<Temperature>(ValloxSEConstants.TEMPERATURE_MAPPING[index], SIUnits.CELSIUS);
    }

    @Override
    public byte convertFromState(byte state) {
        return (byte) Arrays.binarySearch(ValloxSEConstants.TEMPERATURE_MAPPING, state);
    }
}
