/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.client.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Invalid oAuth token exception
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class InvalidTokenException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
}
