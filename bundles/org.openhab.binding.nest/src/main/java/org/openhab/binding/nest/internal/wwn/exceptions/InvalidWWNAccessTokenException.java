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
package org.openhab.binding.nest.internal.wwn.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Will be thrown when there is no valid access token and it was not possible to refresh it
 *
 * @author Martin van Wingerden - Initial contribution
 * @author Martin van Wingerden - Added more centralized handling of invalid access tokens
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class InvalidWWNAccessTokenException extends Exception {
    public InvalidWWNAccessTokenException(Exception cause) {
        super(cause);
    }

    public InvalidWWNAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWWNAccessTokenException(String message) {
        super(message);
    }
}
