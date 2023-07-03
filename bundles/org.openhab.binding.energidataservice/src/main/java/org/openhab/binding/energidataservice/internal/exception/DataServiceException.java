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
package org.openhab.binding.energidataservice.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link DataServiceException} is a generic Energi Data Service exception thrown in case
 * of communication failure or unexpected response. It is intended to be derived by
 * specialized exceptions.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DataServiceException extends Exception {

    private static final long serialVersionUID = 1L;
    private int httpStatus = 0;

    public DataServiceException(String message) {
        super(message);
    }

    public DataServiceException(Throwable cause) {
        super(cause);
    }

    public DataServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataServiceException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
