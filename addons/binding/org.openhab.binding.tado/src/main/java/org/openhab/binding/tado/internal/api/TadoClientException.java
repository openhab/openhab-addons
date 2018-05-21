/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.api;

/**
 * Custom exception for logical errors on the API.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoClientException extends Exception {
    public TadoClientException() {
    }

    public TadoClientException(String message) {
        super(message);
    }

    public TadoClientException(Throwable cause) {
        super(cause);
    }

    public TadoClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public TadoClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
