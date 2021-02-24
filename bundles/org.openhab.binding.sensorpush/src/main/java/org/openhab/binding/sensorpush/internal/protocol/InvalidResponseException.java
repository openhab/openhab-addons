/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.sensorpush.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals than an invalid API response was received
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class InvalidResponseException extends Exception {

    private static final long serialVersionUID = -1025144225509161178L;

    /**
     * Constructs a ParseException with the specified detail message.
     *
     * @param s the detail message
     */
    public InvalidResponseException(String s) {
        super(s);
    }
}
