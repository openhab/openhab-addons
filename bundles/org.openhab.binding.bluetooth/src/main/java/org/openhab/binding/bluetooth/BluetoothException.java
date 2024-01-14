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
package org.openhab.binding.bluetooth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class encompasses exceptions that occur within the bluetooth api. This can be subclassed for more specific
 * exceptions in api implementations.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class BluetoothException extends Exception {

    private static final long serialVersionUID = -2557298438595050148L;

    public BluetoothException() {
        super();
    }

    public BluetoothException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BluetoothException(String message, Throwable cause) {
        super(message, cause);
    }

    public BluetoothException(String message) {
        super(message);
    }

    public BluetoothException(Throwable cause) {
        super(cause);
    }
}
