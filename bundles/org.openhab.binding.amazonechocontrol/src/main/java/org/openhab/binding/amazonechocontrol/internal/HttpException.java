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
package org.openhab.binding.amazonechocontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HttpException} is used for http error codes
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    int code;

    public int getCode() {
        return code;
    }

    public HttpException(int code, String message) {
        super(message);
        this.code = code;
    }
}
