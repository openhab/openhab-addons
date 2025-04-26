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
package org.openhab.binding.wemo.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link WemoException} is thrown in case of an error communicating
 * with a WeMo device.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoException extends Exception {

    private static final long serialVersionUID = 1L;
    private int httpStatus = 0;

    public WemoException(String message) {
        super(message);
    }

    public WemoException(Throwable cause) {
        super(cause);
    }

    public WemoException(String message, Throwable cause) {
        super(message, cause);
    }

    public WemoException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
