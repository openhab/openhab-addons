/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HubException} is a generic custom exception for the HD PowerView Hub
 * with the intent of being derived into specific exception classes.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class HubException extends Exception {

    private static final long serialVersionUID = 4052375893291196875L;

    public HubException(String message) {
        super(message);
    }

    public HubException(String message, Throwable cause) {
        super(message, cause);
    }
}
