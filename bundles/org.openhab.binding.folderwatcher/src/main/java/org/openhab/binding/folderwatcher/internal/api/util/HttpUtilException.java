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
package org.openhab.binding.folderwatcher.internal.api.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HttpUtilException} signal;s there was a problem with contacting the API
 *
 * @author Leo Siepel - initial contribution
 * 
 */
@NonNullByDefault
public class HttpUtilException extends Exception {

    private static final long serialVersionUID = 1L;

    public HttpUtilException(String message) {
        super(message);
    }

    public HttpUtilException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpUtilException(Throwable cause) {
        super(cause);
    }
}
