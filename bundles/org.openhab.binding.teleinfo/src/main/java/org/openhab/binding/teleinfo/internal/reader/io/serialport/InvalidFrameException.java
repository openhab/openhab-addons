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
package org.openhab.binding.teleinfo.internal.reader.io.serialport;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link InvalidFrameException} class defines an exception for invalid frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class InvalidFrameException extends Exception {

    private static final long serialVersionUID = 4729529258857792922L;

    private final Date timestamp;

    public InvalidFrameException() {
        timestamp = new Date();
    }

    public InvalidFrameException(String message) {
        super(message, null);
        timestamp = new Date();
    }

    public InvalidFrameException(String message, Throwable cause) {
        super(message, cause);
        timestamp = new Date();
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
