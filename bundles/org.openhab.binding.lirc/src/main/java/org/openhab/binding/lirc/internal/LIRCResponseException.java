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
package org.openhab.binding.lirc.internal;

/**
 * Exceptions thrown from the serial interface.
 *
 * @author Andrew Nagle - Initial contributor
 */
public class LIRCResponseException extends Exception {

    private static final long serialVersionUID = 6214176461907613559L;

    /**
     * Constructor. Creates new instance of LIRCResponseException
     */
    public LIRCResponseException() {
        super();
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param message the detail message.
     */
    public LIRCResponseException(String message) {
        super(message);
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LIRCResponseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param message the detail message.
     * @param cause the cause. (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LIRCResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
