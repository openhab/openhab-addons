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
package org.openhab.binding.http.internal.http;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HttpAuthException} is an exception after authorization errors
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpAuthException extends Exception {
    private static final long serialVersionUID = 1L;

    public HttpAuthException() {
        super();
    }

    public HttpAuthException(String message) {
        super(message);
    }
}
