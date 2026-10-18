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
package org.openhab.binding.atagone.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when communication with the ATAG ONE thermostat fails.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneCommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AtagOneCommunicationException(String message) {
        super(message);
    }

    public AtagOneCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
