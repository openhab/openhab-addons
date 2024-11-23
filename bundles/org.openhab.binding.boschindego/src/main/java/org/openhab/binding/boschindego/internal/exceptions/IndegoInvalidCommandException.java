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
package org.openhab.binding.boschindego.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link IndegoInvalidCommandException} is thrown when a command is rejected by the device.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoInvalidCommandException extends IndegoException {

    private static final long serialVersionUID = -2946398731437793113L;
    private int errorCode = 0;

    public IndegoInvalidCommandException(String message) {
        super(message);
    }

    public IndegoInvalidCommandException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public boolean hasErrorCode() {
        return errorCode != 0;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
