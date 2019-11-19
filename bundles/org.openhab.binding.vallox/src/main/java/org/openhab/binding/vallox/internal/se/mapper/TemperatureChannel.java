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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.vallox.internal.se.constants.ValloxSEConstants;

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
    public TemperatureChannel(byte variable) {
        super(variable);
    }

    @Override
    public State convertToState(Byte value) {
        int index = Byte.toUnsignedInt(value);
        return new DecimalType(ValloxSEConstants.TEMPERATURE_MAPPING[index]);
    }

    @Override
    public byte convertFromState(Byte state) {
        byte value = 100;
        for (int i = 0; i < 255; i++) {
            byte valueFromTable = ValloxSEConstants.TEMPERATURE_MAPPING[i];
            if (valueFromTable >= state) {
                value = (byte) i;
                break;
            }
        }
        return value;
    }
}
