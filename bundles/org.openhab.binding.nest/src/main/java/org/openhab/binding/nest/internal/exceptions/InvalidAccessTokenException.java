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
package org.openhab.binding.nest.internal.exceptions;

/**
 * Will be thrown when there is no valid access token and it was not possible to refresh it
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Martin van Wingerden - Added more centralized handling of invalid access tokens
 */
@SuppressWarnings("serial")
public class InvalidAccessTokenException extends Exception {
    public InvalidAccessTokenException(Exception cause) {
        super(cause);
    }

    public InvalidAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAccessTokenException(String message) {
        super(message);
    }
}
