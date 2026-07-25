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
package org.openhab.binding.homeconnectdirect.internal.servlet.routing;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown by request handlers.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class RequestHandlerException extends Exception {

    @Serial
    private static final long serialVersionUID = 852_133_612_771_426_467L;

    public RequestHandlerException(final String message) {
        super(message);
    }

    public RequestHandlerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
