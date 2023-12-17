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

package org.openhab.binding.freeathomesystem.internal.util;

import java.util.Objects;

/**
 * The {@link FreeAtHomeHttpCommunicationException} is responsible for handling the communication exception to the SysAp
 * via HTTP link
 *
 * @author Andras Uhrin - Initial contribution
 * @param <FreeAtHomeHttpCommunicationException>
 *
 */
public class FreeAtHomeHttpCommunicationException extends Exception {
    private static final long serialVersionUID = -817364286035448863L;
    private String errorMessage = "Unknown_Exception";
    private int errorCode;

    public FreeAtHomeHttpCommunicationException(int errorCode, String message) {
        super(message);

        this.errorMessage = message;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return this.errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String toString() {
        return "FreeAtHomeHttpCommunicationException [errorMessage=" + errorMessage + ", errorCode=" + errorCode + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, errorMessage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        FreeAtHomeHttpCommunicationException other = (FreeAtHomeHttpCommunicationException) obj;

        return errorCode == other.errorCode && Objects.equals(errorMessage, other.errorMessage);
    }
}
