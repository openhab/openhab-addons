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
package org.openhab.binding.teleinfo.internal.reader.io.serialport;

/**
 * The {@link ConvertionException} class defines a conversion exception.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class ConvertionException extends Exception {

    private static final long serialVersionUID = -1109821041874271681L;
    private static final String ERROR_MESSAGE = "Unable to convert '%1$s' value";

    private String valueToConvert;

    public ConvertionException(String valueToConvert) {
        this(valueToConvert, null);
    }

    public ConvertionException(String valueToConvert, Throwable cause) {
        super(String.format(ERROR_MESSAGE, valueToConvert), cause);
    }

    public String getValueToConvert() {
        return valueToConvert;
    }
}
