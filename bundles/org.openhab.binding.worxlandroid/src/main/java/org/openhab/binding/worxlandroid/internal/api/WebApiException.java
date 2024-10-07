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
package org.openhab.binding.worxlandroid.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WebApiException} is a class for handling the Worx Landroid API exceptions
 *
 * @author Nils Billing - Initial contribution
 */
@NonNullByDefault
public class WebApiException extends Exception {
    private static final long serialVersionUID = 1L;
    private static final int UNKNOWN = 0;

    private final int errorCode;

    public WebApiException(int errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    public WebApiException(String errorMsg, Throwable cause) {
        super(errorMsg);
        this.errorCode = UNKNOWN;
    }

    public WebApiException(String errorMsg) {
        super(errorMsg);
        this.errorCode = UNKNOWN;
    }

    public WebApiException(Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorCode = UNKNOWN;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
