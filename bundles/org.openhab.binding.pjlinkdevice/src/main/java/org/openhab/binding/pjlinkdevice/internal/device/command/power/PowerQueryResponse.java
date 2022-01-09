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
package org.openhab.binding.pjlinkdevice.internal.device.command.power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * The response part of {@link PowerQueryCommand}
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PowerQueryResponse extends PrefixedResponse<PowerQueryResponse.PowerQueryResponseValue> {
    public enum PowerQueryResponseValue {
        STAND_BY("Stand-by", "0"),
        POWER_ON("Power on", "1"),
        COOLING_DOWN("Cooling down", "2"),
        WARMING_UP("Warming up", "3");

        private String text;
        private String code;

        private PowerQueryResponseValue(String text, String code) {
            this.text = text;
            this.code = code;
        }

        public String getText() {
            return this.text;
        }

        public static PowerQueryResponseValue parseString(String code) throws ResponseException {
            for (PowerQueryResponseValue result : PowerQueryResponseValue.values()) {
                if (result.code.equals(code)) {
                    return result;
                }
            }

            throw new ResponseException("Cannot understand power status: " + code);
        }
    }

    public PowerQueryResponse(String response) throws ResponseException {
        super("POWR=", response);
    }

    @Override
    protected PowerQueryResponseValue parseResponseWithoutPrefix(String responseWithoutPrefix)
            throws ResponseException {
        return PowerQueryResponseValue.parseString(responseWithoutPrefix);
    }
}
