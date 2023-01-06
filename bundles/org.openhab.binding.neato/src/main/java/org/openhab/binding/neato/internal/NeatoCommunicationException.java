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
package org.openhab.binding.neato.internal;

/**
 * Exception to encapsulate any issues communicating with Neato APIs
 *
 * @author Jeff Lauterbach - Initial Contribution
 */
public class NeatoCommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public NeatoCommunicationException(Throwable cause) {
        super("Error attempting to communicate with Neato", cause);
    }

    public NeatoCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
