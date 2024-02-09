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
package org.openhab.binding.vesync.internal.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AuthenticationException} is thrown if the authentication/login process is unsuccessful.
 *
 * @author David Godyear - Initial contribution
 */
@NonNullByDefault
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = -7786425895604150557L;

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(final String message) {
        super(message);
    }

    public AuthenticationException(final Throwable cause) {
        super(cause);
    }

    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
