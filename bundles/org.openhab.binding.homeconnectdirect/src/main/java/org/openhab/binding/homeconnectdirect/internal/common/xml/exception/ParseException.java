/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.common.xml.exception;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when XML parsing fails.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ParseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1_644_080_964_145_839_845L;

    public ParseException(final String message) {
        super(message);
    }

    public ParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
