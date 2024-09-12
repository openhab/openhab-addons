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
package org.openhab.binding.wlanthermo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WlanThermoInputException} is thrown if input is invalid or null
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoInputException extends WlanThermoException {

    static final long serialVersionUID = 1L;
    public static final String INVALID_INPUT_EXCEPTION = "Input Data is invalid!";

    public WlanThermoInputException() {
        super(INVALID_INPUT_EXCEPTION);
    }

    public WlanThermoInputException(Throwable cause) {
        super(INVALID_INPUT_EXCEPTION, cause);
    }
}
