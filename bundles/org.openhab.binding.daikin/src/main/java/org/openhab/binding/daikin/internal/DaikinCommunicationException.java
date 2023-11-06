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
package org.openhab.binding.daikin.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for when an unexpected response is received from the Daikin controller.
 *
 * @author Tim Waterhouse - Initial contribution
 *
 */
@NonNullByDefault
public class DaikinCommunicationException extends IOException {
    private static final long serialVersionUID = 529232811860854017L;

    public DaikinCommunicationException(String message) {
        super(message);
    }

    public DaikinCommunicationException(Throwable ex) {
        super(ex);
    }

    public DaikinCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
