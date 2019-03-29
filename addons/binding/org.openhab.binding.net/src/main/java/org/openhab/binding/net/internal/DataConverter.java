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
package org.openhab.binding.net.internal;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.smarthome.core.util.HexUtils;

public class DataConverter {

    public enum CharSet {
        ASCII(),
        BINARY(),
        HEXASTRING(),
        UTF8();

        public static CharSet getConvertTo(String convertTo) throws IllegalArgumentException {

            try {
                return CharSet.valueOf(convertTo);
            } catch (IllegalArgumentException e) {
                String description = String.format("Illegal value '%s'", convertTo);
                throw new IllegalArgumentException(description);
            }
        }
    }

    private CharSet convertTo;

    public DataConverter(String convertTo) throws IllegalArgumentException {
        this.convertTo = CharSet.getConvertTo(convertTo.toUpperCase());
    }

    public DataConverter(CharSet convertTo) {
        this.convertTo = convertTo;
    }

    public Object convertBytes(byte[] data) throws UnsupportedEncodingException {
        Object retval = "";
        switch (convertTo) {
            case ASCII:
                retval = new String(data, StandardCharsets.US_ASCII.name());
                break;
            case BINARY:
                retval = data;
                break;
            case HEXASTRING:
                retval = HexUtils.bytesToHex(data);
                break;
            case UTF8:
                retval = new String(data, StandardCharsets.UTF_8.name());
                break;
        }
        return retval;
    }
}
