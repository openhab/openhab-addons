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
package org.openhab.binding.sinope.internal.config;

/**
 * Holds Config for the Sinope Gateway
 *
 * @author Pascal Larin - Initial contribution
 *
 */
public class SinopeConfig {
    /**
     * Hostname of the Sinope Gateway
     */
    public String hostname;
    /**
     * ip port
     */
    public Integer port;
    /**
     * Gateway ID
     */
    public String gatewayId;
    /**
     * API Key returned by the Gateway
     */
    public String apiKey;
    /**
     * The number of seconds between fetches from the sinope deivces
     */
    public Integer refresh;

    /**
     * Convert Hex Config String to byte
     */
    public static byte[] convert(String value) {
        if (value == null) {
            return null;
        }
        String _value = value;

        _value = _value.replace("-", "");
        _value = _value.replace("0x", "");
        _value = _value.replace(" ", "");

        if (_value.length() == 0) {
            return null;
        }

        if (_value.length() % 2 == 0 && _value.length() > 1) {
            byte[] b = new byte[_value.length() / 2];

            for (int i = 0; i < _value.length(); i = i + 2) {
                b[i / 2] = (byte) Integer.parseInt(_value.substring(i, i + 2), 16);
            }
            return b;
        } else {
            return null;
        }
    }
}
