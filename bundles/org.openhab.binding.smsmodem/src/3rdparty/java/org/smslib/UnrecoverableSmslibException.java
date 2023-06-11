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
package org.smslib;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Exception class for internal SMSLib unrecoverable error
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class UnrecoverableSmslibException extends RuntimeException {

    private static final long serialVersionUID = 7649578885702261759L;

    public UnrecoverableSmslibException(String message) {
        super(message);
    }

    public UnrecoverableSmslibException(String message, Exception cause) {
        super(message, cause);
    }
}
