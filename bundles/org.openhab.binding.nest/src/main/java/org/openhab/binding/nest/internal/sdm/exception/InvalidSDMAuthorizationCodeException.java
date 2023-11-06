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
package org.openhab.binding.nest.internal.sdm.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An authorization code is invalid and cannot be used to obtain the OAuth 2.0 tokens used with the SDM API.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class InvalidSDMAuthorizationCodeException extends Exception {

    private static final long serialVersionUID = -8900246112957957403L;

    public InvalidSDMAuthorizationCodeException(Exception cause) {
        super(cause);
    }

    public InvalidSDMAuthorizationCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSDMAuthorizationCodeException(String message) {
        super(message);
    }
}
