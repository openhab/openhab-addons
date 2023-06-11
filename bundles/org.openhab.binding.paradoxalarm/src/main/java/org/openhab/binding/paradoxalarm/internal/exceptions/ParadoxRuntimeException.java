/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.exceptions;

/**
 * The {@link ParadoxRuntimeException} Used for Paradox binding specific runtime exceptions.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ParadoxRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -4656474289606169766L;

    public ParadoxRuntimeException(String message) {
        super(message);
    }

    public ParadoxRuntimeException() {
        super();
    }

    public ParadoxRuntimeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ParadoxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParadoxRuntimeException(Throwable cause) {
        this("This is a Paradox Binding wrapper of RuntimeException. For detailed error message, see the original exception. Short message: "
                + cause.getMessage(), cause);
    }
}
