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
 * The {@link WlanThermoException} is thrown if an exception in WlanThermoBinding occurs.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoException extends Exception {

    static final long serialVersionUID = 1L;

    public WlanThermoException(String reason) {
        super(reason, null);
    }

    public WlanThermoException(String message, Throwable cause) {
        super(message, cause);
    }

    public WlanThermoException(Throwable cause) {
        super(cause);
    }

    public WlanThermoException() {
    }
}
