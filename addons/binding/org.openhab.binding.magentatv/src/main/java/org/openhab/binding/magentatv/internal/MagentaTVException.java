/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.magentatv.internal;

/**
 * The {@link MagentaTVException} class a binding specific exception class.
 *
 * @author Markus Michels - Initial contribution (markus7017)
 */
public class MagentaTVException extends Exception {

    private static final long serialVersionUID = 6214176461907613559L;

    /**
     * Constructor. Creates new instance of LIRCResponseException
     */
    public MagentaTVException() {
        super();
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param message the detail message.
     */
    public MagentaTVException(String message) {
        super(message);
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param cause the cause. (A null value is permitted, and indicates that the
     *                  cause is nonexistent or unknown.)
     */
    public MagentaTVException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor. Creates new instance of LIRCResponseException
     *
     * @param message the detail message.
     * @param cause   the cause. (A null value is permitted, and indicates that the
     *                    cause is nonexistent or unknown.)
     */
    public MagentaTVException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return getMessage() + " (" + getClass() + ")";
    }
}
