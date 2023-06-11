/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;
import org.openhab.binding.lgtvserial.internal.protocol.serial.responses.QuantityResponse;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;

/**
 * This class handles the temperature value D/N command.
 *
 * @author Richard Lavoie - Initial contribution
 *
 */
public class TemperatureValueCommand extends BaseDecimalCommand {

    protected TemperatureValueCommand(int setId) {
        super('d', 'n', setId, false);
    }

    @Override
    protected LGSerialResponse createResponse(int set, boolean success, String data) {
        return new QuantityResponse(set, success, new QuantityType<>(Integer.parseInt(data, 16), SIUnits.CELSIUS));
    }
}
