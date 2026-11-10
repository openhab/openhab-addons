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
package org.openhab.binding.tailwind.internal.connector;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TailwindUnauthorizedException} is responsible for sending error messages
 * related to controller authentication errors
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindUnauthorizedException extends TailwindCommunicationException {
    private static final long serialVersionUID = 1L;

    public TailwindUnauthorizedException(int statusCode, Exception e) {
        super(statusCode, e);
    }

    public TailwindUnauthorizedException(int statusCode) {
        super(statusCode);
    }

    public TailwindUnauthorizedException(int statusCode, String message) {
        super(statusCode, message);
    }
}
