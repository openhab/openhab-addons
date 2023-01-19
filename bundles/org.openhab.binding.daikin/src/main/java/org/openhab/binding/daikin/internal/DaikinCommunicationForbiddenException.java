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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for when a 403 Forbidden error is received from the Daikin controller.
 *
 * @author Jimmy Tanagra - Initial contribution
 *
 */
@NonNullByDefault
public class DaikinCommunicationForbiddenException extends DaikinCommunicationException {

    private static final long serialVersionUID = 1L;

    public DaikinCommunicationForbiddenException(String message) {
        super(message);
    }

    public DaikinCommunicationForbiddenException(Throwable ex) {
        super(ex);
    }

    public DaikinCommunicationForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
