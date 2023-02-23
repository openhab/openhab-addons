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
package org.openhab.binding.opengarage.internal;

import java.io.IOException;

/**
 * Exception for when an unexpected response is received from the OpenGarage controller.
 *
 * @author Paul Smedley - Initial contribution
 *
 */
public class OpenGarageCommunicationException extends IOException {
    private static final long serialVersionUID = 529232811860854017L;

    public OpenGarageCommunicationException(String message) {
        super(message);
    }

    public OpenGarageCommunicationException(Throwable ex) {
        super(ex);
    }

    public OpenGarageCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
