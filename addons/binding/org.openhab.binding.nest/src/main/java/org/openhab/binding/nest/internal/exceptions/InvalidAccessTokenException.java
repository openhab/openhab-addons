/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.exceptions;

/**
 * Will be thrown when there is no valid access token and it was not possible to refresh it
 *
 * @author Martin van Wingerden - Added more centralized handling of invalid access tokens
 */
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
