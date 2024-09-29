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
 * {@link IndegoException} is a generic Indego exception thrown in case
 * of communication failure or unexpected response. It is intended to
 * be derived by specialized exceptions.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoException extends Exception {

    private static final long serialVersionUID = 6673869982385647268L;

    public IndegoException(String message) {
        super(message);
    }

    public IndegoException(Throwable cause) {
        super(cause);
    }

    public IndegoException(String message, Throwable cause) {
        super(message, cause);
    }
}
