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
package org.openhab.binding.pjlinkdevice.internal.device.command.power;

import java.util.HashMap;

import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class PowerQueryResponse extends PrefixedResponse {

    public enum PowerQueryResponseValue {
        STAND_BY,
        POWER_ON,
        COOLING_DOWN,
        WARMING_UP;

        public String getText() {
            final HashMap<PowerQueryResponseValue, String> texts = new HashMap<PowerQueryResponseValue, String>();
            texts.put(STAND_BY, "Stand-by");
            texts.put(POWER_ON, "Power on");
            texts.put(COOLING_DOWN, "Cooling down");
            texts.put(WARMING_UP, "Warming up");
            return texts.get(this);
        }

        public static PowerQueryResponseValue parseString(String code) throws ResponseException {
            final HashMap<String, PowerQueryResponseValue> codes = new HashMap<String, PowerQueryResponseValue>();
            codes.put("0", STAND_BY);
            codes.put("1", POWER_ON);
            codes.put("2", COOLING_DOWN);
            codes.put("3", WARMING_UP);

            PowerQueryResponseValue result = codes.get(code);
            if (result == null) {
                throw new ResponseException("Cannot understand status: " + code);
            }

            return result;
        }

    }

    private PowerQueryResponseValue result = null;

    public PowerQueryResponse() {
        super("POWR=");
    }

    public PowerQueryResponseValue getResult() {
        return result;
    }

    @Override
    protected void parse0(String responseWithoutPrefix) throws ResponseException {
        this.result = PowerQueryResponseValue.parseString(responseWithoutPrefix);
    }

}
