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
package org.openhab.binding.nest.internal.sdm.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The OAuth 2.0 access token used with the SDM REST API is invalid and could not be refreshed.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class InvalidSDMAccessTokenException extends Exception {

    private static final long serialVersionUID = 6149230876422099759L;

    public InvalidSDMAccessTokenException(Exception cause) {
        super(cause);
    }

    public InvalidSDMAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSDMAccessTokenException(String message) {
        super(message);
    }
}
