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
package org.openhab.binding.squeezebox.internal.utils;

/**
 * Exception thrown when unable to communicate with LMS server.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SqueezeBoxCommunicationException extends Exception {
    private static final long serialVersionUID = 1540489268747099161L;

    public SqueezeBoxCommunicationException(String message) {
        super(message);
    }

    public SqueezeBoxCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
