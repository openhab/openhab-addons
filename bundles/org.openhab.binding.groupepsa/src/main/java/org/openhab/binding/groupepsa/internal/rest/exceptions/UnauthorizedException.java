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
package org.openhab.binding.groupepsa.internal.rest.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class UnauthorizedException extends GroupePSACommunicationException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(int statusCode, Exception e) {
        super(statusCode, e);
    }

    public UnauthorizedException(int statusCode) {
        super(statusCode);
    }

    public UnauthorizedException(int statusCode, String message) {
        super(statusCode, message);
    }
}
