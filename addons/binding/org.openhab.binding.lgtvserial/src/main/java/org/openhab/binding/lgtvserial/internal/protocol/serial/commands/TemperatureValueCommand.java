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
package org.openhab.binding.lgtvserial.internal.protocol.serial.commands;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lgtvserial.internal.protocol.serial.LGSerialResponse;

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

    public static class QuantityResponse implements LGSerialResponse {

        private int setId;

        private boolean success;

        private State state;

        public QuantityResponse(int setId, boolean success, QuantityType<?> state) {
            this.setId = setId;
            this.success = success;
            this.state = state;
        }

        @Override
        public int getSetID() {
            return setId;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public boolean isSuccess() {
            return success;
        }

    }
}
