/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown when a request to the Matter server fails.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterRequestException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String errorMessage;
    private final @Nullable MatterErrorCode errorCode;

    public MatterRequestException(String message, @Nullable MatterErrorCode errorCode) {
        super(message);
        this.errorMessage = message;
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public @Nullable MatterErrorCode getErrorCode() {
        return errorCode;
    }
}
