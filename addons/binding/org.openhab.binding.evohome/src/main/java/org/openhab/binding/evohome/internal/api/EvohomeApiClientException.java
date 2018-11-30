/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api;

/**
 * Exception for errors from the API Client.
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class EvohomeApiClientException extends Exception {

    public EvohomeApiClientException() {
    }

    public EvohomeApiClientException(String message) {
        super(message);
    }

    public EvohomeApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
