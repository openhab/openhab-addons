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
package org.openhab.binding.freeathome.internal.util;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FreeAtHomeGeneralException} is responsible for handling general exceptions in the free@home binding
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeGeneralException extends Exception {
    private static final long serialVersionUID = -835448863173642860L;
    private String errorMessage = "Unknown_Exception";
    private int errorCode = 0;

    public FreeAtHomeGeneralException(int errorCode, String message) {
        super(message);

        this.errorMessage = message;
        this.errorCode = errorCode;
    }

    public @Nullable String getMessage() {
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
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        FreeAtHomeGeneralException other = (FreeAtHomeGeneralException) obj;

        return errorCode == other.errorCode && Objects.equals(errorMessage, other.errorMessage);
    }
}
